package uaemex.ia.proyecto.cliente.view;

import uaemex.ia.proyecto.compartido.Disco;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.function.Consumer;

/**
 * Panel de consultas del cliente: busqueda textual y solicitud de recomendaciones.
 */
public class PanelConsultas extends JPanel {

    private JTextField campoBusqueda;
    private JButton btnBuscar;
    private JButton btnRecomendaciones;
    private JComboBox<Disco> comboRecomendaciones;
    private JButton btnAceptar;
    private JButton btnRechazar;

    private final Consumer<String> onBuscar;
    private final Runnable onRecomendar;
    private final Consumer<Disco> onAceptarRecomendacion;
    private final Consumer<Disco> onRechazarRecomendacion;

    /**
     * Conecta los controles de consulta con las acciones del presentador.
     *
     * @param onBuscar callback que recibe la consulta escrita.
     * @param onRecomendar callback para pedir recomendaciones al servidor.
     * @param onAceptarRecomendacion callback para reforzar una recomendacion aceptada.
     * @param onRechazarRecomendacion callback para penalizar una recomendacion rechazada.
     */
    public PanelConsultas(Consumer<String> onBuscar, Runnable onRecomendar,
                          Consumer<Disco> onAceptarRecomendacion,
                          Consumer<Disco> onRechazarRecomendacion) {
        this.onBuscar = onBuscar;
        this.onRecomendar = onRecomendar;
        this.onAceptarRecomendacion = onAceptarRecomendacion;
        this.onRechazarRecomendacion = onRechazarRecomendacion;

        setLayout(new GridLayout(2, 1, 0, 8));
        setOpaque(false);

        add(crearPanelBusqueda());
        add(crearPanelRecomendaciones());
    }

    /**
     * Construye el bloque visual para busqueda por texto.
     *
     * @return panel configurado con campo y boton de busqueda.
     */
    private JPanel crearPanelBusqueda() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(UIStyles.COLOR_PANEL);
        panel.setBorder(UIStyles.crearBordeTitulo("Buscar Album"));

        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(6, 8, 6, 8);
        gc.anchor = GridBagConstraints.WEST;
        gc.fill = GridBagConstraints.HORIZONTAL;

        gc.gridx = 0; gc.gridy = 0; gc.weightx = 0;
        panel.add(UIStyles.crearEtiqueta("Consulta:"), gc);

        campoBusqueda = new JTextField();
        UIStyles.estilizarCampo(campoBusqueda);
        campoBusqueda.addActionListener(e -> buscar());
        gc.gridx = 1; gc.weightx = 1.0;
        panel.add(campoBusqueda, gc);

        btnBuscar = new JButton("Buscar");
        UIStyles.estilizarBotonSecundario(btnBuscar);
        btnBuscar.addActionListener(e -> buscar());
        gc.gridx = 0; gc.gridy = 1; gc.gridwidth = 2;
        panel.add(btnBuscar, gc);

        return panel;
    }

    /**
     * Construye el bloque visual que dispara la recomendacion automatica.
     *
     * @return panel configurado con descripcion y boton.
     */
    private JPanel crearPanelRecomendaciones() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(UIStyles.COLOR_PANEL);
        panel.setBorder(UIStyles.crearBordeTitulo("Recomendaciones"));

        JLabel descripcion = new JLabel("Basadas en tus generos registrados");
        descripcion.setFont(UIStyles.FUENTE_BASE);
        descripcion.setForeground(UIStyles.COLOR_TEXTO);
        
        btnRecomendaciones = new JButton("Obtener Recomendaciones");
        UIStyles.estilizarBotonSecundario(btnRecomendaciones);
        btnRecomendaciones.addActionListener(e -> onRecomendar.run());

        comboRecomendaciones = new JComboBox<>();
        comboRecomendaciones.setEnabled(false);

        btnAceptar = new JButton("Aceptar");
        UIStyles.estilizarBotonSecundario(btnAceptar);
        btnAceptar.setEnabled(false);
        btnAceptar.addActionListener(e -> enviarFeedback(onAceptarRecomendacion));

        btnRechazar = new JButton("Rechazar");
        UIStyles.estilizarBotonSecundario(btnRechazar);
        btnRechazar.setEnabled(false);
        btnRechazar.addActionListener(e -> enviarFeedback(onRechazarRecomendacion));

        JPanel panelFeedback = new JPanel(new GridLayout(1, 2, 8, 0));
        panelFeedback.setOpaque(false);
        panelFeedback.add(btnAceptar);
        panelFeedback.add(btnRechazar);

        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(4, 8, 4, 8);
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.gridx = 0;
        gc.gridy = 0;
        gc.weightx = 1.0;
        panel.add(descripcion, gc);
        gc.gridy = 1;
        panel.add(btnRecomendaciones, gc);
        gc.gridy = 2;
        panel.add(comboRecomendaciones, gc);
        gc.gridy = 3;
        panel.add(panelFeedback, gc);
        return panel;
    }

    /**
     * Valida la consulta y la entrega al callback de busqueda.
     */
    private void buscar() {
        String consulta = campoBusqueda.getText().trim();
        if (consulta.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Escribe un titulo, artista o genero para buscar.", "Validacion", JOptionPane.WARNING_MESSAGE);
            return;
        }
        onBuscar.accept(consulta);
    }

    /**
     * Limpia el campo de busqueda despues de completar una consulta.
     */
    public void limpiarBusqueda() {
        campoBusqueda.setText("");
    }

    /**
     * Carga en el selector las recomendaciones recientes para recibir retroalimentacion.
     *
     * @param recomendaciones discos recomendados por el servidor.
     */
    public void actualizarRecomendaciones(List<Disco> recomendaciones) {
        DefaultComboBoxModel<Disco> modelo = new DefaultComboBoxModel<>();
        if (recomendaciones != null) {
            for (Disco disco : recomendaciones) {
                modelo.addElement(disco);
            }
        }
        comboRecomendaciones.setModel(modelo);
        boolean tieneOpciones = modelo.getSize() > 0;
        comboRecomendaciones.setEnabled(tieneOpciones && btnRecomendaciones.isEnabled());
        btnAceptar.setEnabled(tieneOpciones && btnRecomendaciones.isEnabled());
        btnRechazar.setEnabled(tieneOpciones && btnRecomendaciones.isEnabled());
    }

    /**
     * Habilita o deshabilita los botones segun el estado de conexion.
     *
     * @param enabled true para permitir consultas, false para bloquearlas.
     */
    public void setBotonera(boolean enabled) {
        btnBuscar.setEnabled(enabled);
        btnRecomendaciones.setEnabled(enabled);
        boolean tieneSeleccion = comboRecomendaciones.getItemCount() > 0;
        comboRecomendaciones.setEnabled(enabled && tieneSeleccion);
        btnAceptar.setEnabled(enabled && tieneSeleccion);
        btnRechazar.setEnabled(enabled && tieneSeleccion);
    }

    /**
     * Envia la recomendacion seleccionada al callback de aprendizaje.
     *
     * @param callback accion que registra aceptacion o rechazo.
     */
    private void enviarFeedback(Consumer<Disco> callback) {
        Disco seleccionado = (Disco) comboRecomendaciones.getSelectedItem();
        if (seleccionado == null) {
            JOptionPane.showMessageDialog(this,
                    "Selecciona una recomendacion para evaluarla.", "Validacion", JOptionPane.WARNING_MESSAGE);
            return;
        }
        callback.accept(seleccionado);
    }
}
