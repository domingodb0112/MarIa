package uaemex.ia.proyecto.servidor.controller;

import uaemex.ia.proyecto.compartido.Disco;

import java.time.Year;

final class ValidadorDiscoServidor {
    private ValidadorDiscoServidor() {
    }

    static String validar(Disco disco) {
        if (disco == null) {
            return "Se requieren datos del disco.";
        }
        if (vacio(disco.getTitulo())) {
            return "El titulo es obligatorio.";
        }
        if (vacio(disco.getArtista())) {
            return "El artista es obligatorio.";
        }
        if (vacio(disco.getGenero())) {
            return "El genero es obligatorio.";
        }
        if (vacio(disco.getFormato())) {
            return "El formato es obligatorio.";
        }
        int maximo = Year.now().getValue() + 1;
        if (disco.getAnio() < 1900 || disco.getAnio() > maximo) {
            return "El anio debe estar entre 1900 y " + maximo + ".";
        }
        return "";
    }

    private static boolean vacio(String texto) {
        return texto == null || texto.trim().isEmpty();
    }
}
