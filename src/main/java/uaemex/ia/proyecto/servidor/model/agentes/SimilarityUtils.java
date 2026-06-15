package uaemex.ia.proyecto.servidor.model.agentes;

import java.text.Normalizer;
import java.util.Locale;

/**
 * Utilidades de normalizacion y similitud textual usadas por busqueda y recomendacion.
 */
public final class SimilarityUtils {

    /**
     * Evita instanciar una clase que solo contiene funciones estaticas.
     */
    private SimilarityUtils() {
    }

    /**
     * Normaliza texto para comparaciones tolerantes a acentos, mayusculas y signos.
     *
     * @param texto texto original.
     * @return texto simplificado para comparar.
     */
    public static String normalizar(String texto) {
        if (texto == null) {
            return "";
        }
        return Normalizer.normalize(texto.trim(), Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9 ]", " ")
                .replaceAll("\\s+", " ")
                .trim();
    }

    /**
     * Calcula la distancia Levenshtein entre dos cadenas.
     *
     * @param a primera cadena normalizada.
     * @param b segunda cadena normalizada.
     * @return cantidad minima de inserciones, borrados o sustituciones.
     */
    public static int levenshtein(String a, String b) {
        int[][] dp = new int[a.length() + 1][b.length() + 1];
        for (int i = 0; i <= a.length(); i++) {
            dp[i][0] = i;
        }
        for (int j = 0; j <= b.length(); j++) {
            dp[0][j] = j;
        }
        for (int i = 1; i <= a.length(); i++) {
            for (int j = 1; j <= b.length(); j++) {
                int costo = a.charAt(i - 1) == b.charAt(j - 1) ? 0 : 1;
                // Cada celda guarda el mejor costo entre borrar, insertar o sustituir caracteres.
                dp[i][j] = Math.min(
                        Math.min(dp[i - 1][j] + 1, dp[i][j - 1] + 1),
                        dp[i - 1][j - 1] + costo);
            }
        }
        return dp[a.length()][b.length()];
    }
}
