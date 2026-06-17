package uaemex.ia.proyecto.servidor.controller;

import uaemex.ia.proyecto.compartido.Disco;
import uaemex.ia.proyecto.servidor.model.agentes.SimilarityUtils;

final class DiscoKeys {
    private DiscoKeys() {
    }

    static String clave(Disco disco) {
        return SimilarityUtils.normalizar(disco.getTitulo()) + "|"
                + SimilarityUtils.normalizar(disco.getArtista());
    }
}
