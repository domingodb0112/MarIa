package uaemex.ia.proyecto.cliente.view;

import javax.swing.*;
import java.awt.*;
import java.util.function.Consumer;

/**
 * Componente visual modular que encapsula el formulario de búsqueda de álbumes.
 * Cumple con la regla de diseño modular para mantener los archivos bajo 150 líneas.
 */
class PanelBusqueda extends JPanel {
    private final JTextField campoBusqueda = new JTextField();
    private final JButton btnBuscar = new JButton("Buscar");
    private final Consumer<String> onBuscar;

    PanelBusqueda(Consumer<String> onBuscar) {
        this.onBuscar = onBuscar;
        setLayout(new GridBagLayout());
        setBackground(UIStyles.COLOR_PANEL);
        setBorder(UIStyles.crearBordeTitulo("Buscar Album"));
        construir();
    }

    /**
     * Limpia el campo de texto de búsqueda.
     */
    void limpiar() {
        campoBusqueda.setText("");
    }

    /**
     * Controla el estado de habilitación del botón de búsqueda.
     *
     * @param enabled true para activar el botón.
     */
    void setBotonera(boolean enabled) {
        btnBuscar.setEnabled(enabled);
    }

    // Ensambla el layout GridBagLayout para el campo y etiqueta
    private void construir() {
        GridBagConstraints gc = baseConstraints();
        add(UIStyles.crearEtiqueta("Consulta:"), gc);
        UIStyles.estilizarCampo(campoBusqueda);
        campoBusqueda.addActionListener(e -> buscar());
        gc.gridx = 1;
        gc.weightx = 1.0;
        add(campoBusqueda, gc);
        
        UIStyles.estilizarBotonSecundario(btnBuscar);
        btnBuscar.addActionListener(e -> buscar());
        gc.gridx = 0;
        gc.gridy = 1;
        gc.gridwidth = 2;
        add(btnBuscar, gc);
    }

    // Inicializa la configuración base del GridBagConstraints
    private GridBagConstraints baseConstraints() {
        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(6, 8, 6, 8);
        gc.anchor = GridBagConstraints.WEST;
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.gridx = 0;
        gc.gridy = 0;
        return gc;
    }

    // Valida y despacha la búsqueda hacia el callback del presentador
    private void buscar() {
        String consulta = campoBusqueda.getText().trim();
        if (consulta.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Escribe un titulo, artista o genero para buscar.", "Validacion",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        onBuscar.accept(consulta);
    }
}
