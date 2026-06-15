package uaemex.ia.proyecto.cliente.view;

import javax.swing.*;
import java.awt.*;

class PanelCamposDisco extends JPanel {

    private final JTextField campoTitulo = new JTextField();
    private final JTextField campoArtista = new JTextField();
    private final JTextField campoAnio = new JTextField();
    private final JTextField campoGenero = new JTextField();
    private final JRadioButton rbVinilo = new JRadioButton("Vinilo", true);
    private final JRadioButton rbCD = new JRadioButton("CD");

    PanelCamposDisco() {
        setLayout(new GridBagLayout());
        setBackground(UIStyles.COLOR_PANEL);
        setBorder(UIStyles.crearBordeTitulo("Registrar Disco"));
        agregarCamposTexto();
        agregarFormato();
        agregarEspaciador();
    }

    FormularioDiscoData obtenerData() {
        return new FormularioDiscoData(
                campoTitulo.getText().trim(),
                campoArtista.getText().trim(),
                campoAnio.getText().trim(),
                campoGenero.getText().trim(),
                rbVinilo.isSelected() ? "Vinilo" : "CD");
    }

    void limpiar() {
        campoTitulo.setText("");
        campoArtista.setText("");
        campoAnio.setText("");
        campoGenero.setText("");
        rbVinilo.setSelected(true);
        campoTitulo.requestFocus();
    }

    private void agregarCamposTexto() {
        String[] etiquetas = {"Titulo:", "Artista:", "Anio:", "Genero:"};
        JTextField[] campos = {campoTitulo, campoArtista, campoAnio, campoGenero};
        GridBagConstraints gc = restriccionesBase();
        for (int i = 0; i < etiquetas.length; i++) {
            gc.gridx = 0; gc.gridy = i; gc.weightx = 0;
            add(UIStyles.crearEtiqueta(etiquetas[i]), gc);
            gc.gridx = 1; gc.weightx = 1.0;
            UIStyles.estilizarCampo(campos[i]);
            add(campos[i], gc);
        }
    }

    private void agregarFormato() {
        GridBagConstraints gc = restriccionesBase();
        gc.gridx = 0; gc.gridy = 4; gc.weightx = 0;
        add(UIStyles.crearEtiqueta("Formato:"), gc);
        gc.gridx = 1; gc.weightx = 1.0;
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        panel.setOpaque(false);
        UIStyles.estilizarRadio(rbVinilo);
        UIStyles.estilizarRadio(rbCD);
        ButtonGroup grupo = new ButtonGroup();
        grupo.add(rbVinilo);
        grupo.add(rbCD);
        panel.add(rbVinilo);
        panel.add(rbCD);
        add(panel, gc);
    }

    private void agregarEspaciador() {
        GridBagConstraints gc = restriccionesBase();
        gc.gridx = 0; gc.gridy = 5; gc.gridwidth = 2;
        gc.weighty = 1.0; gc.fill = GridBagConstraints.BOTH;
        add(Box.createGlue(), gc);
    }

    private GridBagConstraints restriccionesBase() {
        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(6, 8, 6, 8);
        gc.anchor = GridBagConstraints.WEST;
        gc.fill = GridBagConstraints.HORIZONTAL;
        return gc;
    }
}
