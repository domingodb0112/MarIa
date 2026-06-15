package uaemex.ia.proyecto.cliente;

import uaemex.ia.proyecto.cliente.view.VentanaPrincipal;

import javax.swing.SwingUtilities;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.logging.Logger;

/**
 * Punto de entrada de la aplicacion cliente.
 * Lee la configuracion de red y abre la interfaz Swing en el hilo correcto.
 */
public class ClienteApp {

    private static final Logger LOGGER = Logger.getLogger(ClienteApp.class.getName());
    private static final String CONFIG_FILE = "config.properties";
    private static final String DEFAULT_HOST = "localhost";
    private static final int DEFAULT_PORT = 5000;

    /**
     * Inicia el cliente, obtiene host/puerto y crea la ventana principal.
     *
     * @param args argumentos de consola no utilizados por la aplicacion.
     */
    public static void main(String[] args) {
        Properties config = cargarConfiguracion();
        String host = config.getProperty("server.ip", DEFAULT_HOST).trim();
        int puerto = leerPuerto(config.getProperty("server.port"));

        // Swing requiere crear y manipular componentes dentro del Event Dispatch Thread.
        SwingUtilities.invokeLater(() -> {
            VentanaPrincipal ventana = new VentanaPrincipal(host, puerto);
            ventana.setVisible(true);
        });
    }

    /**
     * Carga el archivo config.properties o genera valores por defecto si no existe.
     *
     * @return propiedades con la direccion IP y el puerto del servidor.
     */
    private static Properties cargarConfiguracion() {
        Properties props = new Properties();
        try (InputStream entrada = new FileInputStream(CONFIG_FILE)) {
            props.load(entrada);
            LOGGER.info(() -> "Configuracion cargada desde " + CONFIG_FILE);
        } catch (IOException e) {
            props.setProperty("server.ip", DEFAULT_HOST);
            props.setProperty("server.port", String.valueOf(DEFAULT_PORT));
            LOGGER.info(() -> "Usando configuracion por defecto: "
                    + DEFAULT_HOST + ":" + DEFAULT_PORT);
        }
        return props;
    }

    /**
     * Convierte el puerto configurado a entero, con tolerancia a valores vacios o invalidos.
     *
     * @param valor texto leido desde la configuracion.
     * @return puerto valido para conectar con el servidor.
     */
    private static int leerPuerto(String valor) {
        if (valor == null || valor.trim().isEmpty()) {
            return DEFAULT_PORT;
        }
        try {
            return Integer.parseInt(valor.trim());
        } catch (NumberFormatException e) {
            LOGGER.warning(() -> "Puerto invalido en config.properties. Usando " + DEFAULT_PORT);
            return DEFAULT_PORT;
        }
    }
}
