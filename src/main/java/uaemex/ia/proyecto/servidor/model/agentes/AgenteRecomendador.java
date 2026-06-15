package uaemex.ia.proyecto.servidor.model.agentes;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import uaemex.ia.proyecto.compartido.Disco;
import uaemex.ia.proyecto.servidor.model.PerfilGustos;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

public class AgenteRecomendador {

    private static final Logger LOGGER = Logger.getLogger(AgenteRecomendador.class.getName());
    private static final int MAX_RECOMENDACIONES = 8;
    private static final String ARCHIVO_CATALOGO = "data/catalogo.json";

    private final Gson gson = new Gson();
    private final List<Disco> catalogoClasico;

    public AgenteRecomendador() {
        this.catalogoClasico = cargarCatalogo();
        LOGGER.info(() -> "Catalogo de recomendaciones cargado: "
                + catalogoClasico.size() + " album(es).");
    }

    public List<Disco> recomendar(PerfilGustos perfil, List<Disco> coleccionUsuario) {
        Set<String> discosExistentes = crearIndiceColeccion(coleccionUsuario);
        List<DiscoPuntuado> candidatos = new ArrayList<>();

        for (int i = 0; i < catalogoClasico.size(); i++) {
            Disco disco = catalogoClasico.get(i);
            if (!discosExistentes.contains(claveDisco(disco))) {
                double puntaje = calcularPuntaje(perfil, disco, i);
                candidatos.add(new DiscoPuntuado(disco, puntaje));
            }
        }

        candidatos.sort(Comparator
                .comparingDouble(DiscoPuntuado::getPuntaje).reversed()
                .thenComparing(d -> d.getDisco().getGenero())
                .thenComparing(d -> d.getDisco().getTitulo()));

        List<Disco> recomendaciones = new ArrayList<>();
        for (DiscoPuntuado candidato : candidatos) {
            if (recomendaciones.size() == MAX_RECOMENDACIONES) {
                break;
            }
            recomendaciones.add(candidato.getDisco());
        }
        return recomendaciones;
    }

    private List<Disco> cargarCatalogo() {
        File archivo = new File(ARCHIVO_CATALOGO);
        if (!archivo.exists()) {
            LOGGER.warning(() -> "No se encontro " + ARCHIVO_CATALOGO
                    + ". El recomendador no tendra catalogo externo.");
            return new ArrayList<>();
        }

        try (Reader reader = new FileReader(archivo)) {
            Type tipoLista = new TypeToken<List<Disco>>() {}.getType();
            List<Disco> catalogo = gson.fromJson(reader, tipoLista);
            return catalogo != null ? catalogo : new ArrayList<>();
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "No se pudo cargar el catalogo de recomendaciones.", e);
            return new ArrayList<>();
        }
    }

    private double calcularPuntaje(PerfilGustos perfil, Disco disco, int posicionCatalogo) {
        if (perfil == null || perfil.getTotalDiscos() == 0) {
            return 1.0 - (posicionCatalogo * 0.01);
        }

        String genero = SimilarityUtils.normalizar(disco.getGenero());
        double porcentajeGenero = 0.0;
        for (String generoPerfil : perfil.getPorcentajePorGenero().keySet()) {
            if (SimilarityUtils.normalizar(generoPerfil).equals(genero)) {
                porcentajeGenero = perfil.getPorcentajePorGenero().get(generoPerfil);
                break;
            }
        }

        double bonoGeneroFavorito = SimilarityUtils.normalizar(perfil.getGeneroFavorito()).equals(genero)
                ? 15.0 : 0.0;
        double bonoDiversidad = porcentajeGenero == 0.0 ? 5.0 : 0.0;
        double desempateCatalogo = 1.0 - (posicionCatalogo * 0.01);
        return porcentajeGenero + bonoGeneroFavorito + bonoDiversidad + desempateCatalogo;
    }

    private Set<String> crearIndiceColeccion(List<Disco> coleccionUsuario) {
        Set<String> indice = new HashSet<>();
        for (Disco disco : coleccionUsuario) {
            indice.add(claveDisco(disco));
        }
        return indice;
    }

    private String claveDisco(Disco disco) {
        return SimilarityUtils.normalizar(disco.getTitulo()) + "|"
                + SimilarityUtils.normalizar(disco.getArtista());
    }
}
