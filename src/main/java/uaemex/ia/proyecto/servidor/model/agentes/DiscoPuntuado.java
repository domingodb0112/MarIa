package uaemex.ia.proyecto.servidor.model.agentes;

import uaemex.ia.proyecto.compartido.Disco;

class DiscoPuntuado {
    private final Disco disco;
    private final double puntaje;

    DiscoPuntuado(Disco disco, double puntaje) {
        this.disco = disco;
        this.puntaje = puntaje;
    }

    Disco getDisco() {
        return disco;
    }

    double getPuntaje() {
        return puntaje;
    }
}
