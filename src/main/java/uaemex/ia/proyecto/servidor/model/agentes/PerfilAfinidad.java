package uaemex.ia.proyecto.servidor.model.agentes;

import uaemex.ia.proyecto.compartido.Disco;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Representa el perfil de afinidades de década y artista del usuario.
 * Se alimenta de los discos de la colección para influir en las puntuaciones de las recomendaciones.
 */
class PerfilAfinidad {
    private final Map<String, Integer> artistas = new HashMap<>();
    private final Map<Integer, Integer> decadas = new HashMap<>();
    private final int total;

    PerfilAfinidad(List<Disco> coleccion) {
        int contador = 0;
        for (Disco disco : coleccion) {
            contador++;
            // Mapea la frecuencia de los artistas (en minúsculas y normalizados)
            String artista = SimilarityUtils.normalizar(disco.getArtista());
            if (!artista.isEmpty()) {
                artistas.merge(artista, 1, Integer::sum);
            }
            // Mapea la frecuencia de las décadas
            int decada = SimilarityUtils.decada(disco.getAnio());
            if (decada > 0) {
                decadas.merge(decada, 1, Integer::sum);
            }
        }
        this.total = contador;
    }

    /**
     * Calcula un puntaje de afinidad para el artista de un disco recomendable.
     *
     * @param disco disco candidato.
     * @return puntaje ponderado.
     */
    double afinidadArtista(Disco disco) {
        String artista = SimilarityUtils.normalizar(disco.getArtista());
        return total == 0 || artista.isEmpty() ? 0.0 : artistas.getOrDefault(artista, 0) * 8.0;
    }

    /**
     * Calcula la afinidad por década, sumando décimas adicionales si son décadas vecinas (+- 10 años).
     *
     * @param disco disco candidato.
     * @return puntaje de afinidad de época.
     */
    double afinidadDecada(Disco disco) {
        if (total == 0) {
            return 0.0;
        }
        int decada = SimilarityUtils.decada(disco.getAnio());
        int coincidencias = decadas.getOrDefault(decada, 0);
        int cercanas = decadas.getOrDefault(decada - 10, 0) + decadas.getOrDefault(decada + 10, 0);
        return (coincidencias * 5.0) + (cercanas * 2.0);
    }

    /**
     * Revisa si un artista ha alcanzado el límite de saturación en la tanda de recomendaciones actual.
     * Esto promueve la diversidad evitando recomendar más de 2 discos del mismo artista.
     */
    boolean artistaSaturado(Disco disco, Map<String, Integer> seleccionados) {
        String artista = SimilarityUtils.normalizar(disco.getArtista());
        return !artista.isEmpty() && seleccionados.getOrDefault(artista, 0) >= 2;
    }
}
