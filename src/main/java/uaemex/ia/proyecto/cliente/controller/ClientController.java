package uaemex.ia.proyecto.cliente.controller;

import com.google.gson.Gson;
import uaemex.ia.proyecto.compartido.MensajeSocket;
import uaemex.ia.proyecto.compartido.RespuestaSocket;
import java.io.*;
import java.net.InetSocketAddress;
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
    private final Gson gson = new Gson();
    private Socket socket;
    private PrintWriter salida;
    private BufferedReader entrada;
    private final HeartbeatCliente heartbeat = new HeartbeatCliente(this);

    public ClientController(String host, int puerto) {
        this.host = host;
        this.puerto = puerto;
    }

    /**
     * Establece la conexión e inicia el latido periódico de red.
     */
    public synchronized void conectar() throws IOException {
        desconectar();
        abrirSocket();
        heartbeat.iniciar(); // Inicia el latido PING
        LOGGER.info(() -> "Conectado a " + host + ":" + puerto);
    }

    /**
     * Envía un mensaje de forma segura. Si la conexión falla, intenta reconectar con backoff.
     */
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

    // Ejecuta una petición atómica de red
    private RespuestaSocket enviarUnaVez(MensajeSocket mensaje) throws IOException {
        String json = gson.toJson(mensaje);
        salida.println(json); // Escribe trama en socket
        if (salida.checkError()) {
            desconectar();
            throw new IOException("No se pudo enviar el mensaje al servidor.");
        }
        try {
            String resp = entrada.readLine(); // Espera respuesta del socket
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

    // Envía el mensaje PING para validar el estado del servidor
    synchronized void enviarHeartbeat() throws IOException {
        if (!estaConectado()) reconectarConBackoff();
        RespuestaSocket respuesta = enviarUnaVez(new MensajeSocket("PING", null));
        if (!"OK".equals(respuesta.getStatus()) || !"PONG".equals(respuesta.getMensaje())) {
            desconectar();
            throw new IOException("Heartbeat PING/PONG invalido.");
        }
    }

    // Ciclo de reconexión con incrementos exponenciales de tiempo (backoff)
    private void reconectarConBackoff() throws IOException {
        IOException ultimoError = null;
        long espera = BACKOFF_INICIAL_MS;
        for (int intento = 1; intento <= MAX_REINTENTOS; intento++) {
            try {
                desconectar();
                abrirSocket();
                heartbeat.iniciar(); // Reinicia el latido PING
                LOGGER.info(() -> "Reconexion exitosa a " + host + ":" + puerto);
                return;
            } catch (IOException e) {
                ultimoError = e;
                dormirBackoff(espera, intento);
                espera = Math.min(espera * 2, BACKOFF_MAX_MS); // Duplica el tiempo
            }
        }
        throw new IOException("No se pudo reconectar tras " + MAX_REINTENTOS + " intento(s).", ultimoError);
    }

    // Instancia el Socket y prepara los flujos de lectura/escritura
    private void abrirSocket() throws IOException {
        socket = new Socket();
        socket.connect(new InetSocketAddress(host, puerto), CONNECT_TIMEOUT_MS);
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

    /**
     * Cierra el socket y detiene los latidos de red.
     */
    public synchronized void desconectar() {
        heartbeat.detener(); // Detiene el latidor
        if (socket != null && !socket.isClosed()) {
            try { socket.close(); } catch (IOException ignored) {}
        }
        socket = null;
        salida = null;
        entrada = null;
    }
}
