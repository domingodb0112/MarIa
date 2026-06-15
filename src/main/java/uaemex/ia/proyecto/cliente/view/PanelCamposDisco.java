package uaemex.ia.proyecto.cliente.view;

import javax.swing.*;
import java.awt.*;

/**
 * Panel que contiene los campos necesarios para capturar un disco fisico.
 */
class PanelCamposDisco extends JPanel {

    private final JTextField campoTitulo = new JTextField();
    private final JTextField campoArtista = new JTextField();
    private final JTextField campoAnio = new JTextField();
    private final JTextField campoGenero = new JTextField();
    private final JRadioButton rbVinilo = new JRadioButton("Vinilo", true);
    private final JRadioButton rbCD = new JRadioButton("CD");

    /**
     * Construye el formulario visual con campos de texto y seleccion de formato.
     */
    PanelCamposDisco() {
        setLayout(new GridBagLayout());
        setBackground(UIStyles.COLOR_PANEL);
        setBorder(UIStyles.crearBordeTitulo("Registrar Disco"));
        agregarCamposTexto();
        agregarFormato();
        agregarEspaciador();
    }

    /**
     * Lee y limpia los valores actuales del formulario.
     *
     * @return datos capturados antes de la validacion.
     */
    FormularioDiscoData obtenerData() {
        return new FormularioDiscoData(
                campoTitulo.getText().trim(),
                campoArtista.getText().trim(),
                campoAnio.getText().trim(),
                campoGenero.getText().trim(),
                rbVinilo.isSelected() ? "Vinilo" : "CD");
    }

    /**
     * Vacia el formulario y regresa el foco al primer campo.
     */
    void limpiar() {
        campoTitulo.setText("");
        campoArtista.setText("");
        campoAnio.setText("");
        campoGenero.setText("");
        rbVinilo.setSelected(true);
        campoTitulo.requestFocus();
    }

    /**
     * Agrega los campos de titulo, artista, anio y genero usando GridBagLayout.
     */
    private void agregarCamposTexto() {
        String[] etiquetas = {"Titulo:", "Artista:", "Anio:", "Genero:"};
        JTextField[] campos = {campoTitulo, campoArtista, campoAnio, campoGenero};
        GridBagConstraints gc = restriccionesBase();
        for (int i = 0; i < etiquetas.length; i++) {
            // Se reutiliza el mismo objeto de restricciones cambiando columna/fila por control.
            gc.gridx = 0; gc.gridy = i; gc.weightx = 0;
            add(UIStyles.crearEtiqueta(etiquetas[i]), gc);
            gc.gridx = 1; gc.weightx = 1.0;
            UIStyles.estilizarCampo(campos[i]);
            add(campos[i], gc);
        }
    }

    /**
     * Agrega los radio buttons para elegir si el disco es vinilo o CD.
     */
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

    /**
     * Coloca un relleno flexible para que el panel conserve una altura estable.
     */
    private void agregarEspaciador() {
        GridBagConstraints gc = restriccionesBase();
        gc.gridx = 0; gc.gridy = 5; gc.gridwidth = 2;
        gc.weighty = 1.0; gc.fill = GridBagConstraints.BOTH;
        add(Box.createGlue(), gc);
    }

    /**
     * Crea las restricciones comunes para alinear los campos del formulario.
     *
     * @return configuracion base de GridBagConstraints.
     */
    private GridBagConstraints restriccionesBase() {
        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(6, 8, 6, 8);
        gc.anchor = GridBagConstraints.WEST;
        gc.fill = GridBagConstraints.HORIZONTAL;
        return gc;
    }
}
