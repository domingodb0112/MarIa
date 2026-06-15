package uaemex.ia.proyecto.cliente.view;

final class ErrorMessages {

    private ErrorMessages() {
    }

    static String rootMessage(Throwable error) {
        Throwable actual = error;
        while (actual.getCause() != null) {
            actual = actual.getCause();
        }
        String mensaje = actual.getMessage();
        return mensaje == null || mensaje.trim().isEmpty()
                ? actual.getClass().getSimpleName()
                : mensaje;
    }
}
