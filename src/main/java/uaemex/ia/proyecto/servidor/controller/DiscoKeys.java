package uaemex.ia.proyecto.servidor.controller;

import uaemex.ia.proyecto.compartido.Disco;
import uaemex.ia.proyecto.servidor.model.agentes.SimilarityUtils;

/**
 * Utilidad para generar claves uniformes comparables para la identificación única
 * de discos físicos en el servidor.
 */
public final class DiscoKeys {
    private DiscoKeys() {
    }

    /**
     * Genera una clave combinada con el título y artista normalizados (en minúsculas y sin acentos).
     *
     * @param disco el objeto Disco a indexar/identificar.
     * @return la clave unificada en formato "titulo|artista".
     */
    public static String clave(Disco disco) {
        return SimilarityUtils.normalizar(disco.getTitulo()) + "|"
                + SimilarityUtils.normalizar(disco.getArtista());
    }
}
