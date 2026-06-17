package uaemex.ia.proyecto.servidor.controller;

import uaemex.ia.proyecto.compartido.Disco;
import java.time.Year;

/**
 * Validador del lado del servidor para objetos Disco.
 * Asegura la integridad de los datos recibidos antes de ser registrados en la base de datos.
 */
final class ValidadorDiscoServidor {
    private ValidadorDiscoServidor() {
    }

    /**
     * Valida que todos los campos requeridos estén presentes y que el año sea válido.
     *
     * @param disco el objeto Disco recibido desde el cliente.
     * @return una cadena vacía si es válido, o el mensaje de error correspondiente.
     */
    static String validar(Disco disco) {
        if (disco == null) {
            return "Se requieren datos del disco.";
        }
        if (vacio(disco.getTitulo())) {
            return "El titulo es obligatorio.";
        }
        if (vacio(disco.getArtista())) {
            return "El artista es obligatorio.";
        }
        if (vacio(disco.getGenero())) {
            return "El genero es obligatorio.";
        }
        if (vacio(disco.getFormato())) {
            return "El formato es obligatorio.";
        }
        // El año no puede ser en el futuro lejano (se permite el año actual + 1 para preventas)
        int maximo = Year.now().getValue() + 1;
        if (disco.getAnio() < 1900 || disco.getAnio() > maximo) {
            return "El anio debe estar entre 1900 y " + maximo + ".";
        }
        return "";
    }

    private static boolean vacio(String texto) {
        return texto == null || texto.trim().isEmpty();
    }
}
