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

/**
 * Ejecuta las acciones de negocio solicitadas por un cliente conectado.
 * Esta clase separa el protocolo de socket de la logica de coleccion y agentes.
 */
class AccionesCliente {

    private static final Logger LOGGER = Logger.getLogger(AccionesCliente.class.getName());

    private final AgenteAnalizador analizador = new AgenteAnalizador();
    private final AgenteBuscador buscador = new AgenteBuscador();
    private final AgenteRecomendador recomendador = new AgenteRecomendador();

    /**
     * Guarda un disco enviado por el cliente y recalcula el perfil de gustos.
     *
     * @param mensaje solicitud que debe incluir un Disco en datos.
     * @return respuesta de exito o error de validacion.
     */
    RespuestaSocket registrarDisco(MensajeSocket mensaje) {
        Disco disco = mensaje.getDatos();
        if (disco == null) {
            return RespuestaSocket.error(mensaje.getTransaccionId(), "Se requieren datos del disco.");
        }
        Database.getInstance().guardar(disco);
        // El perfil se recalcula despues del guardado para que las recomendaciones futuras cambien.
        PerfilGustos perfil = analizador.calcularPerfil(Database.getInstance().obtenerTodos());
        LOGGER.info(() -> "Perfil recalculado: " + perfil);
        return RespuestaSocket.ok(mensaje.getTransaccionId(),
                "Disco registrado y guardado correctamente.", disco);
    }

    /**
     * Devuelve todos los discos persistidos en la coleccion.
     *
     * @param mensaje solicitud original usada para conservar el id de transaccion.
     * @return respuesta con la lista completa de discos.
     */
    RespuestaSocket listarDiscos(MensajeSocket mensaje) {
        List<Disco> lista = Database.getInstance().obtenerTodos();
        return RespuestaSocket.okLista(mensaje.getTransaccionId(),
                lista.size() + " disco(s) en la coleccion.", lista);
    }

    /**
     * Busca discos por titulo, artista o genero usando el agente buscador.
     *
     * @param mensaje solicitud que debe contener una consulta en datos.
     * @return respuesta con resultados ordenados o mensaje de error.
     */
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

    /**
     * Genera recomendaciones a partir del perfil calculado con la coleccion actual.
     *
     * @param mensaje solicitud original usada para conservar el id de transaccion.
     * @return respuesta con discos recomendados.
     */
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

    /**
     * Extrae la primera consulta disponible del disco usado como filtro.
     *
     * @param disco datos enviados por el cliente para buscar.
     * @return texto de consulta normalizado con trim o cadena vacia.
     */
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
