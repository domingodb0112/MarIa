package uaemex.ia.proyecto.cliente.view;

/**
 * Extrae mensajes de error legibles para mostrarlos al usuario en la interfaz.
 */
final class ErrorMessages {

    /**
     * Evita crear instancias de esta clase de apoyo.
     */
    private ErrorMessages() {
    }

    /**
     * Busca la causa raiz de una excepcion para evitar mensajes genericos de envoltura.
     *
     * @param error excepcion capturada durante una operacion.
     * @return mensaje concreto o el nombre de la clase si no hay texto disponible.
     */
    static String rootMessage(Throwable error) {
        Throwable actual = error;
        // Algunas APIs envuelven la causa real; recorrer la cadena muestra el problema util.
        while (actual.getCause() != null) {
            actual = actual.getCause();
        }
        String mensaje = actual.getMessage();
        return mensaje == null || mensaje.trim().isEmpty()
                ? actual.getClass().getSimpleName()
                : mensaje;
    }
}
