package uaemex.ia.proyecto.servidor.model.agentes;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import uaemex.ia.proyecto.compartido.Disco;
import uaemex.ia.proyecto.servidor.model.PerfilGustos;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.Type;
import java.util.Comparator;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Agente que recomienda discos del catalogo externo segun el perfil del usuario.
 */
public class AgenteRecomendador {

    private static final Logger LOGGER = Logger.getLogger(AgenteRecomendador.class.getName());
    private static final int MAX_RECOMENDACIONES = 8;
    private static final String ARCHIVO_CATALOGO = "data/catalogo.json";
    private static final String ARCHIVO_APRENDIZAJE = "data/recommendation_learning.json";
    private static final double PESO_APRENDIZAJE = 20.0;
    private static final double EPSILON_EXPLORACION = 0.15;
    private static final Random RANDOM = new Random();
    // Static cache for the catalog to avoid parsing JSON and reading files on every client connection
    private static final List<Disco> CATALOGO_CACHE = cargarCatalogoEstatico();

    private final Gson gson = new Gson();
    private final List<Disco> catalogoClasico;
    private final Map<String, BrazoRecomendacion> aprendizajePorGenero;

    /**
     * Carga el catalogo base de recomendaciones al crear el agente.
     */
    public AgenteRecomendador() {
        this.catalogoClasico = CATALOGO_CACHE;
        this.aprendizajePorGenero = cargarAprendizaje();
    }

    /**
     * Genera recomendaciones evitando discos que el usuario ya tiene registrados.
     *
     * @param perfil perfil de gustos calculado por el analizador.
     * @param coleccionUsuario coleccion actual del usuario.
     * @return lista limitada de discos recomendados.
     */
    public List<Disco> recomendar(PerfilGustos perfil, List<Disco> coleccionUsuario) {
        Set<String> discosExistentes = crearIndiceColeccion(coleccionUsuario);
        List<DiscoPuntuado> candidatos;
        synchronized (aprendizajePorGenero) {
            candidatos = IntStream.range(0, catalogoClasico.size())
                    .filter(i -> !discosExistentes.contains(claveDisco(catalogoClasico.get(i))))
                    .mapToObj(i -> new DiscoPuntuado(catalogoClasico.get(i), calcularPuntaje(perfil, catalogoClasico.get(i), i)))
                    .collect(Collectors.toList());
        }

        return candidatos.stream()
                .sorted(Comparator.comparingDouble(DiscoPuntuado::getPuntaje).reversed()
                        .thenComparing(d -> d.getDisco().getGenero())
                        .thenComparing(d -> d.getDisco().getTitulo()))
                .limit(MAX_RECOMENDACIONES)
                .map(DiscoPuntuado::getDisco)
                .collect(Collectors.toList());
    }

    /**
     * Registra retroalimentacion del usuario sobre una recomendacion y persiste el aprendizaje.
     *
     * @param disco recomendacion aceptada o rechazada.
     * @param aceptada true cuando el usuario acepta la sugerencia.
     */
    public void registrarRetroalimentacion(Disco disco, boolean aceptada) {
        if (disco == null || disco.getGenero() == null || disco.getGenero().trim().isEmpty()) {
            return;
        }

        String genero = SimilarityUtils.normalizar(disco.getGenero());
        synchronized (aprendizajePorGenero) {
            aprendizajePorGenero
                    .computeIfAbsent(genero, g -> new BrazoRecomendacion())
                    .registrar(aceptada ? 1.0 : -1.0);
            persistirAprendizaje();
        }
    }

    /**
     * Carga el catalogo de discos recomendables de manera estatica y thread-safe.
     *
     * @return lista inmutable con el catalogo cargado o vacia si hay fallos.
     */
    private static List<Disco> cargarCatalogoEstatico() {
        File archivo = new File(ARCHIVO_CATALOGO);
        if (!archivo.exists()) {
            LOGGER.warning(() -> "No se encontro " + ARCHIVO_CATALOGO
                    + ". El recomendador no tendra catalogo externo.");
            return java.util.Collections.emptyList();
        }

        try (Reader reader = new FileReader(archivo)) {
            Gson gson = new Gson();
            Type tipoLista = new TypeToken<List<Disco>>() {}.getType();
            List<Disco> catalogo = gson.fromJson(reader, tipoLista);
            if (catalogo != null) {
                LOGGER.info(() -> "Catalogo de recomendaciones cargado en cache: "
                        + catalogo.size() + " album(es).");
                return java.util.Collections.unmodifiableList(new ArrayList<>(catalogo));
            }
            return java.util.Collections.emptyList();
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "No se pudo cargar el catalogo de recomendaciones.", e);
            return java.util.Collections.emptyList();
        }
    }

    /**
     * Calcula el puntaje de un disco candidato frente al perfil del usuario.
     *
     * @param perfil perfil calculado con la coleccion del usuario.
     * @param disco disco candidato del catalogo.
     * @param posicionCatalogo posicion original en el archivo para desempate.
     * @return puntaje mayor para candidatos mas recomendables.
     */
    private double calcularPuntaje(PerfilGustos perfil, Disco disco, int posicionCatalogo) {
        String genero = SimilarityUtils.normalizar(disco.getGenero());
        double exploracion = RANDOM.nextDouble() < EPSILON_EXPLORACION ? RANDOM.nextDouble() * 3.0 : 0.0;
        double refuerzo = aprendizajePorGenero
                .getOrDefault(genero, new BrazoRecomendacion())
                .valorEsperado() * PESO_APRENDIZAJE;

        if (perfil == null || perfil.getTotalDiscos() == 0) {
            return 1.0 - (posicionCatalogo * 0.01) + refuerzo + exploracion;
        }

        double porcentajeGenero = perfil == null || perfil.getPorcentajePorGenero() == null ? 0.0 :
                perfil.getPorcentajePorGenero().entrySet().stream()
                        .filter(e -> SimilarityUtils.normalizar(e.getKey()).equals(genero))
                        .mapToDouble(Map.Entry::getValue)
                        .findFirst().orElse(0.0);

        // Combina afinidad por genero favorito, diversidad y desempate del catalogo.
        double bonoGeneroFavorito = SimilarityUtils.normalizar(perfil.getGeneroFavorito()).equals(genero)
                ? 15.0 : 0.0;
        double bonoDiversidad = porcentajeGenero == 0.0 ? 5.0 : 0.0;
        double desempateCatalogo = 1.0 - (posicionCatalogo * 0.01);
        return porcentajeGenero + bonoGeneroFavorito + bonoDiversidad + desempateCatalogo + refuerzo + exploracion;
    }

    /**
     * Construye un indice de discos existentes para evitar recomendaciones duplicadas.
     *
     * @param coleccionUsuario discos actuales del usuario.
     * @return claves normalizadas titulo-artista.
     */
    private Set<String> crearIndiceColeccion(List<Disco> coleccionUsuario) {
        Set<String> indice = new HashSet<>();
        for (Disco disco : coleccionUsuario) {
            indice.add(claveDisco(disco));
        }
        return indice;
    }

    /**
     * Genera una clave comparable para identificar un disco por titulo y artista.
     *
     * @param disco disco a indexar.
     * @return clave normalizada.
     */
    private String claveDisco(Disco disco) {
        return SimilarityUtils.normalizar(disco.getTitulo()) + "|"
                + SimilarityUtils.normalizar(disco.getArtista());
    }

    /**
     * Lee el estado aprendido por genero desde el archivo local de aprendizaje.
     *
     * @return mapa mutable de brazos del bandido.
     */
    private Map<String, BrazoRecomendacion> cargarAprendizaje() {
        File archivo = new File(ARCHIVO_APRENDIZAJE);
        if (!archivo.exists()) {
            return new HashMap<>();
        }

        try (Reader reader = new FileReader(archivo)) {
            Type tipo = new TypeToken<Map<String, BrazoRecomendacion>>() {}.getType();
            Map<String, BrazoRecomendacion> estado = gson.fromJson(reader, tipo);
            return estado != null ? new HashMap<>(estado) : new HashMap<>();
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "No se pudo cargar el aprendizaje de recomendaciones.", e);
            return new HashMap<>();
        }
    }

    /**
     * Persiste los valores aprendidos del bandido multi-brazo en JSON local.
     */
    private void persistirAprendizaje() {
        File directorio = new File("data");
        directorio.mkdirs();
        try (Writer writer = new FileWriter(ARCHIVO_APRENDIZAJE)) {
            gson.toJson(aprendizajePorGenero, writer);
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "No se pudo persistir el aprendizaje de recomendaciones.", e);
        }
    }

    // BrazoRecomendacion moved to its own file: BrazoRecomendacion.java
}
