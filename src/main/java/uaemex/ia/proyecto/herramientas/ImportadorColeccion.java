package uaemex.ia.proyecto.herramientas;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import uaemex.ia.proyecto.compartido.Disco;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Genera data/coleccion.json desde el catalogo curado externo.
 */
public class ImportadorColeccion {
    private static final Path CATALOGO = Paths.get("data", "catalogo.json");
    private static final Path SALIDA = Paths.get("data", "coleccion.json");
    private static final int LIMITE = 500;
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    public static void main(String[] args) throws IOException {
        Opciones opciones = Opciones.desde(args);
        List<Disco> catalogo = leerCatalogo(opciones.catalogo);
        List<Disco> seleccion = seleccionarBalanceado(catalogo, opciones.limite);
        escribirJson(opciones.salida, seleccion);
        System.out.println("Coleccion escrita en " + opciones.salida
                + " con " + seleccion.size() + " discos.");
    }

    private static List<Disco> leerCatalogo(Path ruta) throws IOException {
        Type tipo = new TypeToken<List<Disco>>() {}.getType();
        try (BufferedReader reader = Files.newBufferedReader(ruta, StandardCharsets.UTF_8)) {
            List<Disco> discos = GSON.fromJson(reader, tipo);
            return discos == null ? new ArrayList<>() : discos;
        }
    }

    private static List<Disco> seleccionarBalanceado(List<Disco> discos, int limite) {
        Map<String, List<Disco>> porGenero = agruparPorGenero(discos);
        List<Disco> seleccion = new ArrayList<>();
        int indice = 0;
        while (seleccion.size() < limite && seleccion.size() < discos.size()) {
            boolean agregado = false;
            for (List<Disco> grupo : porGenero.values()) {
                if (indice < grupo.size() && seleccion.size() < limite) {
                    seleccion.add(grupo.get(indice));
                    agregado = true;
                }
            }
            if (!agregado) break;
            indice++;
        }
        return seleccion;
    }

    private static Map<String, List<Disco>> agruparPorGenero(List<Disco> discos) {
        Map<String, List<Disco>> grupos = new LinkedHashMap<>();
        for (Disco disco : discos) {
            String genero = disco.getGenero() == null ? "Sin genero" : disco.getGenero();
            grupos.computeIfAbsent(genero, g -> new ArrayList<>()).add(disco);
        }
        return grupos;
    }

    private static void escribirJson(Path destino, List<Disco> discos) throws IOException {
        Path padre = destino.toAbsolutePath().getParent();
        if (padre != null) Files.createDirectories(padre);
        try (BufferedWriter writer = Files.newBufferedWriter(destino, StandardCharsets.UTF_8)) {
            GSON.toJson(discos, writer);
        }
    }

    private static class Opciones {
        private Path catalogo = CATALOGO;
        private Path salida = SALIDA;
        private int limite = LIMITE;

        private static Opciones desde(String[] args) {
            Opciones opciones = new Opciones();
            for (int i = 0; i < args.length; i++) {
                if ("--catalog".equals(args[i]) && i + 1 < args.length) {
                    opciones.catalogo = Paths.get(args[++i]);
                } else if ("--output".equals(args[i]) && i + 1 < args.length) {
                    opciones.salida = Paths.get(args[++i]);
                } else if ("--limit".equals(args[i]) && i + 1 < args.length) {
                    opciones.limite = Math.max(1, Integer.parseInt(args[++i]));
                }
            }
            return opciones;
        }
    }
}
