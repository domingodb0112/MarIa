package uaemex.ia.proyecto.servidor.model.agentes;

import uaemex.ia.proyecto.compartido.Disco;
import uaemex.ia.proyecto.servidor.model.PerfilGustos;

import java.text.Normalizer;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Agente encargado de analizar la coleccion del usuario y construir su perfil de gustos.
 */
public class AgenteAnalizador {

    /**
     * Calcula frecuencias, porcentajes y genero favorito a partir de los discos registrados.
     *
     * @param coleccion discos actuales del usuario.
     * @return perfil estadistico de gustos.
     */
    public PerfilGustos calcularPerfil(List<Disco> coleccion) {
        Map<String, Integer> frecuencia = new LinkedHashMap<>();

        for (Disco disco : coleccion) {
            String genero = limpiarGenero(disco.getGenero());
            if (!genero.isEmpty()) {
                // Se acumula por genero normalizado para evitar duplicados por mayusculas o acentos.
                frecuencia.put(genero, frecuencia.getOrDefault(genero, 0) + 1);
            }
        }

        int total = frecuencia.values().stream().mapToInt(Integer::intValue).sum();
        Map<String, Integer> frecuenciaOrdenada = ordenarPorFrecuencia(frecuencia);
        Map<String, Double> porcentajes = new LinkedHashMap<>();

        for (Map.Entry<String, Integer> entry : frecuenciaOrdenada.entrySet()) {
            // Los porcentajes se redondean a dos decimales para mostrarlos de forma legible.
            double porcentaje = total == 0 ? 0.0 : (entry.getValue() * 100.0) / total;
            porcentajes.put(entry.getKey(), Math.round(porcentaje * 100.0) / 100.0);
        }

        String favorito = frecuenciaOrdenada.keySet().stream().findFirst().orElse("Sin datos");
        return new PerfilGustos(total, favorito, frecuenciaOrdenada, porcentajes);
    }

    /**
     * Ordena los generos de mayor a menor frecuencia y usa el nombre como desempate.
     *
     * @param frecuencia conteos sin ordenar.
     * @return mapa ordenado conservando el orden de insercion.
     */
    private Map<String, Integer> ordenarPorFrecuencia(Map<String, Integer> frecuencia) {
        return frecuencia.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue(Comparator.reverseOrder())
                        .thenComparing(Map.Entry.comparingByKey()))
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (a, b) -> a,
                        LinkedHashMap::new));
    }

    /**
     * Normaliza el genero para comparar entradas equivalentes.
     *
     * @param genero genero capturado en un disco.
     * @return genero sin acentos y con capitalizacion uniforme.
     */
    private String limpiarGenero(String genero) {
        if (genero == null) {
            return "";
        }
        String limpio = Normalizer.normalize(genero.trim(), Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "");
        if (limpio.isEmpty()) {
            return "";
        }
        return limpio.substring(0, 1).toUpperCase() + limpio.substring(1).toLowerCase();
    }
}
