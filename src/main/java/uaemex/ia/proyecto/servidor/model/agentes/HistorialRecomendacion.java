package uaemex.ia.proyecto.servidor.model.agentes;

import uaemex.ia.proyecto.compartido.Disco;

/**
 * Registro serializable de una recomendacion emitida por MarIA.
 */
class HistorialRecomendacion {
    private String claveDisco;
    private String titulo;
    private String artista;
    private String genero;
    private int anio;
    private long timestamp;

    HistorialRecomendacion() {
    }

    HistorialRecomendacion(Disco disco, String claveDisco) {
        this.claveDisco = claveDisco;
        this.titulo = disco.getTitulo();
        this.artista = disco.getArtista();
        this.genero = disco.getGenero();
        this.anio = disco.getAnio();
        this.timestamp = System.currentTimeMillis();
    }

    String getClaveDisco() {
        return claveDisco;
    }

    String getArtistaNormalizado() {
        return SimilarityUtils.normalizar(artista);
    }

    int getDecada() {
        return SimilarityUtils.decada(anio);
    }

    long getTimestamp() {
        return timestamp;
    }
}
