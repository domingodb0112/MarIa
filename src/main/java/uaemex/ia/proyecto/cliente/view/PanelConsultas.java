package uaemex.ia.proyecto.cliente.view;

import uaemex.ia.proyecto.compartido.Disco;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.function.Consumer;

/**
 * Panel de consultas del cliente: busqueda textual y recomendaciones.
 */
public class PanelConsultas extends JPanel {
    private final PanelBusqueda panelBusqueda;
    private final PanelRecomendaciones panelRecomendaciones;

    public PanelConsultas(Consumer<String> onBuscar, Runnable onRecomendar,
                          Consumer<Disco> onAceptarRecomendacion,
                          Consumer<Disco> onRechazarRecomendacion) {
        setLayout(new GridLayout(2, 1, 0, 8));
        setOpaque(false);
        panelBusqueda = new PanelBusqueda(onBuscar);
        panelRecomendaciones = new PanelRecomendaciones(onRecomendar,
                onAceptarRecomendacion, onRechazarRecomendacion);
        add(panelBusqueda);
        add(panelRecomendaciones);
    }

    public void limpiarBusqueda() {
        panelBusqueda.limpiar();
    }

    public void actualizarRecomendaciones(List<Disco> recomendaciones) {
        panelRecomendaciones.actualizarRecomendaciones(recomendaciones);
    }

    public void setBotonera(boolean enabled) {
        panelBusqueda.setBotonera(enabled);
        panelRecomendaciones.setBotonera(enabled);
    }
}
