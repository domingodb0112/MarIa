package uaemex.ia.proyecto.cliente.view;

import uaemex.ia.proyecto.compartido.Disco;
import uaemex.ia.proyecto.compartido.TlsConfig;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * Ventana principal y contenedor raíz de la interfaz visual del cliente.
 * Administra los subpaneles modulares (Estadísticas, Formularios, Consultas).
 */
public class VentanaPrincipal extends JFrame {
    private static final int ANCHO = 860, ALTO = 520;
    private final ClientPresenter presenter;
    private PanelFormulario panelFormulario;
    private PanelConsultas panelConsultas;
    private PanelEstado panelEstado;
    private PanelEstadisticasPerfil panelEstadisticas;
    private JTextArea areaLog;

    public VentanaPrincipal(String host, int puerto) {
        this(host, puerto, null);
    }

    public VentanaPrincipal(String host, int puerto, TlsConfig tlsConfig) {
        super("MarIA - Sistema de Recomendacion Musical");
        UIStyles.setupLookAndFeel();
        this.presenter = new ClientPresenter(this, host, puerto, tlsConfig);
        initUI();
        presenter.conectar(); // Intenta conectar al arrancar
    }

    // Inicializa la configuración de la ventana y coloca subpaneles
    private void initUI() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(ANCHO, ALTO);
        setMinimumSize(new Dimension(720, 420));
        setLocationRelativeTo(null);

        JPanel raiz = new JPanel(new BorderLayout(14, 12));
        raiz.setBackground(UIStyles.COLOR_FONDO);
        raiz.setBorder(new EmptyBorder(14, 16, 12, 16));
        setContentPane(raiz);

        // Añade título arriba, formulario a la izquierda, área de logs al centro, stats a la derecha y barra de estado abajo
        raiz.add(crearPanelTitulo(), BorderLayout.NORTH);
        panelConsultas = new PanelConsultas(presenter::buscarAlbum, presenter::registrarDisco,
                presenter::obtenerRecomendaciones, presenter::aceptarRecomendacion,
                presenter::rechazarRecomendacion);
        panelFormulario = new PanelFormulario(presenter::registrarDisco, presenter::listarColeccion, presenter::conectar, panelConsultas);
        raiz.add(panelFormulario, BorderLayout.WEST);
        raiz.add(crearPanelLog(), BorderLayout.CENTER);
        panelEstadisticas = new PanelEstadisticasPerfil();
        raiz.add(panelEstadisticas, BorderLayout.EAST);
        panelEstado = new PanelEstado();
        raiz.add(panelEstado, BorderLayout.SOUTH);
    }

    private JPanel crearPanelTitulo() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panel.setOpaque(false);
        JLabel label = new JLabel("MarIA");
        label.setFont(UIStyles.FUENTE_TITULO);
        label.setForeground(UIStyles.COLOR_PRIMARIO);
        panel.add(label);
        return panel;
    }

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
        scroll.setBorder(UIStyles.crearBordeTitulo("Logs de MarIA"));
        return scroll;
    }

    // Escribe logs del sistema
    public void log(String linea) {
        areaLog.append(linea + "\n");
        areaLog.setCaretPosition(areaLog.getDocument().getLength());
    }

    public void setBotonera(boolean habilitada) {
        panelFormulario.setBotonera(habilitada);
        panelConsultas.setBotonera(habilitada);
    }

    public void setReconectarEnabled(boolean habilitada) {
        panelFormulario.setReconectarEnabled(habilitada);
    }

    public void limpiarFormulario() {
        panelFormulario.limpiarFormulario();
    }

    // Actualiza recomendaciones en el panel de feedback y el dashboard lateral
    public void actualizarRecomendaciones(java.util.List<Disco> recomendaciones) {
        panelConsultas.actualizarRecomendaciones(recomendaciones);
        panelEstadisticas.actualizarRecomendaciones(recomendaciones);
    }

    public void actualizarResultadosBusqueda(java.util.List<Disco> resultados) {
        panelConsultas.actualizarResultadosBusqueda(resultados);
    }

    public void actualizarEstadisticasColeccion(java.util.List<Disco> discos) {
        panelEstadisticas.actualizarColeccion(discos);
    }

    public void agregarDiscoEstadisticas(Disco disco) {
        panelEstadisticas.agregarDisco(disco);
    }

    public void marcarConectado(String host, int puerto) {
        panelEstado.marcarConectado(host, puerto);
    }

    // Deshabilita botoneras al perder la conexión
    public void marcarDesconectado() {
        panelEstado.marcarDesconectado();
        setBotonera(false);
        presenter.desconectar();
    }

    public void mostrarDialogoRed(String mensaje) {
        JOptionPane.showMessageDialog(this, mensaje, "Conexion con MarIA", JOptionPane.WARNING_MESSAGE);
    }
}
