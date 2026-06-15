package uaemex.ia.proyecto.servidor.controller;

import uaemex.ia.proyecto.compartido.Disco;
import uaemex.ia.proyecto.compartido.MensajeSocket;
import uaemex.ia.proyecto.compartido.RespuestaSocket;
import uaemex.ia.proyecto.servidor.model.Database;
import uaemex.ia.proyecto.servidor.model.PerfilGustos;
import uaemex.ia.proyecto.servidor.model.agentes.AgenteAnalizador;
import uaemex.ia.proyecto.servidor.model.agentes.AgenteBuscador;
import uaemex.ia.proyecto.servidor.model.agentes.AgenteRecomendador;

import java.util.List;
import java.util.logging.Logger;

class AccionesCliente {

    private static final Logger LOGGER = Logger.getLogger(AccionesCliente.class.getName());

    private final AgenteAnalizador analizador = new AgenteAnalizador();
    private final AgenteBuscador buscador = new AgenteBuscador();
    private final AgenteRecomendador recomendador = new AgenteRecomendador();

    RespuestaSocket registrarDisco(MensajeSocket mensaje) {
        Disco disco = mensaje.getDatos();
        if (disco == null) {
            return RespuestaSocket.error(mensaje.getTransaccionId(), "Se requieren datos del disco.");
        }
        Database.getInstance().guardar(disco);
        PerfilGustos perfil = analizador.calcularPerfil(Database.getInstance().obtenerTodos());
        LOGGER.info(() -> "Perfil recalculado: " + perfil);
        return RespuestaSocket.ok(mensaje.getTransaccionId(),
                "Disco registrado y guardado correctamente.", disco);
    }

    RespuestaSocket listarDiscos(MensajeSocket mensaje) {
        List<Disco> lista = Database.getInstance().obtenerTodos();
        return RespuestaSocket.okLista(mensaje.getTransaccionId(),
                lista.size() + " disco(s) en la coleccion.", lista);
    }

    RespuestaSocket buscarAlbum(MensajeSocket mensaje) {
        String consulta = obtenerConsultaBusqueda(mensaje.getDatos());
        if (consulta.isEmpty()) {
            return RespuestaSocket.error(mensaje.getTransaccionId(),
                    "Se requiere una consulta en el titulo, artista o genero del disco.");
        }
        List<Disco> resultados = buscador.buscar(consulta, Database.getInstance().obtenerTodos());
        String texto = resultados.isEmpty()
                ? "No se encontraron discos para: " + consulta
                : resultados.size() + " resultado(s) encontrado(s) para: " + consulta;
        return RespuestaSocket.okLista(mensaje.getTransaccionId(), texto, resultados);
    }

    RespuestaSocket obtenerRecomendaciones(MensajeSocket mensaje) {
        List<Disco> coleccion = Database.getInstance().obtenerTodos();
        PerfilGustos perfil = analizador.calcularPerfil(coleccion);
        List<Disco> recomendaciones = recomendador.recomendar(perfil, coleccion);
        String texto = recomendaciones.isEmpty()
                ? "No hay recomendaciones nuevas disponibles."
                : recomendaciones.size() + " recomendacion(es) generada(s) segun tu perfil: "
                        + perfil.getGeneroFavorito();
        return RespuestaSocket.okLista(mensaje.getTransaccionId(), texto, recomendaciones);
    }

    private String obtenerConsultaBusqueda(Disco disco) {
        if (disco == null) {
            return "";
        }
        if (disco.getTitulo() != null && !disco.getTitulo().trim().isEmpty()) {
            return disco.getTitulo().trim();
        }
        if (disco.getArtista() != null && !disco.getArtista().trim().isEmpty()) {
            return disco.getArtista().trim();
        }
        return disco.getGenero() == null ? "" : disco.getGenero().trim();
    }
}
