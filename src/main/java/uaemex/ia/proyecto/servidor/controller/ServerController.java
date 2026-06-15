package uaemex.ia.proyecto.servidor.controller;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ServerController {

    private static final Logger LOGGER = Logger.getLogger(ServerController.class.getName());

    private final int puerto;
    private final ExecutorService pool;

    public ServerController(int puerto) {
        this.puerto = puerto;
        this.pool = Executors.newCachedThreadPool();
    }

    public void iniciar() {
        LOGGER.info(() -> "Iniciando en puerto " + puerto + "...");
        try (ServerSocket serverSocket = new ServerSocket(puerto)) {
            LOGGER.info("Listo. Esperando conexiones...");
            while (true) {
                Socket clienteSocket = serverSocket.accept();
                LOGGER.info(() -> "Nueva conexion desde: "
                        + clienteSocket.getInetAddress().getHostAddress());
                pool.execute(new ManejadorCliente(clienteSocket));
            }
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Error fatal en el servidor.", e);
        } finally {
            pool.shutdown();
        }
    }
}
