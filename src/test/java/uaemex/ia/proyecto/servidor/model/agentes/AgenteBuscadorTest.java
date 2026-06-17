package uaemex.ia.proyecto.servidor.model.agentes;

import org.junit.Test;
import uaemex.ia.proyecto.compartido.Disco;
import java.util.Arrays;
import java.util.List;
import static org.junit.Assert.*;

/**
 * Pruebas unitarias para el Agente Buscador y utilidades de similitud fonética.
 */
public class AgenteBuscadorTest {

    /**
     * Valida que búsquedas aproximadas basadas en fonética en español encuentren coincidencias.
     * Ejemplo: Buscar "Serati" debe dar como resultado a "Cerati".
     */
    @Test
    public void encuentraCoincidenciasFoneticasEnEspanol() {
        AgenteBuscador buscador = new AgenteBuscador();
        List<Disco> discos = Arrays.asList(
                new Disco("Bocanada", "Gustavo Cerati", 1999, "Rock", "CD"),
                new Disco("Clics Modernos", "Charly Garcia", 1983, "Rock", "Vinilo"));

        List<Disco> resultados = buscador.buscar("Serati", discos);

        assertFalse(resultados.isEmpty());
        assertEquals("Gustavo Cerati", resultados.get(0).getArtista());
    }

    /**
     * Verifica que se eliminen correctamente los acentos y se calulen las décadas de manera uniforme.
     */
    @Test
    public void normalizaAcentosYCalculaDecada() {
        assertEquals("cancion del mariachi", SimilarityUtils.normalizar("Canción del Mariachi"));
        assertEquals(1990, SimilarityUtils.decada(1999));
    }
}
