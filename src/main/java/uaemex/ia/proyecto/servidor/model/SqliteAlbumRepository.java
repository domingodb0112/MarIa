package uaemex.ia.proyecto.servidor.model;

import uaemex.ia.proyecto.compartido.Disco;
import uaemex.ia.proyecto.servidor.controller.DiscoKeys;

import java.io.File;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

final class SqliteAlbumRepository {
    private final String url;

    SqliteAlbumRepository(String url) {
        this.url = url;
        File parent = new File("data");
        parent.mkdirs();
        crearTabla();
        importarJsonSiEstaVacio();
    }

    synchronized void guardar(String userId, Disco disco) {
        String sql = "insert or ignore into discos(user_id,titulo,artista,anio,genero,formato,titulo_key,artista_key)"
                + " values(?,?,?,?,?,?,?,?)";
        try (Connection c = conectar(); PreparedStatement ps = c.prepareStatement(sql)) {
            llenarDisco(ps, userId, disco);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new IllegalStateException("No se pudo guardar disco en SQLite.", e);
        }
    }

    synchronized List<Disco> listar(String userId) {
        return listarPagina(userId, 0, Integer.MAX_VALUE);
    }

    synchronized List<Disco> listarPagina(String userId, int pagina, int tamano) {
        String sql = "select titulo,artista,anio,genero,formato from discos where user_id=?"
                + " order by id limit ? offset ?";
        try (Connection c = conectar(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, userId);
            ps.setInt(2, tamano);
            ps.setInt(3, Math.max(0, pagina) * Math.max(1, tamano));
            return leerDiscos(ps);
        } catch (SQLException e) {
            throw new IllegalStateException("No se pudo listar coleccion SQLite.", e);
        }
    }

    synchronized int contar(String userId) {
        try (Connection c = conectar();
             PreparedStatement ps = c.prepareStatement("select count(*) from discos where user_id=?")) {
            ps.setString(1, userId);
            try (ResultSet rs = ps.executeQuery()) { return rs.next() ? rs.getInt(1) : 0; }
        } catch (SQLException e) {
            throw new IllegalStateException("No se pudo contar coleccion SQLite.", e);
        }
    }

    private void crearTabla() {
        String sql = "create table if not exists discos(id integer primary key autoincrement,"
                + "user_id text not null,titulo text,artista text,anio integer,genero text,formato text,"
                + "titulo_key text not null,artista_key text not null,"
                + "unique(user_id,titulo_key,artista_key))";
        try (Connection c = conectar(); Statement st = c.createStatement()) {
            st.execute(sql);
        } catch (SQLException e) {
            throw new IllegalStateException("No se pudo crear la tabla discos.", e);
        }
    }

    private void importarJsonSiEstaVacio() {
        if (contar("default-user") > 0) return;
        for (Disco disco : JsonCollectionBootstrap.cargar()) guardar("default-user", disco);
    }

    private Connection conectar() throws SQLException {
        return DriverManager.getConnection(url);
    }

    private void llenarDisco(PreparedStatement ps, String userId, Disco d) throws SQLException {
        String[] key = DiscoKeys.clave(d).split("\\|", 2);
        ps.setString(1, normalizarUser(userId));
        ps.setString(2, d.getTitulo());
        ps.setString(3, d.getArtista());
        ps.setInt(4, d.getAnio());
        ps.setString(5, d.getGenero());
        ps.setString(6, d.getFormato());
        ps.setString(7, key.length > 0 ? key[0] : "");
        ps.setString(8, key.length > 1 ? key[1] : "");
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

    private String normalizarUser(String userId) {
        return userId == null || userId.trim().isEmpty() ? "default-user" : userId.trim();
    }
}
