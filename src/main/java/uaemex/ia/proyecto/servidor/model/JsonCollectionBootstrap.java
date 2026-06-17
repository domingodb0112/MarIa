package uaemex.ia.proyecto.servidor.model;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import uaemex.ia.proyecto.compartido.Disco;

import java.io.File;
import java.io.FileReader;
import java.io.Reader;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

final class JsonCollectionBootstrap {
    private static final Logger LOGGER = Logger.getLogger(JsonCollectionBootstrap.class.getName());
    private static final String ARCHIVO = "data/coleccion.json";
    private static final Gson GSON = new Gson();

    private JsonCollectionBootstrap() {}

    static List<Disco> cargar() {
        File file = new File(ARCHIVO);
        if (!file.exists()) return new ArrayList<>();
        try (Reader reader = new FileReader(file)) {
            Type type = new TypeToken<List<Disco>>() {}.getType();
            List<Disco> discos = GSON.fromJson(reader, type);
            return discos == null ? new ArrayList<>() : discos;
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "No se pudo importar la coleccion JSON inicial.", e);
            return new ArrayList<>();
        }
    }
}
