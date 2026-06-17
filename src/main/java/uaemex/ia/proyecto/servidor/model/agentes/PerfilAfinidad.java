package uaemex.ia.proyecto.servidor.model.agentes;

import uaemex.ia.proyecto.compartido.Disco;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Resume afinidades de artista y epoca deducidas desde la coleccion del usuario.
 */
class PerfilAfinidad {
    private final Map<String, Integer> artistas = new HashMap<>();
    private final Map<Integer, Integer> decadas = new HashMap<>();
    private final int total;

    PerfilAfinidad(List<Disco> coleccion) {
        int contador = 0;
        for (Disco disco : coleccion) {
            contador++;
            String artista = SimilarityUtils.normalizar(disco.getArtista());
            if (!artista.isEmpty()) {
                artistas.merge(artista, 1, Integer::sum);
            }
            int decada = SimilarityUtils.decada(disco.getAnio());
            if (decada > 0) {
                decadas.merge(decada, 1, Integer::sum);
            }
        }
        this.total = contador;
    }

    double afinidadArtista(Disco disco) {
        String artista = SimilarityUtils.normalizar(disco.getArtista());
        return total == 0 || artista.isEmpty() ? 0.0 : artistas.getOrDefault(artista, 0) * 8.0;
    }

    double afinidadDecada(Disco disco) {
        if (total == 0) {
            return 0.0;
        }
        int decada = SimilarityUtils.decada(disco.getAnio());
        int coincidencias = decadas.getOrDefault(decada, 0);
        int cercanas = decadas.getOrDefault(decada - 10, 0) + decadas.getOrDefault(decada + 10, 0);
        return (coincidencias * 5.0) + (cercanas * 2.0);
    }

    boolean artistaSaturado(Disco disco, Map<String, Integer> seleccionados) {
        String artista = SimilarityUtils.normalizar(disco.getArtista());
        return !artista.isEmpty() && seleccionados.getOrDefault(artista, 0) >= 2;
    }
}
