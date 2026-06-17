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
    private final AgenteRecomendador recomendador = AgenteRecomendador.getInstance();

    /**
     * Guarda un disco enviado por el cliente y recalcula el perfil de gustos.
     *
     * @param mensaje solicitud que debe incluir un Disco en datos.
     * @return respuesta de exito o error de validacion.
     */
    RespuestaSocket registrarDisco(MensajeSocket mensaje) {
        return RegistroTransacciones.ejecutar(mensaje, () -> registrarDiscoInterno(mensaje));
    }

    private RespuestaSocket registrarDiscoInterno(MensajeSocket mensaje) {
        Disco disco = mensaje.getDatos();
        String error = ValidadorDiscoServidor.validar(disco);
        if (!error.isEmpty()) return RespuestaSocket.error(mensaje.getTransaccionId(), error);
        if (existeDisco(disco)) {
            return RespuestaSocket.ok(mensaje.getTransaccionId(), "El disco ya existia en la coleccion.", disco);
        }
        Database.getInstance().guardar(disco);
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

    RespuestaSocket obtenerRecomendaciones(MensajeSocket mensaje) {
        return RegistroTransacciones.ejecutar(mensaje, () -> obtenerRecomendacionesInterno(mensaje));
    }

    private RespuestaSocket obtenerRecomendacionesInterno(MensajeSocket mensaje) {
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
     * Aprende de la aceptacion o rechazo de una recomendacion enviada al cliente.
     *
     * @param mensaje solicitud con el disco evaluado.
     * @param aceptada true si la recomendacion fue aceptada por el usuario.
     * @return respuesta que confirma el ajuste del estado aprendido.
     */
    RespuestaSocket registrarFeedbackRecomendacion(MensajeSocket mensaje, boolean aceptada) {
        return RegistroTransacciones.ejecutar(mensaje, () -> registrarFeedbackInterno(mensaje, aceptada));
    }

    private RespuestaSocket registrarFeedbackInterno(MensajeSocket mensaje, boolean aceptada) {
        Disco disco = mensaje.getDatos();
        if (disco == null) {
            return RespuestaSocket.error(mensaje.getTransaccionId(),
                    "Se requiere el disco recomendado para registrar retroalimentacion.");
        }

        recomendador.registrarRetroalimentacion(disco, aceptada);
        String resultado = aceptada ? "aceptada" : "rechazada";
        return RespuestaSocket.ok(mensaje.getTransaccionId(),
                "Retroalimentacion registrada: recomendacion " + resultado + ".", disco);
    }

    private boolean existeDisco(Disco disco) {
        String clave = DiscoKeys.clave(disco);
        return Database.getInstance().obtenerTodos().stream()
                .anyMatch(actual -> DiscoKeys.clave(actual).equals(clave));
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
