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
 * Orquesta y ejecuta las acciones solicitadas por el cliente TCP.
 * Actúa como puente entre la red y los modelos/agentes del servidor.
 */
class AccionesCliente {
    private static final Logger LOGGER = Logger.getLogger(AccionesCliente.class.getName());
    private final AgenteAnalizador analizador = new AgenteAnalizador();
    private final AgenteBuscador buscador = new AgenteBuscador();
    private final AgenteRecomendador recomendador = AgenteRecomendador.getInstance();

    // Registra un disco en la BD de manera idempotente
    RespuestaSocket registrarDisco(MensajeSocket mensaje) {
        return RegistroTransacciones.ejecutar(mensaje, () -> registrarDiscoInterno(mensaje));
    }

    private RespuestaSocket registrarDiscoInterno(MensajeSocket mensaje) {
        Disco disco = mensaje.getDatos();
        String error = ValidadorDiscoServidor.validar(disco);
        if (!error.isEmpty()) return RespuestaSocket.error(mensaje.getTransaccionId(), error);
        
        // Verifica duplicados antes de guardar
        if (existeDisco(disco)) {
            return RespuestaSocket.ok(mensaje.getTransaccionId(), "El disco ya existia en la coleccion.", disco);
        }
        
        // Guarda en BD y recalcula el perfil de gustos del usuario
        Database.getInstance().guardar(disco);
        PerfilGustos perfil = analizador.calcularPerfil(Database.getInstance().obtenerTodos());
        LOGGER.info(() -> "Perfil recalculado: " + perfil);
        return RespuestaSocket.ok(mensaje.getTransaccionId(), "Disco registrado y guardado correctamente.", disco);
    }

    // Retorna todos los discos registrados en la base de datos
    RespuestaSocket listarDiscos(MensajeSocket mensaje) {
        List<Disco> lista = Database.getInstance().obtenerTodos();
        return RespuestaSocket.okLista(mensaje.getTransaccionId(), lista.size() + " disco(s) en la coleccion.", lista);
    }

    // Realiza búsquedas aproximadas a través del Agente Buscador
    RespuestaSocket buscarAlbum(MensajeSocket mensaje) {
        String consulta = obtenerConsultaBusqueda(mensaje.getDatos());
        if (consulta.isEmpty()) {
            return RespuestaSocket.error(mensaje.getTransaccionId(), "Se requiere titulo, artista o genero.");
        }
        List<Disco> resultados = buscador.buscar(consulta, Database.getInstance().obtenerTodos());
        String texto = resultados.isEmpty() ? "No se encontraron discos para: " + consulta
                : resultados.size() + " resultado(s) encontrado(s) para: " + consulta;
        return RespuestaSocket.okLista(mensaje.getTransaccionId(), texto, resultados);
    }

    // Pide recomendaciones de IA de manera idempotente
    RespuestaSocket obtenerRecomendaciones(MensajeSocket mensaje) {
        return RegistroTransacciones.ejecutar(mensaje, () -> obtenerRecomendacionesInterno(mensaje));
    }

    private RespuestaSocket obtenerRecomendacionesInterno(MensajeSocket mensaje) {
        List<Disco> coleccion = Database.getInstance().obtenerTodos();
        PerfilGustos perfil = analizador.calcularPerfil(coleccion);
        List<Disco> recomendaciones = recomendador.recomendar(perfil, coleccion);
        String texto = recomendaciones.isEmpty() ? "No hay recomendaciones nuevas disponibles."
                : recomendaciones.size() + " recomendacion(es) generada(s) segun tu perfil: " + perfil.getGeneroFavorito();
        return RespuestaSocket.okLista(mensaje.getTransaccionId(), texto, recomendaciones);
    }

    // Registra feedback (aceptar/rechazar) de manera idempotente
    RespuestaSocket registrarFeedbackRecomendacion(MensajeSocket mensaje, boolean aceptada) {
        return RegistroTransacciones.ejecutar(mensaje, () -> registrarFeedbackInterno(mensaje, aceptada));
    }

    private RespuestaSocket registrarFeedbackInterno(MensajeSocket mensaje, boolean aceptada) {
        Disco disco = mensaje.getDatos();
        if (disco == null) {
            return RespuestaSocket.error(mensaje.getTransaccionId(), "Se requiere el disco para registrar feedback.");
        }
        recomendador.registrarRetroalimentacion(disco, aceptada);
        String resultado = aceptada ? "aceptada" : "rechazada";
        return RespuestaSocket.ok(mensaje.getTransaccionId(), "Retroalimentacion registrada: recomendacion " + resultado + ".", disco);
    }

    // Revisa duplicados comparando claves normalizadas de título/artista
    private boolean existeDisco(Disco disco) {
        String clave = DiscoKeys.clave(disco);
        return Database.getInstance().obtenerTodos().stream()
                .anyMatch(actual -> DiscoKeys.clave(actual).equals(clave));
    }

    // Extrae el primer campo no vacío del filtro de búsqueda
    private String obtenerConsultaBusqueda(Disco disco) {
        if (disco == null) return "";
        if (disco.getTitulo() != null && !disco.getTitulo().trim().isEmpty()) return disco.getTitulo().trim();
        if (disco.getArtista() != null && !disco.getArtista().trim().isEmpty()) return disco.getArtista().trim();
        return disco.getGenero() == null ? "" : disco.getGenero().trim();
    }
}
