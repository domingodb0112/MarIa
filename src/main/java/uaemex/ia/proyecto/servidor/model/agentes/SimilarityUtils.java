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
        // Se limpian los acentos (diacriticos) mediante normalizacion unicode NFD y regex de bloques unicode \p{M}
        return Normalizer.normalize(texto.trim(), Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9 ]", " ") // Reemplazar caracteres no alfanumericos por espacios
                .replaceAll("\\s+", " ")       // Unificar multiples espacios seguidos
                .trim();
    }

    /**
     * Reduce un texto a una clave fonetica simple pensada para consultas en espanol.
     *
     * @param texto texto original.
     * @return clave comparable por sonido aproximado.
     */
    public static String claveFoneticaEspanol(String texto) {
        String valor = normalizar(texto);
        if (valor.isEmpty()) {
            return "";
        }
        // Simplificacion de homofonos en español para tolerar errores comunes
        valor = valor.replace('v', 'b')     // B y V suenan igual
                .replace('z', 's')          // Seseo comun: Z por S
                .replace('x', 's')          // X por S
                .replaceAll("h", "")        // La H es muda
                .replaceAll("ll", "y")      // Yeismo comun: LL por Y
                .replaceAll("qu", "k")      // Sonido de K
                .replaceAll("gue", "ge")    // Unificacion de diptongos suaves
                .replaceAll("gui", "gi")
                .replaceAll("ce|ci", "se")  // C suave (ante E, I) suena como S
                .replace('c', 'k')          // C fuerte (ante A, O, U) suena como K
                .replace('q', 'k')          // Q suena como K
                .replace('j', 'g');         // J suena fuerte como G
        // Eliminar caracteres repetidos consecutivos (ej. "beer" -> "ber") para normalizar longitud
        return valor.replaceAll("(.)\\1+", "$1");
    }

    /**
     * Agrupa anios por decada para comparar epocas musicales cercanas.
     *
     * @param anio anio de publicacion.
     * @return inicio de decada o 0 si el dato no es valido.
     */
    public static int decada(int anio) {
        return anio > 0 ? (anio / 10) * 10 : 0;
    }

    /**
     * Calcula la distancia Levenshtein entre dos cadenas.
     *
     * @param a primera cadena normalizada.
     * @param b segunda cadena normalizada.
     * @return cantidad minima de inserciones, borrados o sustituciones.
     */
    public static int levenshtein(String a, String b) {
        // Matriz dp para almacenar subproblemas. dp[i][j] tendra la distancia entre a[0..i-1] y b[0..j-1]
        int[][] dp = new int[a.length() + 1][b.length() + 1];
        
        // Inicializar la primera columna: costo de borrar caracteres de 'a' para llegar a cadena vacia
        for (int i = 0; i <= a.length(); i++) {
            dp[i][0] = i;
        }
        // Inicializar la primera fila: costo de insertar caracteres para armar 'b' desde cadena vacia
        for (int j = 0; j <= b.length(); j++) {
            dp[0][j] = j;
        }
        
        // Llenar el resto de la matriz iterando sobre ambas cadenas
        for (int i = 1; i <= a.length(); i++) {
            for (int j = 1; j <= b.length(); j++) {
                // Si los caracteres coinciden, el costo de sustitucion es 0, de lo contrario es 1
                int costo = a.charAt(i - 1) == b.charAt(j - 1) ? 0 : 1;
                
                // Se busca el minimo costo entre tres transiciones posibles:
                // 1. dp[i-1][j] + 1 (Borrado del caracter actual en la cadena 'a')
                // 2. dp[i][j-1] + 1 (Insercion de un caracter para coincidir con 'b')
                // 3. dp[i-1][j-1] + costo (Sustitucion del caracter de 'a' por el de 'b')
                dp[i][j] = Math.min(
                        Math.min(dp[i - 1][j] + 1, dp[i][j - 1] + 1),
                        dp[i - 1][j - 1] + costo);
            }
        }
        // La celda final derecha-inferior guarda la distancia total minima
        return dp[a.length()][b.length()];
    }
}
