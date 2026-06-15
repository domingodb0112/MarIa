package uaemex.ia.proyecto.servidor.controller;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import uaemex.ia.proyecto.compartido.MensajeSocket;
import uaemex.ia.proyecto.compartido.RespuestaSocket;

import java.io.*;
import java.net.Socket;
import java.util.logging.Logger;

public class ManejadorCliente implements Runnable {

    private static final Logger LOGGER = Logger.getLogger(ManejadorCliente.class.getName());

    private final Socket socket;
    private final Gson gson = new Gson();
    private final AccionesCliente acciones = new AccionesCliente();

    public ManejadorCliente(Socket socket) {
        this.socket = socket;
    }

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
