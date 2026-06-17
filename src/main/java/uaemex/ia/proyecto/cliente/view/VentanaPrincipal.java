package uaemex.ia.proyecto.cliente.view;

import uaemex.ia.proyecto.compartido.Disco;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class VentanaPrincipal extends JFrame {
    private static final int ANCHO = 860, ALTO = 520;
    private final ClientPresenter presenter;
    private PanelFormulario panelFormulario;
    private PanelConsultas panelConsultas;
    private PanelEstado panelEstado;
    private PanelEstadisticasPerfil panelEstadisticas;
    private JTextArea areaLog;

    public VentanaPrincipal(String host, int puerto) {
        super("MarIA - Sistema de Recomendacion Musical");
        UIStyles.setupLookAndFeel();
        this.presenter = new ClientPresenter(this, host, puerto);
        initUI();
        presenter.conectar();
    }

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
        panelConsultas = new PanelConsultas(presenter::buscarAlbum, presenter::obtenerRecomendaciones,
                presenter::aceptarRecomendacion, presenter::rechazarRecomendacion);
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

    public void actualizarRecomendaciones(java.util.List<Disco> recomendaciones) {
        panelConsultas.actualizarRecomendaciones(recomendaciones);
        panelEstadisticas.actualizarRecomendaciones(recomendaciones);
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

    public void marcarDesconectado() {
        panelEstado.marcarDesconectado();
        setBotonera(false);
        presenter.desconectar();
    }

    public void mostrarDialogoRed(String mensaje) {
        JOptionPane.showMessageDialog(this, mensaje, "Conexion con MarIA", JOptionPane.WARNING_MESSAGE);
    }
}
