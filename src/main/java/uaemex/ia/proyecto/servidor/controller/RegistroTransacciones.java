package uaemex.ia.proyecto.servidor.controller;

import uaemex.ia.proyecto.compartido.MensajeSocket;
import uaemex.ia.proyecto.compartido.RespuestaSocket;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Supplier;

/**
 * Controla e implementa la idempotencia de transacciones en el servidor.
 * Almacena las respuestas de las ultimas transacciones procesadas (LRU Cache)
 * para evitar procesar dos veces la misma solicitud ante reintentos de red.
 */
final class RegistroTransacciones {
    private static final int MAX_ENTRADAS = 300;
    
    // Cache de tipo LRU (Least Recently Used) basada en LinkedHashMap
    private static final Map<String, RespuestaSocket> CACHE = new LinkedHashMap<String, RespuestaSocket>() {
        @Override
        protected boolean removeEldestEntry(Map.Entry<String, RespuestaSocket> eldest) {
            return size() > MAX_ENTRADAS;
        }
    };

    private RegistroTransacciones() {
    }

    /**
     * Ejecuta una accion de manera idempotente.
     * Si la transaccion ya fue procesada previamente, retorna la respuesta cacheada.
     *
     * @param mensaje el mensaje recibido por el socket que contiene el transaccionId.
     * @param accion la funcion lambda de negocio que genera la respuesta si no esta cacheada.
     * @return la respuesta del socket (nueva o recuperada de la cache).
     */
    static synchronized RespuestaSocket ejecutar(MensajeSocket mensaje, Supplier<RespuestaSocket> accion) {
        String id = mensaje == null ? null : mensaje.getTransaccionId();
        if (id == null || id.trim().isEmpty()) {
            return accion.get();
        }
        RespuestaSocket previa = CACHE.get(id);
        if (previa != null) {
            return previa; // Retorna la respuesta previa para asegurar idempotencia
        }
        RespuestaSocket respuesta = accion.get();
        CACHE.put(id, respuesta); // Guarda la respuesta en la cache LRU
        return respuesta;
    }
}
