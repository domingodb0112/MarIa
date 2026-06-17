package uaemex.ia.proyecto.servidor.controller;

import org.junit.Test;
import uaemex.ia.proyecto.compartido.RespuestaSocket;

import static org.junit.Assert.*;

public class PingPongSocketTest {
    @Test
    public void respondePongEnProtocoloJson() {
        ManejadorCliente manejador = new ManejadorCliente(null);

        RespuestaSocket respuesta = manejador.procesarMensaje(
                "{\"transaccionId\":\"t-ping\",\"accion\":\"PING\"}");

        assertEquals("t-ping", respuesta.getTransaccionId());
        assertEquals("OK", respuesta.getStatus());
        assertEquals("PONG", respuesta.getMensaje());
    }
}
