package uaemex.ia.proyecto.cliente.view;

import javax.swing.*;
import java.awt.*;
import java.util.function.Consumer;

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

    void limpiar() {
        campoBusqueda.setText("");
    }

    void setBotonera(boolean enabled) {
        btnBuscar.setEnabled(enabled);
    }

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

    private GridBagConstraints baseConstraints() {
        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(6, 8, 6, 8);
        gc.anchor = GridBagConstraints.WEST;
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.gridx = 0;
        gc.gridy = 0;
        return gc;
    }

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
