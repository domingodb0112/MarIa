package uaemex.ia.proyecto.servidor.controller;

import org.junit.Test;
import uaemex.ia.proyecto.compartido.MensajeSocket;
import uaemex.ia.proyecto.compartido.RespuestaSocket;
import java.util.concurrent.atomic.AtomicInteger;
import static org.junit.Assert.*;

/**
 * Pruebas unitarias para validar que el control de idempotencia (LRU Cache) funcione de manera correcta.
 */
public class RegistroTransaccionesTest {

    /**
     * Valida que ejecuciones repetidas con el mismo transaccionId retornen la respuesta cacheada
     * sin volver a ejecutar la lógica interna de negocio.
     */
    @Test
    public void reutilizaRespuestaParaMismaTransaccion() {
        AtomicInteger llamadas = new AtomicInteger();
        MensajeSocket mensaje = new MensajeSocket("REGISTRAR_DISCO", null);

        // Primera llamada: debe procesar y cachear
        RespuestaSocket primera = RegistroTransacciones.ejecutar(mensaje, () -> {
            llamadas.incrementAndGet();
            return RespuestaSocket.ok(mensaje.getTransaccionId(), "ok", null);
        });
        
        // Segunda llamada: debe retornar la caché directamente
        RespuestaSocket segunda = RegistroTransacciones.ejecutar(mensaje, () -> {
            llamadas.incrementAndGet();
            return RespuestaSocket.error(mensaje.getTransaccionId(), "duplicada");
        });

        // Comprueba que sea el mismo objeto e invocación única
        assertSame(primera, segunda);
        assertEquals(1, llamadas.get());
        assertEquals("ok", segunda.getMensaje());
    }
}
