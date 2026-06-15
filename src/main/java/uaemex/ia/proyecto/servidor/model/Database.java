package uaemex.ia.proyecto.servidor.model;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import uaemex.ia.proyecto.compartido.Disco;

import java.io.*;
import java.lang.reflect.Type;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Database {

    private static final Logger LOGGER = Logger.getLogger(Database.class.getName());
    private static final String ARCHIVO = "data/coleccion.json";
    private static volatile Database instancia;

    private final Gson gson;
    private final List<Disco> coleccion;

    private Database() {
        gson = new GsonBuilder().setPrettyPrinting().create();
        coleccion = cargarDesdeArchivo();
        LOGGER.info(() -> "Coleccion cargada: " + coleccion.size() + " disco(s).");
    }

    // Double-checked locking para singleton thread-safe
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

    public synchronized void guardar(Disco disco) {
        coleccion.add(disco);
        persistir();
        LOGGER.info(() -> "Disco guardado: " + disco);
    }

    public synchronized List<Disco> obtenerTodos() {
        return new ArrayList<>(coleccion);
    }

    private List<Disco> cargarDesdeArchivo() {
        File archivo = new File(ARCHIVO);
        if (!archivo.exists()) {
            new File("data").mkdirs();
            return new ArrayList<>();
        }
        try (Reader reader = new FileReader(archivo)) {
            Type tipo = new TypeToken<List<Disco>>() {}.getType();
            List<Disco> lista = gson.fromJson(reader, tipo);
            return lista != null ? lista : new ArrayList<>();
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "Error al cargar la coleccion.", e);
            return new ArrayList<>();
        }
    }

    private void persistir() {
        File directorio = new File("data");
        directorio.mkdirs();
        Path destino = new File(ARCHIVO).toPath();
        Path temporal = new File(directorio, "coleccion.json.tmp").toPath();

        try (Writer writer = new FileWriter(temporal.toFile())) {
            gson.toJson(coleccion, writer);
            writer.flush();
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "Error al escribir el archivo temporal de la coleccion.", e);
            return;
        }

        try {
            Files.move(temporal, destino,
                    StandardCopyOption.REPLACE_EXISTING,
                    StandardCopyOption.ATOMIC_MOVE);
        } catch (AtomicMoveNotSupportedException e) {
            LOGGER.log(Level.WARNING, "Movimiento atomico no soportado; intentando reemplazo seguro.", e);
            reemplazarSinAtomicMove(temporal, destino);
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "Error al reemplazar la coleccion con el archivo temporal.", e);
        }
    }

    private void reemplazarSinAtomicMove(Path temporal, Path destino) {
        try {
            if (Files.exists(temporal)) {
                Files.move(temporal, destino, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "Error al persistir la coleccion.", e);
        }
    }
}
