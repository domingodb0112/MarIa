package uaemex.ia.proyecto.servidor.controller;

import uaemex.ia.proyecto.compartido.MensajeSocket;
import uaemex.ia.proyecto.compartido.RespuestaSocket;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Supplier;

final class RegistroTransacciones {
    private static final int MAX_ENTRADAS = 300;
    private static final Map<String, RespuestaSocket> CACHE = new LinkedHashMap<String, RespuestaSocket>() {
        @Override
        protected boolean removeEldestEntry(Map.Entry<String, RespuestaSocket> eldest) {
            return size() > MAX_ENTRADAS;
        }
    };

    private RegistroTransacciones() {
    }

    static synchronized RespuestaSocket ejecutar(MensajeSocket mensaje, Supplier<RespuestaSocket> accion) {
        String id = mensaje == null ? null : mensaje.getTransaccionId();
        if (id == null || id.trim().isEmpty()) {
            return accion.get();
        }
        RespuestaSocket previa = CACHE.get(id);
        if (previa != null) {
            return previa;
        }
        RespuestaSocket respuesta = accion.get();
        CACHE.put(id, respuesta);
        return respuesta;
    }
}
