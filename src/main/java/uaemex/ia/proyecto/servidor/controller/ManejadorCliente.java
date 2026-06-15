package uaemex.ia.proyecto.servidor.controller;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import uaemex.ia.proyecto.compartido.MensajeSocket;
import uaemex.ia.proyecto.compartido.RespuestaSocket;

import java.io.*;
import java.net.Socket;
import java.util.logging.Logger;

/**
 * Atiende a un cliente TCP en su propio hilo.
 * Lee solicitudes JSON linea por linea y responde con JSON en el mismo socket.
 */
public class ManejadorCliente implements Runnable {

    private static final Logger LOGGER = Logger.getLogger(ManejadorCliente.class.getName());

    private final Socket socket;
    private final Gson gson = new Gson();
    private final AccionesCliente acciones = new AccionesCliente();

    /**
     * Recibe el socket aceptado por el servidor.
     *
     * @param socket conexion dedicada a un cliente.
     */
    public ManejadorCliente(Socket socket) {
        this.socket = socket;
    }

    /**
     * Ciclo principal del cliente: leer JSON, procesarlo y escribir la respuesta.
     */
    @Override
    public void run() {
        String direccion = socket.getInetAddress().getHostAddress();
        LOGGER.info(() -> "Hilo iniciado para cliente: " + direccion);

        try (
            BufferedReader entrada = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter salida = new PrintWriter(socket.getOutputStream(), true)
        ) {
            String lineaJson;
            while ((lineaJson = entrada.readLine()) != null) {
                final String jsonRecibido = lineaJson;
                LOGGER.fine(() -> "JSON recibido de " + direccion + ": " + jsonRecibido);
                // Cada linea representa una solicitud independiente dentro de la misma conexion.
                RespuestaSocket respuesta = procesarMensaje(jsonRecibido);
                String jsonRespuesta = gson.toJson(respuesta);
                LOGGER.fine(() -> "JSON enviado a " + direccion + ": " + jsonRespuesta);
                salida.println(jsonRespuesta);
            }
        } catch (IOException e) {
            LOGGER.info(() -> "Cliente " + direccion + " desconectado: " + e.getMessage());
        } finally {
            try { socket.close(); } catch (IOException ignored) {}
            LOGGER.info(() -> "Hilo terminado para cliente: " + direccion);
        }
    }

    /**
     * Convierte una linea JSON a MensajeSocket y despacha la accion correspondiente.
     *
     * @param lineaJson texto recibido desde el socket.
     * @return respuesta serializable para enviar al cliente.
     */
    private RespuestaSocket procesarMensaje(String lineaJson) {
        MensajeSocket mensaje;
        try {
            mensaje = gson.fromJson(lineaJson, MensajeSocket.class);
        } catch (JsonSyntaxException e) {
            return RespuestaSocket.error("N/A", "JSON malformado: " + e.getMessage());
        }

        if (mensaje.getAccion() == null) {
            return RespuestaSocket.error(mensaje.getTransaccionId(), "El campo 'accion' es obligatorio.");
        }

        // El protocolo se mantiene explicito para que cliente y servidor compartan los mismos nombres.
        switch (mensaje.getAccion()) {
            case "REGISTRAR_DISCO":         return acciones.registrarDisco(mensaje);
            case "LISTAR_DISCOS":           return acciones.listarDiscos(mensaje);
            case "BUSCAR_ALBUM":            return acciones.buscarAlbum(mensaje);
            case "OBTENER_RECOMENDACIONES": return acciones.obtenerRecomendaciones(mensaje);
            default:
                return RespuestaSocket.error(mensaje.getTransaccionId(),
                        "Accion no reconocida: " + mensaje.getAccion());
        }
    }
}
