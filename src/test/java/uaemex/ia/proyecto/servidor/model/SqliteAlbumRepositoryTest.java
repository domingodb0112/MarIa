package uaemex.ia.proyecto.servidor.model;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import uaemex.ia.proyecto.compartido.Disco;

import java.io.File;
import java.util.List;

import static org.junit.Assert.*;

public class SqliteAlbumRepositoryTest {
    @Rule
    public TemporaryFolder temp = new TemporaryFolder();

    @Test
    public void paginaResultadosDeColeccionEscolar() throws Exception {
        SqliteAlbumRepository repo = repo("maria-test.sqlite");
        repo.guardar(disco("Uno", "Grupo A"));
        repo.guardar(disco("Dos", "Grupo B"));
        repo.guardar(disco("Tres", "Grupo C"));

        List<Disco> pagina = repo.listarPagina(1, 2);

        assertEquals(3, repo.contar());
        assertEquals(1, pagina.size());
        assertEquals("Tres", pagina.get(0).getTitulo());
    }

    @Test
    public void evitaDuplicadosEnColeccionUnica() throws Exception {
        SqliteAlbumRepository repo = repo("maria-duplicados.sqlite");
        Disco disco = disco("Uno", "Grupo A");

        repo.guardar(disco);
        repo.guardar(disco);

        assertEquals(1, repo.contar());
        assertTrue(repo.existe(disco));
    }

    @Test
    public void buscaCandidatosDesdeSqlite() throws Exception {
        SqliteAlbumRepository repo = repo("maria-busqueda.sqlite");
        repo.guardar(new Disco("Bocanada", "Gustavo Cerati", 1999, "Rock", "CD"));
        repo.guardar(new Disco("Kind of Blue", "Miles Davis", 1959, "Jazz", "Vinilo"));

        List<Disco> resultados = repo.buscarCandidatos("jazz", 10);

        assertEquals(1, resultados.size());
        assertEquals("Kind of Blue", resultados.get(0).getTitulo());
    }

    private SqliteAlbumRepository repo(String nombre) throws Exception {
        File db = temp.newFile(nombre);
        return new SqliteAlbumRepository("jdbc:sqlite:" + db.getAbsolutePath(), false);
    }

    private Disco disco(String titulo, String artista) {
        return new Disco(titulo, artista, 2001, "Rock", "CD");
    }
}
