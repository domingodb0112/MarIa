package uaemex.ia.proyecto.cliente.view;

import uaemex.ia.proyecto.compartido.Disco;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * Ventana principal del cliente Swing.
 * Contiene los paneles de captura, consulta, log de respuestas y estado de conexion.
 */
public class VentanaPrincipal extends JFrame {

    private static final int ANCHO = 860;
    private static final int ALTO  = 520;

    private final ClientPresenter presenter;
    private PanelFormulario panelFormulario;
    private PanelConsultas panelConsultas;
    private PanelEstado panelEstado;
    private JTextArea areaLog;

    /**
     * Crea la ventana y dispara la primera conexion al servidor.
     *
     * @param host host del servidor configurado.
     * @param puerto puerto TCP del servidor configurado.
     */
    public VentanaPrincipal(String host, int puerto) {
        super("Sistema de Recomendacion de Musica Fisica — UAEMEX IA");
        UIStyles.setupLookAndFeel();
        this.presenter = new ClientPresenter(this, host, puerto);
        initUI();
        presenter.conectar();
    }

    /**
     * Ensambla la distribucion principal de la interfaz.
     */
    private void initUI() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(ANCHO, ALTO);
        setMinimumSize(new Dimension(720, 420));
        setLocationRelativeTo(null);

        JPanel raiz = new JPanel(new BorderLayout(14, 12));
        raiz.setBackground(UIStyles.COLOR_FONDO);
        raiz.setBorder(new EmptyBorder(14, 16, 12, 16));
        setContentPane(raiz);

        raiz.add(crearPanelTitulo(), BorderLayout.NORTH);

        panelConsultas = new PanelConsultas(presenter::buscarAlbum, presenter::obtenerRecomendaciones);
        panelFormulario = new PanelFormulario(presenter::registrarDisco, presenter::listarColeccion, presenter::conectar, panelConsultas);
        raiz.add(panelFormulario, BorderLayout.WEST);

        raiz.add(crearPanelLog(), BorderLayout.CENTER);

        panelEstado = new PanelEstado();
        raiz.add(panelEstado, BorderLayout.SOUTH);
    }

    /**
     * Construye el encabezado con el nombre del sistema.
     *
     * @return panel superior de titulo.
     */
    private JPanel crearPanelTitulo() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panel.setOpaque(false);
        JLabel label = new JLabel("Coleccion de Musica Fisica");
        label.setFont(UIStyles.FUENTE_TITULO);
        label.setForeground(UIStyles.COLOR_PRIMARIO);
        panel.add(label);
        return panel;
    }

    /**
     * Crea el area central donde se muestran respuestas y errores del servidor.
     *
     * @return scroll que contiene el area de texto.
     */
    private JScrollPane crearPanelLog() {
        areaLog = new JTextArea();
        areaLog.setEditable(false);
        areaLog.setFont(new Font("Monospaced", Font.PLAIN, 13));
        areaLog.setBackground(new Color(250, 252, 255));
        areaLog.setForeground(UIStyles.COLOR_TEXTO);
        areaLog.setBorder(new EmptyBorder(10, 10, 10, 10));
        areaLog.setLineWrap(true);
        areaLog.setWrapStyleWord(true);
        JScrollPane scroll = new JScrollPane(areaLog);
        scroll.getViewport().setBackground(areaLog.getBackground());
        scroll.setBorder(UIStyles.crearBordeTitulo("Respuestas del Servidor"));
        return scroll;
    }

    /**
     * Agrega una linea al registro visible y desplaza el cursor al final.
     *
     * @param linea texto a mostrar.
     */
    public void log(String linea) {
        areaLog.append(linea + "\n");
        areaLog.setCaretPosition(areaLog.getDocument().getLength());
    }

    /**
     * Habilita o bloquea controles que dependen de la conexion.
     *
     * @param habilitada true para activar acciones de usuario.
     */
    public void setBotonera(boolean habilitada) {
        panelFormulario.setBotonera(habilitada);
        panelConsultas.setBotonera(habilitada);
    }

    /**
     * Controla especificamente el boton de reconexion.
     *
     * @param habilitada true para permitir un nuevo intento.
     */
    public void setReconectarEnabled(boolean habilitada) {
        panelFormulario.setReconectarEnabled(habilitada);
    }

    /**
     * Limpia el formulario de registro desde el presentador.
     */
    public void limpiarFormulario() {
        panelFormulario.limpiarFormulario();
    }

    /**
     * Refleja en la barra de estado que el servidor esta disponible.
     *
     * @param host host conectado.
     * @param puerto puerto conectado.
     */
    public void marcarConectado(String host, int puerto) {
        panelEstado.marcarConectado(host, puerto);
    }

    /**
     * Refleja la perdida de conexion y desactiva acciones dependientes del servidor.
     */
    public void marcarDesconectado() {
        panelEstado.marcarDesconectado();
        setBotonera(false);
        presenter.desconectar();
    }

    /**
     * Muestra un dialogo de advertencia para problemas de comunicacion.
     *
     * @param mensaje detalle que se mostrara al usuario.
     */
    public void mostrarDialogoRed(String mensaje) {
        JOptionPane.showMessageDialog(this, mensaje, "Conexion con el servidor",
                JOptionPane.WARNING_MESSAGE);
    }
}
