package uaemex.ia.proyecto.servidor.controller;

import uaemex.ia.proyecto.compartido.MensajeSocket;
import uaemex.ia.proyecto.compartido.RespuestaSocket;

import java.util.logging.Logger;

final class TransactionLog {
    private TransactionLog() {}

    static void recibido(Logger log, String remote, MensajeSocket msg) {
        log.info(() -> String.format(
                "event=socket_received correlationId=%s userId=%s remote=%s action=%s page=%d size=%d",
                msg.correlationId(), msg.getUserId(), remote, msg.getAccion(),
                msg.getPagina(), msg.getTamanoPagina()));
    }

    static void respondido(Logger log, MensajeSocket msg, RespuestaSocket resp) {
        log.info(() -> String.format(
                "event=socket_response correlationId=%s userId=%s action=%s status=%s total=%d",
                msg.correlationId(), msg.getUserId(), msg.getAccion(), resp.getStatus(), resp.getTotal()));
    }

    static void error(Logger log, String correlationId, String reason) {
        log.warning(() -> String.format(
                "event=socket_error correlationId=%s reason=\"%s\"", correlationId, reason));
    }
}
