package uaemex.ia.proyecto.cliente.controller;

import com.google.gson.Gson;
import uaemex.ia.proyecto.compartido.MensajeSocket;
import uaemex.ia.proyecto.compartido.RespuestaSocket;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.logging.Logger;

public class ClientController {

    private static final Logger LOGGER = Logger.getLogger(ClientController.class.getName());
    private static final int CONNECT_TIMEOUT_MS = 5000;
    private static final int READ_TIMEOUT_MS = 7000;

    private final String host;
    private final int puerto;
    private final Gson gson = new Gson();

    private Socket socket;
    private PrintWriter salida;
    private BufferedReader entrada;

    public ClientController(String host, int puerto) {
        this.host = host;
        this.puerto = puerto;
    }

    public void conectar() throws IOException {
        desconectar();
        Socket nuevoSocket = new Socket();
        nuevoSocket.connect(new InetSocketAddress(host, puerto), CONNECT_TIMEOUT_MS);
        nuevoSocket.setSoTimeout(READ_TIMEOUT_MS);
        socket = nuevoSocket;
        salida = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()), true);
        entrada = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        LOGGER.info(() -> "Conectado a " + host + ":" + puerto);
    }

    public RespuestaSocket enviarMensaje(MensajeSocket mensaje) throws IOException {
        if (!estaConectado()) {
            throw new IOException("No hay conexion activa con el servidor.");
        }

        String json = gson.toJson(mensaje);
        LOGGER.fine(() -> "Enviando: " + json);
        salida.println(json);
        if (salida.checkError()) {
            desconectar();
            throw new IOException("No se pudo enviar el mensaje al servidor.");
        }

        String jsonRespuesta;
        try {
            jsonRespuesta = entrada.readLine();
        } catch (SocketTimeoutException e) {
            desconectar();
            throw new SocketTimeoutException("El servidor no respondio a tiempo.");
        }

        if (jsonRespuesta == null) {
            desconectar();
            throw new IOException("El servidor cerro la conexion.");
        }
        LOGGER.fine(() -> "Respuesta: " + jsonRespuesta);
        return gson.fromJson(jsonRespuesta, RespuestaSocket.class);
    }

    public boolean estaConectado() {
        return socket != null && socket.isConnected() && !socket.isClosed();
    }

    public void desconectar() {
        if (socket != null && !socket.isClosed()) {
            try {
                socket.close();
                LOGGER.info("Desconectado.");
            } catch (IOException ignored) {
            }
        }
        socket = null;
        salida = null;
        entrada = null;
    }
}
