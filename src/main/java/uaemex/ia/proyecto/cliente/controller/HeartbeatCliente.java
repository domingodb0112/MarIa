package uaemex.ia.proyecto.cliente.controller;

import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

class HeartbeatCliente {
    private static final Logger LOGGER = Logger.getLogger(HeartbeatCliente.class.getName());
    private static final long INTERVALO_SEGUNDOS = 15;
    private final ClientController controller;
    private ScheduledExecutorService executor;

    HeartbeatCliente(ClientController controller) {
        this.controller = controller;
    }

    synchronized void iniciar() {
        detener();
        executor = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread hilo = new Thread(r, "maria-heartbeat");
            hilo.setDaemon(true);
            return hilo;
        });
        executor.scheduleAtFixedRate(this::latir, INTERVALO_SEGUNDOS,
                INTERVALO_SEGUNDOS, TimeUnit.SECONDS);
    }

    synchronized void detener() {
        if (executor != null) {
            executor.shutdownNow();
            executor = null;
        }
    }

    private void latir() {
        try {
            controller.enviarHeartbeat();
        } catch (IOException e) {
            LOGGER.warning(() -> "Heartbeat fallido: " + e.getMessage());
        }
    }
}
