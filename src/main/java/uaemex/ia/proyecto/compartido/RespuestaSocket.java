package uaemex.ia.proyecto.compartido;

import java.util.List;

/**
 * Respuesta que el servidor envia al cliente despues de procesar una accion.
 */
public class RespuestaSocket {

    private String transaccionId;
    private String status;        // "OK" o "ERROR"
    private String mensaje;
    private Disco datos;          // null cuando la respuesta no lleva un disco individual
    private List<Disco> listaDiscos; // usado por LISTAR_DISCOS

    /**
     * Constructor vacio requerido por Gson para reconstruir respuestas desde JSON.
     */
    public RespuestaSocket() {}

    /**
     * Fabrica una respuesta exitosa con un disco individual.
     *
     * @param transaccionId id de la solicitud original.
     * @param mensaje mensaje humano del resultado.
     * @param datos disco asociado a la respuesta.
     * @return respuesta marcada como OK.
     */
    public static RespuestaSocket ok(String transaccionId, String mensaje, Disco datos) {
        RespuestaSocket r = new RespuestaSocket();
        r.transaccionId = transaccionId;
        r.status = "OK";
        r.mensaje = mensaje;
        r.datos = datos;
        return r;
    }

    /**
     * Fabrica una respuesta exitosa con una lista de discos.
     *
     * @param transaccionId id de la solicitud original.
     * @param mensaje mensaje humano del resultado.
     * @param lista discos encontrados, listados o recomendados.
     * @return respuesta marcada como OK.
     */
    public static RespuestaSocket okLista(String transaccionId, String mensaje, List<Disco> lista) {
        RespuestaSocket r = new RespuestaSocket();
        r.transaccionId = transaccionId;
        r.status = "OK";
        r.mensaje = mensaje;
        r.listaDiscos = lista;
        return r;
    }

    /**
     * Fabrica una respuesta de error sin datos adicionales.
     *
     * @param transaccionId id de la solicitud original o N/A si no pudo leerse.
     * @param mensaje descripcion del problema.
     * @return respuesta marcada como ERROR.
     */
    public static RespuestaSocket error(String transaccionId, String mensaje) {
        RespuestaSocket r = new RespuestaSocket();
        r.transaccionId = transaccionId;
        r.status = "ERROR";
        r.mensaje = mensaje;
        return r;
    }

    /** @return id compartido con la solicitud que origino la respuesta. */
    public String getTransaccionId()    { return transaccionId; }
    /** @return estado de la operacion, OK o ERROR. */
    public String getStatus()           { return status; }
    /** @return mensaje explicativo para el cliente. */
    public String getMensaje()          { return mensaje; }
    /** @return disco individual cuando la respuesta lo incluye. */
    public Disco getDatos()             { return datos; }
    /** @return lista de discos para operaciones de consulta. */
    public List<Disco> getListaDiscos() { return listaDiscos; }

    /**
     * Representacion de depuracion de la respuesta completa.
     *
     * @return texto con id, estado, mensaje y datos.
     */
    @Override
    public String toString() {
        return String.format("RespuestaSocket{id='%s', status='%s', mensaje='%s', datos=%s, lista=%s}",
                transaccionId, status, mensaje, datos, listaDiscos);
    }
}
