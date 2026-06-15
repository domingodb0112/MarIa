package uaemex.ia.proyecto.cliente.view;

import javax.swing.*;
import java.awt.*;

/**
 * Franja inferior que informa si el cliente esta conectado al servidor.
 */
public class PanelEstado extends JPanel {

    private final JLabel lblEstado;

    /**
     * Inicializa la etiqueta de estado con el valor de desconexion.
     */
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

    /**
     * Muestra un estado exitoso con la direccion del servidor activo.
     *
     * @param host host conectado.
     * @param puerto puerto conectado.
     */
    public void marcarConectado(String host, int puerto) {
        lblEstado.setForeground(new Color(24, 128, 72));
        lblEstado.setText("Conectado a " + host + ":" + puerto);
    }

    /**
     * Muestra un estado de desconexion e indica la accion esperada.
     */
    public void marcarDesconectado() {
        lblEstado.setForeground(new Color(184, 63, 63));
        lblEstado.setText("Sin conexion - use Reconectar al Servidor");
    }
}
