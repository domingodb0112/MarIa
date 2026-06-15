package uaemex.ia.proyecto.cliente.view;

import uaemex.ia.proyecto.compartido.Disco;

import javax.swing.JOptionPane;
import java.awt.Component;
import java.util.Optional;

/**
 * Valida los datos del formulario y los convierte a un Disco listo para enviar.
 */
final class FormularioDiscoValidator {

    /**
     * Evita instanciar la clase porque toda la validacion es estatica.
     */
    private FormularioDiscoValidator() {
    }

    /**
     * Revisa campos obligatorios y rango del anio antes de construir el modelo compartido.
     *
     * @param parent componente padre para dialogos de validacion.
     * @param data datos capturados por la vista.
     * @return Optional con Disco valido o vacio cuando hay errores.
     */
    static Optional<Disco> validar(Component parent, FormularioDiscoData data) {
        if (data.titulo.isEmpty() || data.artista.isEmpty()
                || data.anio.isEmpty() || data.genero.isEmpty()) {
            mostrar(parent, "Todos los campos son obligatorios.");
            return Optional.empty();
        }

        try {
            // El anio viaja como texto desde Swing y solo aqui se convierte al tipo del modelo.
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

    /**
     * Muestra una advertencia de validacion asociada al formulario.
     *
     * @param parent componente padre del dialogo.
     * @param mensaje texto que explica el problema encontrado.
     */
    private static void mostrar(Component parent, String mensaje) {
        JOptionPane.showMessageDialog(parent, mensaje, "Validacion", JOptionPane.WARNING_MESSAGE);
    }
}
