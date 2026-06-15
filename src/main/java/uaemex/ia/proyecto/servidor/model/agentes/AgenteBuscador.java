package uaemex.ia.proyecto.servidor.model.agentes;

import uaemex.ia.proyecto.compartido.Disco;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class AgenteBuscador {

    private static final int MAX_RESULTADOS = 10;

    public List<Disco> buscar(String consulta, List<Disco> coleccion) {
        String consultaNormalizada = SimilarityUtils.normalizar(consulta);
        List<ResultadoBusqueda> candidatos = new ArrayList<>();

        if (consultaNormalizada.isEmpty()) {
            return new ArrayList<>();
        }

        for (Disco disco : coleccion) {
            int puntaje = calcularPuntaje(consultaNormalizada, disco);
            if (puntaje <= umbral(consultaNormalizada.length())) {
                candidatos.add(new ResultadoBusqueda(disco, puntaje));
            }
        }

        candidatos.sort(Comparator
                .comparingInt(ResultadoBusqueda::getPuntaje)
                .thenComparing(r -> SimilarityUtils.normalizar(r.getDisco().getTitulo()))
                .thenComparing(r -> SimilarityUtils.normalizar(r.getDisco().getArtista())));

        List<Disco> resultados = new ArrayList<>();
        for (ResultadoBusqueda candidato : candidatos) {
            if (resultados.size() == MAX_RESULTADOS) {
                break;
            }
            resultados.add(candidato.getDisco());
        }
        return resultados;
    }

    private int calcularPuntaje(String consulta, Disco disco) {
        int mejor = Integer.MAX_VALUE;
        mejor = Math.min(mejor, distanciaCampo(consulta, disco.getTitulo()));
        mejor = Math.min(mejor, distanciaCampo(consulta, disco.getArtista()));
        mejor = Math.min(mejor, distanciaCampo(consulta, disco.getGenero()));
        return mejor;
    }

    private int distanciaCampo(String consulta, String valor) {
        String campo = SimilarityUtils.normalizar(valor);
        if (campo.isEmpty()) {
            return Integer.MAX_VALUE;
        }
        if (campo.contains(consulta)) {
            return 0;
        }

        int mejor = SimilarityUtils.levenshtein(consulta, campo);
        String[] palabras = campo.split("\\s+");
        for (String palabra : palabras) {
            mejor = Math.min(mejor, SimilarityUtils.levenshtein(consulta, palabra));
        }
        return mejor;
    }

    private int umbral(int longitudConsulta) {
        if (longitudConsulta <= 4) {
            return 1;
        }
        if (longitudConsulta <= 8) {
            return 2;
        }
        return Math.max(3, longitudConsulta / 3);
    }
}
