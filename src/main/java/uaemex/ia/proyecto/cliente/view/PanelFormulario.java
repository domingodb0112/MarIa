package uaemex.ia.proyecto.cliente.view;

import uaemex.ia.proyecto.compartido.Disco;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.Optional;
import java.util.function.Consumer;

public class PanelFormulario extends JPanel {

    private final PanelCamposDisco panelCampos = new PanelCamposDisco();
    private JButton btnRegistrar;
    private JButton btnListar;
    private JButton btnReconectar;

    private final Consumer<Disco> onRegistrar;
    private final Runnable onListar;
    private final Runnable onReconectar;

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

    private JPanel crearBotones() {
        JPanel botones = new JPanel(new GridLayout(3, 1, 0, 6));
        botones.setOpaque(false);
        botones.setBorder(new EmptyBorder(0, 0, 4, 0));

        btnRegistrar = new JButton("Registrar Disco");
        btnListar = new JButton("Listar Coleccion");
        btnReconectar = new JButton("Reconectar al Servidor");

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

    private void registrarDisco() {
        Optional<Disco> disco = FormularioDiscoValidator.validar(this, panelCampos.obtenerData());
        disco.ifPresent(onRegistrar);
    }

    public void limpiarFormulario() {
        panelCampos.limpiar();
    }

    public void setBotonera(boolean enabled) {
        btnRegistrar.setEnabled(enabled);
        btnListar.setEnabled(enabled);
    }

    public void setReconectarEnabled(boolean enabled) {
        btnReconectar.setEnabled(enabled);
    }
}
