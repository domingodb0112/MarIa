package uaemex.ia.proyecto.servidor.model.agentes;

import uaemex.ia.proyecto.compartido.Disco;

/**
 * Par interno que conserva un disco encontrado y su distancia de busqueda.
 */
class ResultadoBusqueda {
    private final Disco disco;
    private final int puntaje;

    /**
     * Crea el resultado con el puntaje calculado.
     *
     * @param disco disco candidato.
     * @param puntaje distancia de coincidencia; menor es mejor.
     */
    ResultadoBusqueda(Disco disco, int puntaje) {
        this.disco = disco;
        this.puntaje = puntaje;
    }

    /**
     * @return disco candidato de la busqueda.
     */
    Disco getDisco() {
        return disco;
    }

    /**
     * @return distancia de coincidencia calculada.
     */
    int getPuntaje() {
        return puntaje;
    }
}
