package uaemex.ia.proyecto.servidor.model.agentes;

import org.junit.Test;
import uaemex.ia.proyecto.compartido.Disco;
import uaemex.ia.proyecto.servidor.model.PerfilGustos;

import java.util.Collections;
import java.util.List;
import java.util.Random;

import static org.junit.Assert.*;

public class AgenteRecomendadorTest {
    @Test
    public void feedbackPositivoConvergeHaciaValorAlto() {
        AgenteRecomendador recomendador = new AgenteRecomendador(new Random(7));
        for (int i = 0; i < 30; i++) recomendador.registrarRetroalimentacion(salsa(), true);

        assertTrue(recomendador.valorAprendido("default-user", "genero:salsa") > 0.95);
        assertTrue(recomendador.valorAprendido("default-user", "artista:orquesta prueba") > 0.95);
    }

    @Test
    public void feedbackNegativoConvergeHaciaValorBajo() {
        AgenteRecomendador recomendador = new AgenteRecomendador(new Random(7));
        for (int i = 0; i < 30; i++) recomendador.registrarRetroalimentacion(jazz(), false);

        assertTrue(recomendador.valorAprendido("default-user", "genero:jazz") < -0.95);
    }

    @Test
    public void bandidoEpsilonGreedyPrefiereGeneroReforzado() {
        AgenteRecomendador recomendador = new AgenteRecomendador(new Random(11));
        for (int i = 0; i < 25; i++) {
            recomendador.registrarRetroalimentacion(salsa(), true);
            recomendador.registrarRetroalimentacion(jazz(), false);
        }

        List<Disco> sugerencias = recomendador.recomendar(perfilVacio(), Collections.emptyList());

        assertFalse(sugerencias.isEmpty());
        assertEquals("Salsa", sugerencias.get(0).getGenero());
    }

    private PerfilGustos perfilVacio() {
        return new PerfilGustos(0, "Sin datos", Collections.emptyMap(), Collections.emptyMap());
    }

    private Disco salsa() {
        return new Disco("Salsa de Prueba", "Orquesta Prueba", 1999, "Salsa", "CD");
    }

    private Disco jazz() {
        return new Disco("Jazz de Prueba", "Cuarteto Prueba", 1962, "Jazz", "Vinilo");
    }
}
