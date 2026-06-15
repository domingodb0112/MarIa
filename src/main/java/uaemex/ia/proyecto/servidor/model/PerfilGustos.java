package uaemex.ia.proyecto.servidor.model;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Resumen estadistico de la coleccion del usuario por genero musical.
 */
public class PerfilGustos {

    private final int totalDiscos;
    private final String generoFavorito;
    private final Map<String, Integer> frecuenciaPorGenero;
    private final Map<String, Double> porcentajePorGenero;

    /**
     * Crea un perfil inmutable a partir de frecuencias y porcentajes calculados.
     *
     * @param totalDiscos total de discos considerados en el perfil.
     * @param generoFavorito genero con mayor frecuencia.
     * @param frecuenciaPorGenero conteo por genero.
     * @param porcentajePorGenero porcentaje por genero.
     */
    public PerfilGustos(int totalDiscos, String generoFavorito,
                        Map<String, Integer> frecuenciaPorGenero,
                        Map<String, Double> porcentajePorGenero) {
        this.totalDiscos = totalDiscos;
        this.generoFavorito = generoFavorito;
        this.frecuenciaPorGenero = new LinkedHashMap<>(frecuenciaPorGenero);
        this.porcentajePorGenero = new LinkedHashMap<>(porcentajePorGenero);
    }

    /**
     * @return total de discos usados para calcular el perfil.
     */
    public int getTotalDiscos() {
        return totalDiscos;
    }

    /**
     * @return genero con mayor presencia o "Sin datos".
     */
    public String getGeneroFavorito() {
        return generoFavorito;
    }

    /**
     * @return mapa no modificable con conteos por genero.
     */
    public Map<String, Integer> getFrecuenciaPorGenero() {
        return Collections.unmodifiableMap(frecuenciaPorGenero);
    }

    /**
     * @return mapa no modificable con porcentajes por genero.
     */
    public Map<String, Double> getPorcentajePorGenero() {
        return Collections.unmodifiableMap(porcentajePorGenero);
    }

    /**
     * @return texto de depuracion con el contenido completo del perfil.
     */
    @Override
    public String toString() {
        return "PerfilGustos{" +
                "totalDiscos=" + totalDiscos +
                ", generoFavorito='" + generoFavorito + '\'' +
                ", frecuenciaPorGenero=" + frecuenciaPorGenero +
                ", porcentajePorGenero=" + porcentajePorGenero +
                '}';
    }
}
