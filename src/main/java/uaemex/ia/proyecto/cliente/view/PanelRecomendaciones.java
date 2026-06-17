package uaemex.ia.proyecto.cliente.view;

import uaemex.ia.proyecto.compartido.Disco;
import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.function.Consumer;

/**
 * Componente visual modular que encapsula la sección de visualización y
 * retroalimentación de recomendaciones del motor de Inteligencia Artificial.
 */
class PanelRecomendaciones extends JPanel {
    private final JButton btnRecomendaciones = new JButton("Obtener Recomendaciones");
    private final JComboBox<Disco> comboRecomendaciones = new JComboBox<>();
    private final JButton btnAceptar = new JButton("Aceptar");
    private final JButton btnRechazar = new JButton("Rechazar");
    private final Runnable onRecomendar;
    private final Consumer<Disco> onAceptar;
    private final Consumer<Disco> onRechazar;

    PanelRecomendaciones(Runnable onRecomendar, Consumer<Disco> onAceptar, Consumer<Disco> onRechazar) {
        this.onRecomendar = onRecomendar;
        this.onAceptar = onAceptar;
        this.onRechazar = onRechazar;
        setLayout(new GridBagLayout());
        setBackground(UIStyles.COLOR_PANEL);
        setBorder(UIStyles.crearBordeTitulo("Recomendaciones"));
        construir();
    }

    /**
     * Carga las recomendaciones en el ComboBox y actualiza la habilitación de botones.
     *
     * @param recomendaciones lista de discos recomendados por el servidor.
     */
    void actualizarRecomendaciones(List<Disco> recomendaciones) {
        DefaultComboBoxModel<Disco> modelo = new DefaultComboBoxModel<>();
        if (recomendaciones != null) recomendaciones.forEach(modelo::addElement);
        comboRecomendaciones.setModel(modelo);
        actualizarEstadoFeedback(btnRecomendaciones.isEnabled());
    }

    /**
     * Habilita o deshabilita los controles del panel según el estado de red.
     *
     * @param enabled true para activar acciones de usuario.
     */
    void setBotonera(boolean enabled) {
        btnRecomendaciones.setEnabled(enabled);
        actualizarEstadoFeedback(enabled);
    }

    // Ubica los componentes en una cuadrícula vertical GridBagLayout
    private void construir() {
        JLabel descripcion = UIStyles.crearEtiqueta("Basadas en tus generos registrados");
        prepararBotones();
        GridBagConstraints gc = constraints();
        add(descripcion, gc);
        gc.gridy = 1;
        add(btnRecomendaciones, gc);
        gc.gridy = 2;
        add(comboRecomendaciones, gc);
        gc.gridy = 3;
        add(panelFeedback(), gc);
    }

    private void prepararBotones() {
        UIStyles.estilizarBotonSecundario(btnRecomendaciones);
        UIStyles.estilizarBotonSecundario(btnAceptar);
        UIStyles.estilizarBotonSecundario(btnRechazar);
        btnRecomendaciones.addActionListener(e -> onRecomendar.run());
        btnAceptar.addActionListener(e -> enviarFeedback(onAceptar));
        btnRechazar.addActionListener(e -> enviarFeedback(onRechazar));
        actualizarEstadoFeedback(false);
    }

    private JPanel panelFeedback() {
        JPanel panel = new JPanel(new GridLayout(1, 2, 8, 0));
        panel.setOpaque(false);
        panel.add(btnAceptar);
        panel.add(btnRechazar);
        return panel;
    }

    private GridBagConstraints constraints() {
        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(4, 8, 4, 8);
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.gridx = 0;
        gc.weightx = 1.0;
        return gc;
    }

    // Evalúa si habilitar el combobox y botones según si hay ítems recomendados
    private void actualizarEstadoFeedback(boolean enabled) {
        boolean tiene = comboRecomendaciones.getItemCount() > 0;
        comboRecomendaciones.setEnabled(enabled && tiene);
        btnAceptar.setEnabled(enabled && tiene);
        btnRechazar.setEnabled(enabled && tiene);
    }

    // Despacha el callback con la selección de la recomendación actual
    private void enviarFeedback(Consumer<Disco> callback) {
        Disco seleccionado = (Disco) comboRecomendaciones.getSelectedItem();
        if (seleccionado == null) {
            JOptionPane.showMessageDialog(this,
                    "Selecciona una recomendacion para evaluarla.", "Validacion",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        callback.accept(seleccionado);
    }
}
