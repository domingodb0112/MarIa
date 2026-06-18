package uaemex.ia.proyecto.cliente.view;

import uaemex.ia.proyecto.compartido.Disco;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.function.Consumer;

/**
 * Componente visual modular que encapsula el formulario de búsqueda de álbumes.
 * Cumple con la regla de diseño modular para mantener los archivos bajo 150 líneas.
 */
class PanelBusqueda extends JPanel {
    private final JTextField campoBusqueda = new JTextField();
    private final JButton btnBuscar = new JButton("Buscar");
    private final JComboBox<Disco> comboResultados = new JComboBox<>();
    private final JButton btnAgregar = new JButton("Agregar a coleccion");
    private final Consumer<String> onBuscar;
    private final Consumer<Disco> onAgregar;

    PanelBusqueda(Consumer<String> onBuscar, Consumer<Disco> onAgregar) {
        this.onBuscar = onBuscar;
        this.onAgregar = onAgregar;
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
        actualizarResultados(null);
    }

    /**
     * Carga los resultados de busqueda para que el usuario elija uno.
     *
     * @param resultados discos devueltos por el servidor.
     */
    void actualizarResultados(List<Disco> resultados) {
        DefaultComboBoxModel<Disco> modelo = new DefaultComboBoxModel<>();
        if (resultados != null) resultados.forEach(modelo::addElement);
        comboResultados.setModel(modelo);
        actualizarEstadoResultados(btnBuscar.isEnabled());
    }

    /**
     * Controla el estado de habilitación del botón de búsqueda.
     *
     * @param enabled true para activar el botón.
     */
    void setBotonera(boolean enabled) {
        btnBuscar.setEnabled(enabled);
        actualizarEstadoResultados(enabled);
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

        gc.gridy = 2;
        gc.gridwidth = 1;
        add(UIStyles.crearEtiqueta("Resultados:"), gc);
        comboResultados.setFont(UIStyles.FUENTE_BASE);
        gc.gridx = 1;
        add(comboResultados, gc);

        UIStyles.estilizarBotonSecundario(btnAgregar);
        btnAgregar.addActionListener(e -> agregarSeleccionado());
        gc.gridx = 0;
        gc.gridy = 3;
        gc.gridwidth = 2;
        add(btnAgregar, gc);
        actualizarEstadoResultados(false);
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
        actualizarResultados(null);
        onBuscar.accept(consulta);
    }

    // Habilita la seleccion solo cuando hay resultados y conexion activa.
    private void actualizarEstadoResultados(boolean enabled) {
        boolean tieneResultados = comboResultados.getItemCount() > 0;
        comboResultados.setEnabled(enabled && tieneResultados);
        btnAgregar.setEnabled(enabled && tieneResultados);
    }

    // Registra en la coleccion el album seleccionado del ComboBox.
    private void agregarSeleccionado() {
        Disco seleccionado = (Disco) comboResultados.getSelectedItem();
        if (seleccionado == null) {
            JOptionPane.showMessageDialog(this,
                    "Selecciona un album encontrado para agregarlo.", "Validacion",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        onAgregar.accept(seleccionado);
    }
}
