package uaemex.ia.proyecto.servidor.model.agentes;

import uaemex.ia.proyecto.compartido.Disco;
import uaemex.ia.proyecto.servidor.model.PerfilGustos;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class AgenteRecomendador {
    private static final int MAX_RECOMENDACIONES = 8;
    private static final double PESO_APRENDIZAJE = 20.0;
    private static final double EPSILON_EXPLORACION = 0.15;
    private static final Random RANDOM = new Random();
    private static final List<Disco> CATALOGO_CACHE = RecomendadorStorage.cargarCatalogo();
    private static final AgenteRecomendador INSTANCE = new AgenteRecomendador();

    private final List<Disco> catalogoClasico = CATALOGO_CACHE;
    private final Map<String, BrazoRecomendacion> aprendizajePorGenero;
    private final List<HistorialRecomendacion> historial;

    private AgenteRecomendador() {
        this.aprendizajePorGenero = RecomendadorStorage.cargarAprendizaje();
        this.historial = RecomendadorStorage.cargarHistorial();
    }

    public static AgenteRecomendador getInstance() {
        return INSTANCE;
    }

    public synchronized List<Disco> recomendar(PerfilGustos perfil, List<Disco> coleccionUsuario) {
        if (coleccionUsuario == null) coleccionUsuario = Collections.emptyList();
        Set<String> discosExistentes = crearIndiceColeccion(coleccionUsuario);
        PerfilAfinidad afinidad = new PerfilAfinidad(coleccionUsuario);
        List<DiscoPuntuado> candidatos;
        candidatos = IntStream.range(0, catalogoClasico.size())
                .filter(i -> !discosExistentes.contains(claveDisco(catalogoClasico.get(i))))
                .mapToObj(i -> new DiscoPuntuado(catalogoClasico.get(i),
                        calcularPuntaje(perfil, afinidad, catalogoClasico.get(i), i)))
                .collect(Collectors.toList());

        List<Disco> recomendaciones = seleccionarDiversasPorArtista(candidatos, afinidad);
        registrarHistorial(recomendaciones);
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
        if (disco == null || disco.getGenero() == null || disco.getGenero().trim().isEmpty()) return;
        String genero = "genero:" + SimilarityUtils.normalizar(disco.getGenero());
        String artista = "artista:" + SimilarityUtils.normalizar(disco.getArtista());
        String decada = "decada:" + SimilarityUtils.decada(disco.getAnio());
        registrarSenalAprendizaje(genero, aceptada);
        registrarSenalAprendizaje(artista, aceptada);
        registrarSenalAprendizaje(decada, aceptada);
        RecomendadorStorage.guardarAprendizaje(aprendizajePorGenero);
    }

    private void registrarSenalAprendizaje(String clave, boolean aceptada) {
        if (clave.endsWith(":") || clave.endsWith(":0")) return;
        aprendizajePorGenero.computeIfAbsent(clave, g -> new BrazoRecomendacion()).registrar(aceptada ? 1.0 : -1.0);
    }

    private double calcularPuntaje(PerfilGustos perfil, PerfilAfinidad afinidad, Disco disco, int posicionCatalogo) {
        String genero = SimilarityUtils.normalizar(disco.getGenero());
        double exploracion = RANDOM.nextDouble() < EPSILON_EXPLORACION ? RANDOM.nextDouble() * 3.0 : 0.0;
        double refuerzo = (valorAprendido("genero:" + genero)
                + valorAprendido(genero)
                + valorAprendido("artista:" + SimilarityUtils.normalizar(disco.getArtista()))
                + valorAprendido("decada:" + SimilarityUtils.decada(disco.getAnio()))) * PESO_APRENDIZAJE;
        double afinidadHistorica = afinidad.afinidadArtista(disco) + afinidad.afinidadDecada(disco);
        double penalizacionReciente = penalizacionHistorial(disco);

        if (perfil == null || perfil.getTotalDiscos() == 0) {
            return 1.0 - (posicionCatalogo * 0.01) + refuerzo + exploracion - penalizacionReciente;
        }

        double porcentajeGenero = perfil.getPorcentajePorGenero() == null ? 0.0 :
                perfil.getPorcentajePorGenero().entrySet().stream()
                        .filter(e -> SimilarityUtils.normalizar(e.getKey()).equals(genero))
                        .mapToDouble(Map.Entry::getValue).findFirst().orElse(0.0);

        double bonoGeneroFavorito = SimilarityUtils.normalizar(perfil.getGeneroFavorito()).equals(genero) ? 15.0 : 0.0;
        double bonoDiversidad = porcentajeGenero == 0.0 ? 5.0 : 0.0;
        double desempateCatalogo = 1.0 - (posicionCatalogo * 0.01);
        return porcentajeGenero + bonoGeneroFavorito + bonoDiversidad + desempateCatalogo
                + refuerzo + exploracion + afinidadHistorica - penalizacionReciente;
    }

    private double valorAprendido(String clave) {
        return aprendizajePorGenero.getOrDefault(clave, new BrazoRecomendacion()).valorEsperado();
    }

    private double penalizacionHistorial(Disco disco) {
        String clave = claveDisco(disco);
        String artista = SimilarityUtils.normalizar(disco.getArtista());
        int decada = SimilarityUtils.decada(disco.getAnio());
        long reciente = System.currentTimeMillis() - (7L * 24 * 60 * 60 * 1000);
        return historial.stream().filter(h -> h.getTimestamp() >= reciente)
                .mapToDouble(h -> {
                    if (clave.equals(h.getClaveDisco())) return 18.0;
                    double pen = artista.equals(h.getArtistaNormalizado()) ? 3.0 : 0.0;
                    return decada > 0 && decada == h.getDecada() ? pen + 1.0 : pen;
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

    private void registrarHistorial(List<Disco> recomendaciones) {
        for (Disco disco : recomendaciones) {
            historial.add(new HistorialRecomendacion(disco, claveDisco(disco)));
        }
        while (historial.size() > 200) historial.remove(0);
        RecomendadorStorage.guardarHistorial(historial);
    }
}
