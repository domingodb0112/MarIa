package uaemex.ia.proyecto.servidor.model.agentes;

import uaemex.ia.proyecto.compartido.Disco;

class ResultadoBusqueda {
    private final Disco disco;
    private final int puntaje;

    ResultadoBusqueda(Disco disco, int puntaje) {
        this.disco = disco;
        this.puntaje = puntaje;
    }

    Disco getDisco() {
        return disco;
    }

    int getPuntaje() {
        return puntaje;
    }
}
