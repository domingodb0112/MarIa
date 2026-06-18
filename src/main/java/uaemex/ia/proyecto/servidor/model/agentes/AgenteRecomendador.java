package uaemex.ia.proyecto.servidor.model.agentes;

import uaemex.ia.proyecto.compartido.Disco;
import uaemex.ia.proyecto.servidor.model.PerfilGustos;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class AgenteRecomendador {
    private static final int MAX_RECOMENDACIONES = 8;
    private static final double PESO_APRENDIZAJE = 20.0;
    private static final double EPSILON_EXPLORACION = 0.15; // 15% de probabilidad de explorar opciones aleatorias
    private static final List<Disco> CATALOGO_CACHE = RecomendadorStorage.cargarCatalogo();
    private static final AgenteRecomendador INSTANCE = new AgenteRecomendador();

    private final List<Disco> catalogoClasico = CATALOGO_CACHE;
    private final Map<String, Map<String, BrazoRecomendacion>> aprendizajePorUsuario = new HashMap<>();
    private final Map<String, List<HistorialRecomendacion>> historialPorUsuario = new HashMap<>();
    private final Random random;
    private final boolean persistir;
    private AgenteRecomendador() { this(new Random(), true); }
    AgenteRecomendador(Random random) { this(random, false); }
    AgenteRecomendador(Random random, boolean persistir) {
        this.random = random;
        this.persistir = persistir;
    }
    public static AgenteRecomendador getInstance() { return INSTANCE; }
    public synchronized List<Disco> recomendar(PerfilGustos perfil, List<Disco> coleccionUsuario) {
        return recomendar("default-user", perfil, coleccionUsuario);
    }
    public synchronized List<Disco> recomendar(String userId, PerfilGustos perfil, List<Disco> coleccionUsuario) {
        if (coleccionUsuario == null) coleccionUsuario = Collections.emptyList();
        // Se crea un conjunto de claves para no recomendar discos que el usuario ya posee
        Set<String> discosExistentes = crearIndiceColeccion(coleccionUsuario);
        PerfilAfinidad afinidad = new PerfilAfinidad(coleccionUsuario);
        
        // Puntuamos todos los discos candidatos del catalogo general que no estan en la coleccion
        List<DiscoPuntuado> candidatos = IntStream.range(0, catalogoClasico.size())
                .filter(i -> !discosExistentes.contains(claveDisco(catalogoClasico.get(i))))
                .mapToObj(i -> new DiscoPuntuado(catalogoClasico.get(i),
                        calcularPuntaje(userId, perfil, afinidad, catalogoClasico.get(i), i)))
                .collect(Collectors.toList());

        // Diversificamos las recomendaciones para no agrupar demasiados discos de un solo artista
        List<Disco> recomendaciones = seleccionarDiversasPorArtista(candidatos, afinidad);
        // Almacenamos estas recomendaciones en el historial reciente para activar el control de fatiga
        registrarHistorial(userId, recomendaciones);
        return recomendaciones;
    }
    private List<Disco> seleccionarDiversasPorArtista(List<DiscoPuntuado> candidatos, PerfilAfinidad afinidad) {
        Map<String, Integer> artistasSeleccionados = new HashMap<>();
        return candidatos.stream()
                .sorted(Comparator.comparingDouble(DiscoPuntuado::getPuntaje).reversed()
                        .thenComparing(d -> d.getDisco().getGenero())
                        .thenComparing(d -> d.getDisco().getTitulo()))
                .filter(d -> !afinidad.artistaSaturado(d.getDisco(), artistasSeleccionados))
                .peek(d -> artistasSeleccionados.merge(
                        SimilarityUtils.normalizar(d.getDisco().getArtista()), 1, Integer::sum))
                .limit(MAX_RECOMENDACIONES)
                .map(DiscoPuntuado::getDisco)
                .collect(Collectors.toList());
    }
    public synchronized void registrarRetroalimentacion(Disco disco, boolean aceptada) {
        registrarRetroalimentacion("default-user", disco, aceptada);
    }
    public synchronized void registrarRetroalimentacion(String userId, Disco disco, boolean aceptada) {
        if (disco == null || disco.getGenero() == null || disco.getGenero().trim().isEmpty()) return;
        Map<String, BrazoRecomendacion> aprendizaje = aprendizaje(userId);
        
        // Extraemos claves normalizadas para asociar los premios o castigos en el bandido multibrazo
        String genero = "genero:" + SimilarityUtils.normalizar(disco.getGenero());
        String artista = "artista:" + SimilarityUtils.normalizar(disco.getArtista());
        String decada = "decada:" + SimilarityUtils.decada(disco.getAnio());
        
        // Se registra el feedback (+1.0 si es aceptado, -1.0 si es rechazado) en cada brazo/dimensión
        registrarSenalAprendizaje(aprendizaje, genero, aceptada);
        registrarSenalAprendizaje(aprendizaje, artista, aceptada);
        registrarSenalAprendizaje(aprendizaje, decada, aceptada);
        
        // Se guarda persistentemente el estado del aprendizaje del recomendador
        if (persistir) RecomendadorStorage.guardarAprendizaje(userId, aprendizaje);
    }
    private void registrarSenalAprendizaje(Map<String, BrazoRecomendacion> aprendizaje, String clave, boolean aceptada) {
        if (clave.endsWith(":") || clave.endsWith(":0")) return;
        aprendizaje.computeIfAbsent(clave, g -> new BrazoRecomendacion()).registrar(aceptada ? 1.0 : -1.0);
    }
    private double calcularPuntaje(String userId, PerfilGustos perfil, PerfilAfinidad afinidad, Disco disco, int posicionCatalogo) {
        String genero = SimilarityUtils.normalizar(disco.getGenero());
        
        // Lógica de Exploración vs Explotación (Epsilon-Greedy): con un 15% de azar, agregamos ruido exploratorio
        double exploracion = random.nextDouble() < EPSILON_EXPLORACION ? random.nextDouble() * 3.0 : 0.0;
        
        // Refuerzo aprendido: el conocimiento extraído del historial de clics (Aceptar/Rechazar) del usuario
        double refuerzo = refuerzoAprendido(userId, disco, genero);
        
        // Afinidades estáticas: similitud de artista y década inferida a partir de lo que el usuario ya tiene físicamente
        double afinidadHistorica = afinidad.afinidadArtista(disco) + afinidad.afinidadDecada(disco);
        
        // Penalización por fatiga: restamos puntaje si el disco o artista se recomendó recientemente
        double penalizacionReciente = penalizacionHistorial(userId, disco);

        // Si el usuario no tiene discos en su colección física, usamos el orden predeterminado del catálogo como base
        if (perfil == null || perfil.getTotalDiscos() == 0) {
            return 1.0 - (posicionCatalogo * 0.01) + refuerzo + exploracion - penalizacionReciente;
        }

        // Puntuamos segun la representacion porcentual del genero en su coleccion fisica
        double porcentajeGenero = perfil.getPorcentajePorGenero() == null ? 0.0 :
                perfil.getPorcentajePorGenero().entrySet().stream()
                        .filter(e -> SimilarityUtils.normalizar(e.getKey()).equals(genero))
                        .mapToDouble(Map.Entry::getValue).findFirst().orElse(0.0);

        // Bonos para guiar la recomendacion: bono extra si es su genero mas consumido, o si exploramos generos nuevos
        double bonoGeneroFavorito = SimilarityUtils.normalizar(perfil.getGeneroFavorito()).equals(genero) ? 15.0 : 0.0;
        double bonoDiversidad = porcentajeGenero == 0.0 ? 5.0 : 0.0;
        double desempateCatalogo = 1.0 - (posicionCatalogo * 0.01);
        
        // La suma de todas las señales produce el puntaje del candidato
        return porcentajeGenero + bonoGeneroFavorito + bonoDiversidad + desempateCatalogo
                + refuerzo + exploracion + afinidadHistorica - penalizacionReciente;
    }
    private double refuerzoAprendido(String userId, Disco disco, String genero) {
        return (valorAprendido(userId, "genero:" + genero)
                + valorAprendido(userId, genero)
                + valorAprendido(userId, "artista:" + SimilarityUtils.normalizar(disco.getArtista()))
                + valorAprendido(userId, "decada:" + SimilarityUtils.decada(disco.getAnio()))) * PESO_APRENDIZAJE;
    }
    double valorAprendido(String userId, String clave) {
        return aprendizaje(userId).getOrDefault(clave, new BrazoRecomendacion()).valorEsperado();
    }
    private double penalizacionHistorial(String userId, Disco disco) {
        String clave = claveDisco(disco);
        String artista = SimilarityUtils.normalizar(disco.getArtista());
        int decada = SimilarityUtils.decada(disco.getAnio());
        // Establecemos una ventana temporal de fatiga de 7 dias
        long reciente = System.currentTimeMillis() - (7L * 24 * 60 * 60 * 1000);
        
        // Iteramos el historial y penalizamos con fuerza los discos o artistas repetidos recientemente
        return historial(userId).stream().filter(h -> h.getTimestamp() >= reciente)
                .mapToDouble(h -> {
                    if (clave.equals(h.getClaveDisco())) return 18.0; // Fuerte castigo si es exactamente el mismo disco
                    double pen = artista.equals(h.getArtistaNormalizado()) ? 3.0 : 0.0; // Castigo leve si es el mismo artista
                    return decada > 0 && decada == h.getDecada() ? pen + 1.0 : pen; // Castigo por misma decada
                }).sum();
    }
    private Set<String> crearIndiceColeccion(List<Disco> coleccionUsuario) {
        Set<String> indice = new HashSet<>();
        for (Disco disco : coleccionUsuario) indice.add(claveDisco(disco));
        return indice;
    }
    private String claveDisco(Disco disco) {
        return SimilarityUtils.normalizar(disco.getTitulo()) + "|" + SimilarityUtils.normalizar(disco.getArtista());
    }
    private void registrarHistorial(String userId, List<Disco> recomendaciones) {
        List<HistorialRecomendacion> historial = historial(userId);
        for (Disco disco : recomendaciones) {
            historial.add(new HistorialRecomendacion(disco, claveDisco(disco)));
        }
        // Limitar el tamaño del historial persistente a los ultimos 200 registros
        while (historial.size() > 200) historial.remove(0);
        if (persistir) RecomendadorStorage.guardarHistorial(userId, historial);
    }
    private Map<String, BrazoRecomendacion> aprendizaje(String userId) {
        if (!persistir) return aprendizajePorUsuario.computeIfAbsent(userId, id -> new HashMap<>());
        return aprendizajePorUsuario.computeIfAbsent(userId, RecomendadorStorage::cargarAprendizaje);
    }
    private List<HistorialRecomendacion> historial(String userId) {
        if (!persistir) return historialPorUsuario.computeIfAbsent(userId, id -> new ArrayList<>());
        return historialPorUsuario.computeIfAbsent(userId, RecomendadorStorage::cargarHistorial);
    }
}
