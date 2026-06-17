package uaemex.ia.proyecto.servidor.model.agentes;

import org.junit.Test;
import uaemex.ia.proyecto.compartido.Disco;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

public class AgenteBuscadorTest {
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

    @Test
    public void normalizaAcentosYCalculaDecada() {
        assertEquals("cancion del mariachi", SimilarityUtils.normalizar("Canción del Mariachi"));
        assertEquals(1990, SimilarityUtils.decada(1999));
    }
}
