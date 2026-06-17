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
    public void segmentaPorUsuarioYPaginaResultados() throws Exception {
        File db = temp.newFile("maria-test.sqlite");
        SqliteAlbumRepository repo = new SqliteAlbumRepository("jdbc:sqlite:" + db.getAbsolutePath());

        repo.guardar("ana", disco("Uno", "Grupo A"));
        repo.guardar("ana", disco("Dos", "Grupo B"));
        repo.guardar("ana", disco("Tres", "Grupo C"));
        repo.guardar("luis", disco("Otro", "Grupo D"));

        List<Disco> pagina = repo.listarPagina("ana", 1, 2);

        assertEquals(3, repo.contar("ana"));
        assertEquals(1, repo.contar("luis"));
        assertEquals(1, pagina.size());
        assertEquals("Tres", pagina.get(0).getTitulo());
    }

    @Test
    public void evitaDuplicadosDentroDelMismoUsuario() throws Exception {
        File db = temp.newFile("maria-duplicados.sqlite");
        SqliteAlbumRepository repo = new SqliteAlbumRepository("jdbc:sqlite:" + db.getAbsolutePath());

        repo.guardar("ana", disco("Uno", "Grupo A"));
        repo.guardar("ana", disco("Uno", "Grupo A"));
        repo.guardar("luis", disco("Uno", "Grupo A"));

        assertEquals(1, repo.contar("ana"));
        assertEquals(1, repo.contar("luis"));
    }

    private Disco disco(String titulo, String artista) {
        return new Disco(titulo, artista, 2001, "Rock", "CD");
    }
}
