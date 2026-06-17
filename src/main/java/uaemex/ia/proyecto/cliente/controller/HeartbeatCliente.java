package uaemex.ia.proyecto.cliente.controller;

import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

/**
 * Gestiona el envío periódico de latidos (heartbeats) hacia el servidor.
 * Se ejecuta de manera asíncrona mediante un ScheduledExecutorService con hilos daemon
 * para evitar interferir o congelar la interfaz visual (EDT).
 */
class HeartbeatCliente {
    private static final Logger LOGGER = Logger.getLogger(HeartbeatCliente.class.getName());
    private static final long INTERVALO_SEGUNDOS = 15;
    
    private final ClientController controller;
    private ScheduledExecutorService executor;

    HeartbeatCliente(ClientController controller) {
        this.controller = controller;
    }

    /**
     * Inicia la ejecución periódica del latido.
     * Detiene previamente cualquier tarea activa para evitar duplicaciones.
     */
    synchronized void iniciar() {
        detener();
        executor = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread hilo = new Thread(r, "maria-heartbeat");
            hilo.setDaemon(true); // Se configura como daemon para no bloquear el apagado de la JVM
            return hilo;
        });
        executor.scheduleAtFixedRate(this::latir, INTERVALO_SEGUNDOS,
                INTERVALO_SEGUNDOS, TimeUnit.SECONDS);
    }

    /**
     * Detiene inmediatamente el servicio de latidos.
     */
    synchronized void detener() {
        if (executor != null) {
            executor.shutdownNow();
            executor = null;
        }
    }

    // Envía la solicitud PING a través del controlador
    private void latir() {
        try {
            controller.enviarHeartbeat();
        } catch (IOException e) {
            LOGGER.warning(() -> "Heartbeat fallido: " + e.getMessage());
        }
    }
}
