package uaemex.ia.proyecto.servidor.model.agentes;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import uaemex.ia.proyecto.compartido.Disco;
import java.io.*;
import java.lang.reflect.Type;
import java.nio.file.*;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class RecomendadorStorage {
    private static final Logger LOGGER = Logger.getLogger(RecomendadorStorage.class.getName());
    private static final String ARCHIVO_CATALOGO = "data/catalogo.json";
    private static final String ARCHIVO_APRENDIZAJE = "data/recommendation_learning.json";
    private static final String ARCHIVO_HISTORIAL = "data/recommendation_history.json";
    private static final Gson GSON = new Gson();

    private RecomendadorStorage() {}

    public static List<Disco> cargarCatalogo() {
        File file = new File(ARCHIVO_CATALOGO);
        if (!file.exists()) {
            LOGGER.warning(() -> "No se encontro " + ARCHIVO_CATALOGO + ". Recomendador sin catalogo.");
            return Collections.emptyList();
        }
        try (Reader reader = new FileReader(file)) {
            Type type = new TypeToken<List<Disco>>() {}.getType();
            List<Disco> list = GSON.fromJson(reader, type);
            return list != null ? Collections.unmodifiableList(new ArrayList<>(list)) : Collections.emptyList();
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "Error al cargar catalogo.", e);
            return Collections.emptyList();
        }
    }

    public static Map<String, BrazoRecomendacion> cargarAprendizaje() {
        File file = new File(ARCHIVO_APRENDIZAJE);
        if (!file.exists()) return new HashMap<>();
        try (Reader reader = new FileReader(file)) {
            Type type = new TypeToken<Map<String, BrazoRecomendacion>>() {}.getType();
            Map<String, BrazoRecomendacion> map = GSON.fromJson(reader, type);
            return map != null ? new HashMap<>(map) : new HashMap<>();
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "Error al cargar aprendizaje.", e);
            return new HashMap<>();
        }
    }

    public static synchronized void guardarAprendizaje(Map<String, BrazoRecomendacion> map) {
        try {
            escribirAtomico(ARCHIVO_APRENDIZAJE, map);
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "Error al guardar aprendizaje.", e);
        }
    }

    public static List<HistorialRecomendacion> cargarHistorial() {
        File file = new File(ARCHIVO_HISTORIAL);
        if (!file.exists()) return new ArrayList<>();
        try (Reader reader = new FileReader(file)) {
            Type type = new TypeToken<List<HistorialRecomendacion>>() {}.getType();
            List<HistorialRecomendacion> list = GSON.fromJson(reader, type);
            return list != null ? new ArrayList<>(list) : new ArrayList<>();
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "Error al cargar historial.", e);
            return new ArrayList<>();
        }
    }

    public static synchronized void guardarHistorial(List<HistorialRecomendacion> history) {
        try {
            escribirAtomico(ARCHIVO_HISTORIAL, history);
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "Error al guardar historial.", e);
        }
    }

    private static void escribirAtomico(String ruta, Object datos) throws IOException {
        Path destino = Paths.get(ruta);
        Files.createDirectories(destino.toAbsolutePath().getParent());
        Path temporal = destino.resolveSibling(destino.getFileName() + ".tmp");
        try (Writer writer = Files.newBufferedWriter(temporal)) {
            GSON.toJson(datos, writer);
        }
        try {
            Files.move(temporal, destino, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
        } catch (AtomicMoveNotSupportedException e) {
            Files.move(temporal, destino, StandardCopyOption.REPLACE_EXISTING);
        } finally {
            Files.deleteIfExists(temporal);
        }
    }
}
