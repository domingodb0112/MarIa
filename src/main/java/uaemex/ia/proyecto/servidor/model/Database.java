package uaemex.ia.proyecto.servidor.model;

import uaemex.ia.proyecto.compartido.Disco;

import java.util.List;
import java.util.logging.Logger;

/**
 * Fachada singleton para la coleccion escolar persistida en SQLite.
 */
public class Database {

    private static final Logger LOGGER = Logger.getLogger(Database.class.getName());
    private static volatile Database instancia;

    private final SqliteAlbumRepository repositorio;

    /**
     * Carga la coleccion desde disco al crear la instancia unica.
     */
    private Database() {
        repositorio = new SqliteAlbumRepository(DbConfig.url());
        LOGGER.info(() -> "Base SQLite lista. Coleccion: " + repositorio.contar() + " disco(s).");
    }

    /**
     * Obtiene la instancia unica de la base local.
     * Usa double-checked locking para inicializar de forma segura en ambiente multihilo.
     *
     * @return repositorio compartido.
     */
    public static Database getInstance() {
        if (instancia == null) {
            synchronized (Database.class) {
                if (instancia == null) {
                    instancia = new Database();
                }
            }
        }
        return instancia;
    }

    /**
     * Agrega un disco a la coleccion y persiste inmediatamente el archivo JSON.
     *
     * @param disco disco a guardar.
     */
    public synchronized void guardar(Disco disco) {
        repositorio.guardar(disco);
        LOGGER.info(() -> "Disco guardado: " + disco);
    }

    public synchronized void guardar(String userId, Disco disco) {
        guardar(disco);
    }

    /**
     * Devuelve una copia de la coleccion para evitar modificaciones externas directas.
     *
     * @return lista nueva con los discos actuales.
     */
    public synchronized List<Disco> obtenerTodos() {
        return obtenerTodos("default-user");
    }

    public synchronized List<Disco> obtenerTodos(String userId) {
        return repositorio.listar();
    }

    public synchronized List<Disco> obtenerPagina(String userId, int pagina, int tamano) {
        return repositorio.listarPagina(pagina, tamano);
    }

    public synchronized int contar(String userId) {
        return repositorio.contar();
    }

    public synchronized boolean existe(Disco disco) {
        return repositorio.existe(disco);
    }

    public synchronized List<Disco> buscarCandidatos(String consulta, int limite) {
        return repositorio.buscarCandidatos(consulta, limite);
    }
}
