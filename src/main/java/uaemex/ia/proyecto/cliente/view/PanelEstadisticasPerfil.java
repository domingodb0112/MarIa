package uaemex.ia.proyecto.cliente.view;

import uaemex.ia.proyecto.compartido.Disco;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Tablero compacto con estadisticas del perfil musical visible en Swing.
 */
public class PanelEstadisticasPerfil extends JPanel {
    private final JLabel lblTotal = dato("0");
    private final JLabel lblGenero = dato("Sin datos");
    private final JLabel lblDecada = dato("Sin datos");
    private final JLabel lblArtista = dato("Sin datos");
    private final DefaultListModel<String> modeloRecomendaciones = new DefaultListModel<>();
    private final List<Disco> coleccion = new ArrayList<>();

    public PanelEstadisticasPerfil() {
        setLayout(new BorderLayout(0, 8));
        setPreferredSize(new Dimension(240, 0));
        setBackground(UIStyles.COLOR_PANEL);
        setBorder(UIStyles.crearBordeTitulo("Perfil"));
        add(crearResumen(), BorderLayout.NORTH);
        add(crearListaRecomendaciones(), BorderLayout.CENTER);
    }

    public void actualizarColeccion(List<Disco> discos) {
        coleccion.clear();
        if (discos != null) {
            coleccion.addAll(discos);
        }
        refrescarResumen();
    }

    public void agregarDisco(Disco disco) {
        if (disco != null) {
            coleccion.add(disco);
            refrescarResumen();
        }
    }

    public void actualizarRecomendaciones(List<Disco> recomendaciones) {
        modeloRecomendaciones.clear();
        if (recomendaciones == null || recomendaciones.isEmpty()) {
            modeloRecomendaciones.addElement("Sin recomendaciones recientes");
            return;
        }
        recomendaciones.stream()
                .limit(5)
                .map(d -> d.getTitulo() + " - " + d.getArtista())
                .forEach(modeloRecomendaciones::addElement);
    }

    private JPanel crearResumen() {
        JPanel panel = new JPanel(new GridLayout(4, 1, 0, 6));
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(8, 10, 4, 10));
        panel.add(fila("Discos", lblTotal));
        panel.add(fila("Genero", lblGenero));
        panel.add(fila("Epoca", lblDecada));
        panel.add(fila("Artista", lblArtista));
        return panel;
    }

    private JScrollPane crearListaRecomendaciones() {
        JList<String> lista = new JList<>(modeloRecomendaciones);
        lista.setFont(UIStyles.FUENTE_BASE);
        lista.setForeground(UIStyles.COLOR_TEXTO);
        lista.setFixedCellHeight(24);
        modeloRecomendaciones.addElement("Sin recomendaciones recientes");
        JScrollPane scroll = new JScrollPane(lista);
        scroll.setBorder(BorderFactory.createTitledBorder("Recientes"));
        return scroll;
    }

    private JPanel fila(String etiqueta, JLabel valor) {
        JPanel panel = new JPanel(new BorderLayout(6, 0));
        panel.setOpaque(false);
        panel.add(UIStyles.crearEtiqueta(etiqueta + ":"), BorderLayout.WEST);
        panel.add(valor, BorderLayout.CENTER);
        return panel;
    }

    private static JLabel dato(String texto) {
        JLabel label = new JLabel(texto);
        label.setFont(UIStyles.FUENTE_SUBTITULO);
        label.setForeground(UIStyles.COLOR_PRIMARIO);
        return label;
    }

    private void refrescarResumen() {
        lblTotal.setText(String.valueOf(coleccion.size()));
        lblGenero.setText(topTexto(coleccion, Disco::getGenero));
        lblArtista.setText(topTexto(coleccion, Disco::getArtista));
        lblDecada.setText(topDecada());
    }

    private String topDecada() {
        Map<String, Integer> conteo = new LinkedHashMap<>();
        for (Disco disco : coleccion) {
            int decada = decada(disco.getAnio());
            if (decada > 0) {
                conteo.merge(decada + "s", 1, Integer::sum);
            }
        }
        return topEntrada(conteo);
    }

    private String topTexto(List<Disco> discos, java.util.function.Function<Disco, String> extractor) {
        Map<String, Integer> conteo = new LinkedHashMap<>();
        for (Disco disco : discos) {
            String valor = extractor.apply(disco);
            if (valor != null && !valor.trim().isEmpty()) {
                conteo.merge(valor.trim(), 1, Integer::sum);
            }
        }
        return topEntrada(conteo);
    }

    private String topEntrada(Map<String, Integer> conteo) {
        return conteo.entrySet().stream()
                .max(Comparator.comparingInt(Map.Entry<String, Integer>::getValue))
                .map(Map.Entry::getKey)
                .orElse("Sin datos");
    }

    private int decada(int anio) {
        return anio > 0 ? (anio / 10) * 10 : 0;
    }
}
