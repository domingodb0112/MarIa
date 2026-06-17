package uaemex.ia.proyecto.servidor.model.agentes;

import org.junit.Test;
import uaemex.ia.proyecto.compartido.Disco;
import uaemex.ia.proyecto.servidor.model.PerfilGustos;

import java.util.Collections;
import java.util.List;
import java.util.Random;

import static org.junit.Assert.*;

public class AgenteRecomendadorTest {
    private static final String USER_A = "test-user-a";
    private static final String USER_B = "test-user-b";

    @Test
    public void feedbackPositivoConvergeHaciaValorAlto() {
        AgenteRecomendador recomendador = new AgenteRecomendador(new Random(7));
        Disco disco = salsa();

        for (int i = 0; i < 30; i++) {
            recomendador.registrarRetroalimentacion(USER_A, disco, true);
        }

        assertTrue(recomendador.valorAprendido(USER_A, "genero:salsa") > 0.95);
        assertTrue(recomendador.valorAprendido(USER_A, "artista:orquesta prueba") > 0.95);
    }

    @Test
    public void feedbackNegativoConvergeHaciaValorBajo() {
        AgenteRecomendador recomendador = new AgenteRecomendador(new Random(7));
        Disco disco = jazz();

        for (int i = 0; i < 30; i++) {
            recomendador.registrarRetroalimentacion(USER_A, disco, false);
        }

        assertTrue(recomendador.valorAprendido(USER_A, "genero:jazz") < -0.95);
    }

    @Test
    public void aprendizajeSeAislaPorUsuario() {
        AgenteRecomendador recomendador = new AgenteRecomendador(new Random(3));

        recomendador.registrarRetroalimentacion(USER_A, salsa(), true);
        recomendador.registrarRetroalimentacion(USER_B, salsa(), false);

        assertTrue(recomendador.valorAprendido(USER_A, "genero:salsa") > 0.0);
        assertTrue(recomendador.valorAprendido(USER_B, "genero:salsa") < 0.0);
    }

    @Test
    public void bandidoEpsilonGreedyPrefiereGeneroReforzado() {
        AgenteRecomendador recomendador = new AgenteRecomendador(new Random(11));

        for (int i = 0; i < 25; i++) {
            recomendador.registrarRetroalimentacion(USER_A, salsa(), true);
            recomendador.registrarRetroalimentacion(USER_A, jazz(), false);
        }

        List<Disco> sugerencias = recomendador.recomendar(USER_A, perfilVacio(), Collections.emptyList());

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
