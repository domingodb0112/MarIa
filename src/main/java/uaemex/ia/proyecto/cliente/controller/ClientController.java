package uaemex.ia.proyecto.cliente.controller;

import com.google.gson.Gson;
import uaemex.ia.proyecto.compartido.MensajeSocket;
import uaemex.ia.proyecto.compartido.RespuestaSocket;
import uaemex.ia.proyecto.compartido.TlsConfig;
import java.io.*;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.logging.Logger;

/**
 * Controlador de comunicación de red para el cliente.
 * Se encarga de abrir y cerrar sockets TCP, enviar mensajes JSON y controlar la reconexión.
 */
public class ClientController {
    private static final Logger LOGGER = Logger.getLogger(ClientController.class.getName());
    private static final int CONNECT_TIMEOUT_MS = 5000, READ_TIMEOUT_MS = 7000, MAX_REINTENTOS = 4;
    private static final long BACKOFF_INICIAL_MS = 400, BACKOFF_MAX_MS = 5000;

    private final String host;
    private final int puerto;
    private final TlsConfig tlsConfig;
    private final Gson gson = new Gson();
    private Socket socket;
    private PrintWriter salida;
    private BufferedReader entrada;
    private final HeartbeatCliente heartbeat = new HeartbeatCliente(this);

    public ClientController(String host, int puerto) {
        this(host, puerto, null);
    }

    public ClientController(String host, int puerto, TlsConfig tlsConfig) {
        this.host = host;
        this.puerto = puerto;
        this.tlsConfig = tlsConfig;
    }

    public synchronized void conectar() throws IOException {
        desconectar();
        abrirSocket();
        heartbeat.iniciar();
        LOGGER.info(() -> "Conectado a " + host + ":" + puerto);
    }

    public synchronized RespuestaSocket enviarMensaje(MensajeSocket mensaje) throws IOException {
        if (!estaConectado()) reconectarConBackoff();
        try {
            return enviarUnaVez(mensaje);
        } catch (IOException e) {
            LOGGER.warning(() -> "Envio fallido, intentando reconectar: " + e.getMessage());
            reconectarConBackoff();
            return enviarUnaVez(mensaje);
        }
    }

    private RespuestaSocket enviarUnaVez(MensajeSocket mensaje) throws IOException {
        String json = gson.toJson(mensaje);
        salida.println(json);
        if (salida.checkError()) {
            desconectar();
            throw new IOException("No se pudo enviar el mensaje al servidor.");
        }
        try {
            String resp = entrada.readLine();
            if (resp == null) {
                desconectar();
                throw new IOException("El servidor cerro la conexion.");
            }
            return gson.fromJson(resp, RespuestaSocket.class);
        } catch (SocketTimeoutException e) {
            desconectar();
            throw new SocketTimeoutException("El servidor no respondio a tiempo.");
        }
    }

    synchronized void enviarHeartbeat() throws IOException {
        if (!estaConectado()) reconectarConBackoff();
        RespuestaSocket respuesta = enviarUnaVez(new MensajeSocket("PING", null));
        if (!"OK".equals(respuesta.getStatus()) || !"PONG".equals(respuesta.getMensaje())) {
            desconectar();
            throw new IOException("Heartbeat PING/PONG invalido.");
        }
    }

    private void reconectarConBackoff() throws IOException {
        IOException ultimoError = null;
        long espera = BACKOFF_INICIAL_MS;
        for (int intento = 1; intento <= MAX_REINTENTOS; intento++) {
            try {
                desconectar();
                abrirSocket();
                heartbeat.iniciar();
                LOGGER.info(() -> "Reconexion exitosa a " + host + ":" + puerto);
                return;
            } catch (IOException e) {
                ultimoError = e;
                dormirBackoff(espera, intento);
                espera = Math.min(espera * 2, BACKOFF_MAX_MS);
            }
        }
        throw new IOException("No se pudo reconectar tras " + MAX_REINTENTOS + " intento(s).", ultimoError);
    }

    private void abrirSocket() throws IOException {
        try {
            socket = TlsClientSockets.conectar(host, puerto, CONNECT_TIMEOUT_MS, tlsConfig);
        } catch (Exception e) {
            throw new IOException("No se pudo abrir socket TLS/TCP.", e);
        }
        socket.setSoTimeout(READ_TIMEOUT_MS);
        salida = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()), true);
        entrada = new BufferedReader(new InputStreamReader(socket.getInputStream()));
    }

    private void dormirBackoff(long espera, int intento) throws IOException {
        try {
            LOGGER.info(() -> "Reintentando conexion en " + espera + " ms. Intento " + intento);
            Thread.sleep(espera);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("Reconexion interrumpida.", e);
        }
    }

    public synchronized boolean estaConectado() {
        return socket != null && socket.isConnected() && !socket.isClosed();
    }

    public synchronized void desconectar() {
        heartbeat.detener();
        if (socket != null && !socket.isClosed()) {
            try { socket.close(); } catch (IOException ignored) {}
        }
        socket = null;
        salida = null;
        entrada = null;
    }
}
