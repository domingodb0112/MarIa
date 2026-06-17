package uaemex.ia.proyecto.servidor.model;

import uaemex.ia.proyecto.compartido.Disco;
import uaemex.ia.proyecto.servidor.controller.DiscoKeys;
import uaemex.ia.proyecto.servidor.model.agentes.SimilarityUtils;

import java.io.File;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

final class SqliteAlbumRepository {
    private final String url;

    SqliteAlbumRepository(String url) { this(url, true); }

    SqliteAlbumRepository(String url, boolean bootstrapJson) {
        this.url = url;
        File parent = new File("data");
        parent.mkdirs();
        crearTabla();
        if (bootstrapJson) importarJsonSiEstaVacio();
    }

    synchronized void guardar(Disco disco) {
        String sql = "insert or ignore into discos(user_id,titulo,artista,anio,genero,formato,"
                + "titulo_key,artista_key,genero_key) values(?,?,?,?,?,?,?,?,?)";
        try (Connection c = conectar(); PreparedStatement ps = c.prepareStatement(sql)) {
            llenarDisco(ps, disco);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new IllegalStateException("No se pudo guardar disco en SQLite.", e);
        }
    }

    synchronized boolean existe(Disco disco) {
        String[] key = DiscoKeys.clave(disco).split("\\|", 2);
        String sql = "select 1 from discos where user_id=? and titulo_key=? and artista_key=? limit 1";
        try (Connection c = conectar(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, "default-user");
            ps.setString(2, key.length > 0 ? key[0] : "");
            ps.setString(3, key.length > 1 ? key[1] : "");
            try (ResultSet rs = ps.executeQuery()) { return rs.next(); }
        } catch (SQLException e) {
            throw new IllegalStateException("No se pudo validar duplicados en SQLite.", e);
        }
    }

    synchronized List<Disco> listar() {
        return listarPagina(0, Integer.MAX_VALUE);
    }

    synchronized List<Disco> listarPagina(int pagina, int tamano) {
        String sql = "select titulo,artista,anio,genero,formato from discos where user_id='default-user'"
                + " order by id limit ? offset ?";
        try (Connection c = conectar(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, tamano);
            ps.setInt(2, Math.max(0, pagina) * Math.max(1, tamano));
            return leerDiscos(ps);
        } catch (SQLException e) {
            throw new IllegalStateException("No se pudo listar coleccion SQLite.", e);
        }
    }

    synchronized List<Disco> buscarCandidatos(String consulta, int limite) {
        String q = "%" + SimilarityUtils.normalizar(consulta) + "%";
        String sql = "select titulo,artista,anio,genero,formato from discos where user_id='default-user'"
                + " and (titulo_key like ? or artista_key like ? or genero_key like ?) order by id limit ?";
        try (Connection c = conectar(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, q);
            ps.setString(2, q);
            ps.setString(3, q);
            ps.setInt(4, limite);
            return leerDiscos(ps);
        } catch (SQLException e) {
            throw new IllegalStateException("No se pudo buscar en SQLite.", e);
        }
    }

    synchronized int contar() {
        try (Connection c = conectar();
             PreparedStatement ps = c.prepareStatement("select count(*) from discos where user_id='default-user'")) {
            try (ResultSet rs = ps.executeQuery()) { return rs.next() ? rs.getInt(1) : 0; }
        } catch (SQLException e) {
            throw new IllegalStateException("No se pudo contar coleccion SQLite.", e);
        }
    }

    private void crearTabla() {
        String sql = "create table if not exists discos(id integer primary key autoincrement,"
                + "user_id text not null,titulo text,artista text,anio integer,genero text,formato text,"
                + "titulo_key text not null,artista_key text not null,genero_key text not null default '',"
                + "unique(user_id,titulo_key,artista_key))";
        try (Connection c = conectar(); Statement st = c.createStatement()) {
            st.execute(sql);
            asegurarGeneroKey(c);
            st.execute("create index if not exists idx_discos_busqueda on discos(user_id,titulo_key,artista_key,genero_key)");
        } catch (SQLException e) {
            throw new IllegalStateException("No se pudo crear la tabla discos.", e);
        }
    }

    private void importarJsonSiEstaVacio() {
        if (contar() > 0) return;
        for (Disco disco : JsonCollectionBootstrap.cargar()) guardar(disco);
    }

    private Connection conectar() throws SQLException {
        return DriverManager.getConnection(url);
    }

    private void asegurarGeneroKey(Connection c) throws SQLException {
        try (ResultSet rs = c.getMetaData().getColumns(null, null, "discos", "genero_key")) {
            if (!rs.next()) {
                try (Statement st = c.createStatement()) {
                    st.execute("alter table discos add column genero_key text not null default ''");
                }
            }
        }
        try (Statement st = c.createStatement()) {
            st.execute("update discos set genero_key=lower(genero) where genero_key=''");
        }
    }

    private void llenarDisco(PreparedStatement ps, Disco d) throws SQLException {
        String[] key = DiscoKeys.clave(d).split("\\|", 2);
        ps.setString(1, "default-user");
        ps.setString(2, d.getTitulo());
        ps.setString(3, d.getArtista());
        ps.setInt(4, d.getAnio());
        ps.setString(5, d.getGenero());
        ps.setString(6, d.getFormato());
        ps.setString(7, key.length > 0 ? key[0] : "");
        ps.setString(8, key.length > 1 ? key[1] : "");
        ps.setString(9, SimilarityUtils.normalizar(d.getGenero()));
    }

    private List<Disco> leerDiscos(PreparedStatement ps) throws SQLException {
        List<Disco> discos = new ArrayList<>();
        try (ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                discos.add(new Disco(rs.getString(1), rs.getString(2), rs.getInt(3),
                        rs.getString(4), rs.getString(5)));
            }
        }
        return discos;
    }
}
