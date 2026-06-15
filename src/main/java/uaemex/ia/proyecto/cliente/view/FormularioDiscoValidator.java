package uaemex.ia.proyecto.cliente.view;

import uaemex.ia.proyecto.compartido.Disco;

import javax.swing.JOptionPane;
import java.awt.Component;
import java.util.Optional;

final class FormularioDiscoValidator {

    private FormularioDiscoValidator() {
    }

    static Optional<Disco> validar(Component parent, FormularioDiscoData data) {
        if (data.titulo.isEmpty() || data.artista.isEmpty()
                || data.anio.isEmpty() || data.genero.isEmpty()) {
            mostrar(parent, "Todos los campos son obligatorios.");
            return Optional.empty();
        }

        try {
            int anio = Integer.parseInt(data.anio);
            if (anio < 1900 || anio > 2100) {
                throw new NumberFormatException();
            }
            return Optional.of(new Disco(data.titulo, data.artista, anio, data.genero, data.formato));
        } catch (NumberFormatException e) {
            mostrar(parent, "El anio debe ser un numero valido (ej. 1973).");
            return Optional.empty();
        }
    }

    private static void mostrar(Component parent, String mensaje) {
        JOptionPane.showMessageDialog(parent, mensaje, "Validacion", JOptionPane.WARNING_MESSAGE);
    }
}
