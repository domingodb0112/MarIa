package uaemex.ia.proyecto.cliente.view;

import javax.swing.*;
import java.awt.*;
import java.util.function.Consumer;

/**
 * Panel de consultas del cliente: busqueda textual y solicitud de recomendaciones.
 */
public class PanelConsultas extends JPanel {

    private JTextField campoBusqueda;
    private JButton btnBuscar;
    private JButton btnRecomendaciones;

    private final Consumer<String> onBuscar;
    private final Runnable onRecomendar;

    /**
     * Conecta los controles de consulta con las acciones del presentador.
     *
     * @param onBuscar callback que recibe la consulta escrita.
     * @param onRecomendar callback para pedir recomendaciones al servidor.
     */
    public PanelConsultas(Consumer<String> onBuscar, Runnable onRecomendar) {
        this.onBuscar = onBuscar;
        this.onRecomendar = onRecomendar;

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
        JPanel panel = new JPanel(new BorderLayout(0, 6));
        panel.setBackground(UIStyles.COLOR_PANEL);
        panel.setBorder(UIStyles.crearBordeTitulo("Recomendaciones"));

        JLabel descripcion = new JLabel("Basadas en tus generos registrados");
        descripcion.setFont(UIStyles.FUENTE_BASE);
        descripcion.setForeground(UIStyles.COLOR_TEXTO);
        
        btnRecomendaciones = new JButton("Obtener Recomendaciones");
        UIStyles.estilizarBotonSecundario(btnRecomendaciones);
        btnRecomendaciones.addActionListener(e -> onRecomendar.run());

        panel.add(descripcion, BorderLayout.CENTER);
        panel.add(btnRecomendaciones, BorderLayout.SOUTH);
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
     * Habilita o deshabilita los botones segun el estado de conexion.
     *
     * @param enabled true para permitir consultas, false para bloquearlas.
     */
    public void setBotonera(boolean enabled) {
        btnBuscar.setEnabled(enabled);
        btnRecomendaciones.setEnabled(enabled);
    }
}
