package uaemex.ia.proyecto.servidor.controller;

import org.junit.Test;
import uaemex.ia.proyecto.compartido.RespuestaSocket;
import static org.junit.Assert.*;

/**
 * Pruebas unitarias para validar el protocolo de comunicación PING/PONG a través de sockets.
 */
public class PingPongSocketTest {

    /**
     * Valida que el servidor responda correctamente con PONG cuando recibe un mensaje PING en JSON.
     */
    @Test
    public void respondePongEnProtocoloJson() {
        ManejadorCliente manejador = new ManejadorCliente(null);

        // Envía una trama JSON simulada de PING
        RespuestaSocket respuesta = manejador.procesarMensaje(
                "{\"transaccionId\":\"t-ping\",\"accion\":\"PING\"}");

        // Verifica que la respuesta sea exitosa y contenga la palabra PONG
        assertEquals("t-ping", respuesta.getTransaccionId());
        assertEquals("OK", respuesta.getStatus());
        assertEquals("PONG", respuesta.getMensaje());
    }
}
