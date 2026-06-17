package uaemex.ia.proyecto.cliente.view;

import uaemex.ia.proyecto.compartido.Disco;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * Panel lateral que agrupa captura de discos, consultas y botones principales.
 */
public class PanelFormulario extends JPanel {

    private final PanelCamposDisco panelCampos = new PanelCamposDisco();
    private JButton btnRegistrar;
    private JButton btnListar;
    private JButton btnReconectar;

    private final Consumer<Disco> onRegistrar;
    private final Runnable onListar;
    private final Runnable onReconectar;

    /**
     * Construye el panel y enlaza las acciones de usuario con el presentador.
     *
     * @param onRegistrar callback para registrar un disco valido.
     * @param onListar callback para listar la coleccion.
     * @param onReconectar callback para intentar una nueva conexion.
     * @param panelConsultas panel de busqueda y recomendaciones reutilizado.
     */
    public PanelFormulario(Consumer<Disco> onRegistrar, Runnable onListar, Runnable onReconectar, PanelConsultas panelConsultas) {
        this.onRegistrar = onRegistrar;
        this.onListar = onListar;
        this.onReconectar = onReconectar;

        setLayout(new BorderLayout(0, 10));
        setPreferredSize(new Dimension(320, 0));
        setOpaque(false);

        JPanel contenido = new JPanel(new BorderLayout(0, 8));
        contenido.setOpaque(false);
        contenido.add(panelCampos, BorderLayout.CENTER);
        contenido.add(panelConsultas, BorderLayout.SOUTH);

        add(contenido, BorderLayout.CENTER);
        add(crearBotones(), BorderLayout.SOUTH);
    }

    /**
     * Crea los botones inferiores de acciones generales.
     *
     * @return panel con botones ya estilizados y enlazados.
     */
    private JPanel crearBotones() {
        JPanel botones = new JPanel(new GridLayout(3, 1, 0, 6));
        botones.setOpaque(false);
        botones.setBorder(new EmptyBorder(0, 0, 4, 0));

        btnRegistrar = new JButton("Registrar Disco");
        btnListar = new JButton("Listar Coleccion");
        btnReconectar = new JButton("Reconectar a MarIA");

        UIStyles.estilizarBotonPrimario(btnRegistrar);
        UIStyles.estilizarBotonSecundario(btnListar);
        UIStyles.estilizarBotonSecundario(btnReconectar);

        btnRegistrar.addActionListener(e -> registrarDisco());
        btnListar.addActionListener(e -> onListar.run());
        btnReconectar.addActionListener(e -> onReconectar.run());

        botones.add(btnRegistrar);
        botones.add(btnListar);
        botones.add(btnReconectar);

        return botones;
    }

    /**
     * Valida el formulario y, si es correcto, envia el disco al callback.
     */
    private void registrarDisco() {
        Optional<Disco> disco = FormularioDiscoValidator.validar(this, panelCampos.obtenerData());
        disco.ifPresent(onRegistrar);
    }

    /**
     * Borra los campos despues de un registro exitoso.
     */
    public void limpiarFormulario() {
        panelCampos.limpiar();
    }

    /**
     * Activa o bloquea las acciones que requieren conexion con el servidor.
     *
     * @param enabled true para permitir registro/listado.
     */
    public void setBotonera(boolean enabled) {
        btnRegistrar.setEnabled(enabled);
        btnListar.setEnabled(enabled);
    }

    /**
     * Controla el boton de reconexion para evitar multiples intentos simultaneos.
     *
     * @param enabled true para permitir reconectar.
     */
    public void setReconectarEnabled(boolean enabled) {
        btnReconectar.setEnabled(enabled);
    }
}
