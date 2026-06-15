package uaemex.ia.proyecto.servidor.model.agentes;

import uaemex.ia.proyecto.compartido.Disco;

/**
 * Par interno que asocia un disco candidato con su puntaje de recomendacion.
 */
class DiscoPuntuado {
    private final Disco disco;
    private final double puntaje;

    /**
     * Crea el par candidato-puntaje.
     *
     * @param disco disco evaluado.
     * @param puntaje valor calculado por el recomendador.
     */
    DiscoPuntuado(Disco disco, double puntaje) {
        this.disco = disco;
        this.puntaje = puntaje;
    }

    /**
     * @return disco candidato.
     */
    Disco getDisco() {
        return disco;
    }

    /**
     * @return puntaje asignado al disco.
     */
    double getPuntaje() {
        return puntaje;
    }
}
