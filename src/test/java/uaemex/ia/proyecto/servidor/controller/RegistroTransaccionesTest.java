package uaemex.ia.proyecto.servidor.controller;

import org.junit.Test;
import uaemex.ia.proyecto.compartido.MensajeSocket;
import uaemex.ia.proyecto.compartido.RespuestaSocket;

import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.*;

public class RegistroTransaccionesTest {
    @Test
    public void reutilizaRespuestaParaMismaTransaccion() {
        AtomicInteger llamadas = new AtomicInteger();
        MensajeSocket mensaje = new MensajeSocket("REGISTRAR_DISCO", null);

        RespuestaSocket primera = RegistroTransacciones.ejecutar(mensaje, () -> {
            llamadas.incrementAndGet();
            return RespuestaSocket.ok(mensaje.getTransaccionId(), "ok", null);
        });
        RespuestaSocket segunda = RegistroTransacciones.ejecutar(mensaje, () -> {
            llamadas.incrementAndGet();
            return RespuestaSocket.error(mensaje.getTransaccionId(), "duplicada");
        });

        assertSame(primera, segunda);
        assertEquals(1, llamadas.get());
        assertEquals("ok", segunda.getMensaje());
    }
}
