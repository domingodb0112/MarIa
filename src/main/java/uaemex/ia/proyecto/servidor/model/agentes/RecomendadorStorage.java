package uaemex.ia.proyecto.servidor.model.agentes;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import uaemex.ia.proyecto.compartido.Disco;
import java.io.*;
import java.lang.reflect.Type;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Gestor de persistencia del Agente Recomendador.
 * Lee y escribe en archivos locales el catálogo externo, el historial y el aprendizaje por refuerzo.
 */
public final class RecomendadorStorage {
    private static final Logger LOGGER = Logger.getLogger(RecomendadorStorage.class.getName());
    private static final String ARCHIVO_CATALOGO = "data/catalogo.json";
    private static final String ARCHIVO_APRENDIZAJE = "data/recommendation_learning.json";
    private static final String ARCHIVO_HISTORIAL = "data/recommendation_history.json";
    private static final Gson GSON = new Gson();

    private RecomendadorStorage() {}

    /**
     * Carga el catálogo estático externo de discos recomendables.
     *
     * @return lista inmutable de discos cargados.
     */
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

    /**
     * Carga el aprendizaje acumulado del recomendador (bandidos multibrazo).
     *
     * @return mapa mutable de género/decada/artista y sus puntuaciones.
     */
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

    /**
     * Guarda el aprendizaje acumulado del recomendador de manera atómica.
     *
     * @param map mapa de aprendizaje a guardar.
     */
    public static void guardarAprendizaje(Map<String, BrazoRecomendacion> map) {
        new File("data").mkdirs();
        try (Writer writer = new FileWriter(ARCHIVO_APRENDIZAJE)) {
            GSON.toJson(map, writer);
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "Error al guardar aprendizaje.", e);
        }
    }

    /**
     * Carga el historial de recomendaciones recientemente emitidas para evitar redundancia.
     *
     * @return lista de registros de historial.
     */
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

    /**
     * Guarda el historial de recomendaciones de manera atómica.
     *
     * @param history lista de registros a guardar.
     */
    public static void guardarHistorial(List<HistorialRecomendacion> history) {
        new File("data").mkdirs();
        try (Writer writer = new FileWriter(ARCHIVO_HISTORIAL)) {
            GSON.toJson(history, writer);
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "Error al guardar historial.", e);
        }
    }
}
