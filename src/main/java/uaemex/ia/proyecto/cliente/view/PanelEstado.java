package uaemex.ia.proyecto.cliente.view;

import javax.swing.*;
import java.awt.*;

public class PanelEstado extends JPanel {

    private final JLabel lblEstado;

    public PanelEstado() {
        setLayout(new FlowLayout(FlowLayout.LEFT, 6, 2));
        setOpaque(false);
        setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, UIStyles.COLOR_BORDE));

        add(UIStyles.crearEtiqueta("Estado:"));
        lblEstado = new JLabel("Desconectado");
        lblEstado.setFont(UIStyles.FUENTE_BASE);
        lblEstado.setForeground(UIStyles.COLOR_TEXTO);
        add(lblEstado);
    }

    public void marcarConectado(String host, int puerto) {
        lblEstado.setForeground(new Color(24, 128, 72));
        lblEstado.setText("Conectado a " + host + ":" + puerto);
    }

    public void marcarDesconectado() {
        lblEstado.setForeground(new Color(184, 63, 63));
        lblEstado.setText("Sin conexion - use Reconectar al Servidor");
    }
}
