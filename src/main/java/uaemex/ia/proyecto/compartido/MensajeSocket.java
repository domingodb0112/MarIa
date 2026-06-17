package uaemex.ia.proyecto.compartido;

import java.util.UUID;

/**
 * Solicitud enviada por el cliente al servidor a traves del socket.
 * Incluye un identificador de transaccion, la accion solicitada y datos opcionales.
 */
public class MensajeSocket {

    private String transaccionId;
    private String accion;
    private Disco datos;
    private String userId = "default-user";
    private int pagina = 0;
    private int tamanoPagina = 100;

    /**
     * Constructor vacio para deserializacion JSON; genera un id si se crea manualmente.
     */
    public MensajeSocket() {
        this.transaccionId = UUID.randomUUID().toString();
    }

    /**
     * Crea una solicitud completa para enviar al servidor.
     *
     * @param accion accion que debe ejecutar el servidor.
     * @param datos disco asociado o null si la accion no requiere datos.
     */
    public MensajeSocket(String accion, Disco datos) {
        this.transaccionId = UUID.randomUUID().toString();
        this.accion = accion;
        this.datos = datos;
    }

    public MensajeSocket(String accion, Disco datos, String userId) {
        this(accion, datos);
        setUserId(userId);
    }

    /** @return identificador unico para relacionar solicitud y respuesta. */
    public String getTransaccionId() { return transaccionId; }
    /** @param transaccionId identificador recibido o asignado manualmente. */
    public void setTransaccionId(String transaccionId) { this.transaccionId = transaccionId; }

    /** @return accion solicitada al servidor. */
    public String getAccion() { return accion; }
    /** @param accion nueva accion solicitada. */
    public void setAccion(String accion) { this.accion = accion; }

    /** @return datos enviados con la accion. */
    public Disco getDatos() { return datos; }
    /** @param datos disco enviado con la accion. */
    public void setDatos(Disco datos) { this.datos = datos; }

    public String getUserId() { return userId == null || userId.trim().isEmpty() ? "default-user" : userId; }
    public void setUserId(String userId) {
        this.userId = userId == null || userId.trim().isEmpty() ? "default-user" : userId.trim();
    }

    public int getPagina() { return Math.max(0, pagina); }
    public void setPagina(int pagina) { this.pagina = Math.max(0, pagina); }

    public int getTamanoPagina() { return tamanoPagina <= 0 ? 100 : Math.min(tamanoPagina, 500); }
    public void setTamanoPagina(int tamanoPagina) {
        this.tamanoPagina = tamanoPagina <= 0 ? 100 : Math.min(tamanoPagina, 500);
    }

    public String correlationId() { return transaccionId == null ? "N/A" : transaccionId; }

    /**
     * Devuelve una representacion util para trazas de depuracion.
     *
     * @return texto con id, accion y datos.
     */
    @Override
    public String toString() {
        return String.format("MensajeSocket{id='%s', user='%s', accion='%s', datos=%s}",
                transaccionId, getUserId(), accion, datos);
    }
}
