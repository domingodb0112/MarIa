package uaemex.ia.proyecto.servidor.model.agentes;

import uaemex.ia.proyecto.compartido.Disco;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Agente que realiza busquedas aproximadas dentro de la coleccion del usuario.
 */
public class AgenteBuscador {

    private static final int MAX_RESULTADOS = 10;

    /**
     * Busca discos cuyo titulo, artista o genero sean similares a la consulta.
     *
     * @param consulta texto ingresado por el usuario.
     * @param coleccion discos donde se buscara.
     * @return hasta diez discos ordenados por mejor coincidencia.
     */
    public List<Disco> buscar(String consulta, List<Disco> coleccion) {
        String consultaNormalizada = SimilarityUtils.normalizar(consulta);
        List<ResultadoBusqueda> candidatos = new ArrayList<>();

        if (consultaNormalizada.isEmpty()) {
            return new ArrayList<>();
        }

        for (Disco disco : coleccion) {
            int puntaje = calcularPuntaje(consultaNormalizada, disco);
            if (puntaje <= umbral(consultaNormalizada.length())) {
                // Menor distancia significa mayor similitud con la consulta.
                candidatos.add(new ResultadoBusqueda(disco, puntaje));
            }
        }

        // Ordena primero por similitud y despues por titulo/artista para resultados deterministas.
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

    /**
     * Calcula el mejor puntaje de coincidencia entre la consulta y los campos buscables.
     *
     * @param consulta consulta ya normalizada.
     * @param disco disco candidato.
     * @return distancia minima encontrada.
     */
    private int calcularPuntaje(String consulta, Disco disco) {
        int mejor = Integer.MAX_VALUE;
        mejor = Math.min(mejor, distanciaCampo(consulta, disco.getTitulo()));
        mejor = Math.min(mejor, distanciaCampo(consulta, disco.getArtista()));
        mejor = Math.min(mejor, distanciaCampo(consulta, disco.getGenero()));
        return mejor;
    }

    /**
     * Evalua la distancia entre la consulta y un campo de texto.
     *
     * @param consulta consulta normalizada.
     * @param valor valor original del campo.
     * @return distancia Levenshtein minima o Integer.MAX_VALUE si no hay campo.
     */
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
            // Comparar palabra por palabra ayuda con titulos o artistas compuestos.
            mejor = Math.min(mejor, SimilarityUtils.levenshtein(consulta, palabra));
        }
        return mejor;
    }

    /**
     * Define la tolerancia de errores permitidos segun la longitud de la consulta.
     *
     * @param longitudConsulta cantidad de caracteres normalizados.
     * @return distancia maxima aceptada para considerar una coincidencia.
     */
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
