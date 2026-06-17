package uaemex.ia.proyecto.servidor;

import uaemex.ia.proyecto.servidor.controller.ServerController;
import uaemex.ia.proyecto.compartido.TlsConfig;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.logging.Logger;

/**
 * Punto de entrada de la aplicacion servidor.
 * Lee el puerto configurado y delega el ciclo de escucha al ServerController.
 */
public class ServidorApp {

    private static final Logger LOGGER = Logger.getLogger(ServidorApp.class.getName());
    private static final String CONFIG_FILE = "config.properties";
    private static final int DEFAULT_PORT = 5000;

    /**
     * Inicia el servidor TCP usando el puerto definido en config.properties o el valor por defecto.
     *
     * @param args argumentos de consola no utilizados por la aplicacion.
     */
    public static void main(String[] args) {
        Properties config = cargarConfiguracion();
        int puerto = leerPuerto(config.getProperty("server.port"));
        ServerController servidor = new ServerController(puerto, new TlsConfig(config));
        servidor.iniciar();
    }

    /**
     * Carga el puerto del archivo de configuracion.
     *
     * @return puerto valido para abrir el ServerSocket.
     */
    private static Properties cargarConfiguracion() {
        Properties props = new Properties();
        try (InputStream entrada = new FileInputStream(CONFIG_FILE)) {
            props.load(entrada);
        } catch (IOException e) {
            LOGGER.info(() -> "No se encontro " + CONFIG_FILE
                    + ". Usando puerto por defecto " + DEFAULT_PORT);
            props.setProperty("server.port", String.valueOf(DEFAULT_PORT));
        }
        return props;
    }

    /**
     * Convierte el texto del puerto a entero y valida el rango permitido por TCP.
     *
     * @param valor valor leido desde la configuracion.
     * @return puerto valido o el puerto por defecto ante errores.
     */
    private static int leerPuerto(String valor) {
        if (valor == null || valor.trim().isEmpty()) {
            LOGGER.warning(() -> "server.port no esta definido. Usando " + DEFAULT_PORT);
            return DEFAULT_PORT;
        }
        try {
            int puerto = Integer.parseInt(valor.trim());
            if (puerto < 1 || puerto > 65535) {
                LOGGER.warning(() -> "server.port fuera de rango. Usando " + DEFAULT_PORT);
                return DEFAULT_PORT;
            }
            return puerto;
        } catch (NumberFormatException e) {
            LOGGER.warning(() -> "server.port invalido. Usando " + DEFAULT_PORT);
            return DEFAULT_PORT;
        }
    }
}
