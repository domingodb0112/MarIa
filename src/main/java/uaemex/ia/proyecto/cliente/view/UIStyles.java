package uaemex.ia.proyecto.cliente.view;

import com.formdev.flatlaf.FlatLightLaf;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;

/**
 * Centraliza colores, fuentes y estilos visuales usados por la interfaz Swing.
 */
public final class UIStyles {

    public static final Color COLOR_FONDO = new Color(244, 247, 251);
    public static final Color COLOR_PANEL = Color.WHITE;
    public static final Color COLOR_PRIMARIO = new Color(39, 76, 119);
    public static final Color COLOR_ACENTO = new Color(96, 150, 186);
    public static final Color COLOR_TEXTO = new Color(29, 38, 48);
    public static final Color COLOR_BORDE = new Color(210, 220, 232);
    
    public static final Font FUENTE_BASE = new Font("SansSerif", Font.PLAIN, 13);
    public static final Font FUENTE_TITULO = new Font("SansSerif", Font.BOLD, 20);
    public static final Font FUENTE_SUBTITULO = new Font("SansSerif", Font.BOLD, 13);

    /**
     * Evita instanciar la clase de estilos compartidos.
     */
    private UIStyles() {
        // Clase de utilidad
    }

    /**
     * Configura FlatLaf y valores globales de UIManager para controles basicos.
     */
    public static void setupLookAndFeel() {
        if (!FlatLightLaf.setup()) {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) {
            }
        }
        UIManager.put("Button.font", FUENTE_SUBTITULO);
        UIManager.put("Label.font", FUENTE_BASE);
        UIManager.put("TextField.font", FUENTE_BASE);
        UIManager.put("RadioButton.font", FUENTE_BASE);
    }

    /**
     * Crea una etiqueta con la fuente y color estandar del sistema.
     *
     * @param texto texto mostrado.
     * @return JLabel configurado.
     */
    public static JLabel crearEtiqueta(String texto) {
        JLabel label = new JLabel(texto);
        label.setFont(FUENTE_BASE);
        label.setForeground(COLOR_TEXTO);
        return label;
    }

    /**
     * Crea un borde titulado consistente para paneles de formulario y resultados.
     *
     * @param titulo titulo visible del panel.
     * @return borde listo para asignarse al componente.
     */
    public static TitledBorder crearBordeTitulo(String titulo) {
        TitledBorder borde = BorderFactory.createTitledBorder(
                new LineBorder(COLOR_BORDE, 1, true),
                titulo,
                TitledBorder.LEFT,
                TitledBorder.TOP,
                FUENTE_SUBTITULO,
                COLOR_PRIMARIO);
        borde.setTitlePosition(TitledBorder.TOP);
        return borde;
    }

    /**
     * Aplica el estilo de entrada usado por todos los JTextField del cliente.
     *
     * @param campo campo a modificar.
     */
    public static void estilizarCampo(JTextField campo) {
        campo.setFont(FUENTE_BASE);
        campo.setForeground(COLOR_TEXTO);
        campo.setBackground(new Color(250, 252, 255));
        campo.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(COLOR_BORDE, 1, true),
                new EmptyBorder(6, 8, 6, 8)));
    }

    /**
     * Aplica estilo transparente y tipografia base a un radio button.
     *
     * @param radio control a modificar.
     */
    public static void estilizarRadio(JRadioButton radio) {
        radio.setOpaque(false);
        radio.setFont(FUENTE_BASE);
        radio.setForeground(COLOR_TEXTO);
    }

    /**
     * Aplica el estilo visual de accion principal.
     *
     * @param boton boton a modificar.
     */
    public static void estilizarBotonPrimario(JButton boton) {
        estilizarBotonBase(boton, COLOR_PRIMARIO, Color.WHITE);
    }

    /**
     * Aplica el estilo visual de accion secundaria.
     *
     * @param boton boton a modificar.
     */
    public static void estilizarBotonSecundario(JButton boton) {
        estilizarBotonBase(boton, COLOR_ACENTO, Color.WHITE);
    }

    /**
     * Comparte el formato comun de botones y recibe los colores especificos.
     *
     * @param boton boton a modificar.
     * @param fondo color de fondo.
     * @param texto color del texto.
     */
    private static void estilizarBotonBase(JButton boton, Color fondo, Color texto) {
        boton.setFont(FUENTE_SUBTITULO);
        boton.setForeground(texto);
        boton.setBackground(fondo);
        boton.setFocusPainted(false);
        boton.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(fondo.darker(), 1, true),
                new EmptyBorder(7, 10, 7, 10)));
        boton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }
}
