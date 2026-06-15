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

    /**
     * Devuelve una representacion util para trazas de depuracion.
     *
     * @return texto con id, accion y datos.
     */
    @Override
    public String toString() {
        return String.format("MensajeSocket{id='%s', accion='%s', datos=%s}", transaccionId, accion, datos);
    }
}
