package uaemex.ia.proyecto.cliente.view;

import uaemex.ia.proyecto.cliente.controller.ClientController;
import uaemex.ia.proyecto.compartido.Disco;
import uaemex.ia.proyecto.compartido.MensajeSocket;
import uaemex.ia.proyecto.compartido.RespuestaSocket;

import java.util.List;
import java.util.function.Consumer;

/**
 * Presentador de la ventana principal.
 * Coordina la vista Swing con el controlador de sockets sin mezclar UI y red.
 */
public class ClientPresenter {

    private final VentanaPrincipal vista;
    private final String host;
    private final int puerto;
    private ClientController controller;
    /**
     * Crea el presentador asociado a una vista y a un servidor concreto.
     *
     * @param vista ventana que recibira cambios de estado y mensajes.
     * @param host host del servidor.
     * @param puerto puerto del servidor.
     */
    public ClientPresenter(VentanaPrincipal vista, String host, int puerto) {
        this.vista = vista;
        this.host = host;
        this.puerto = puerto;
        this.controller = new ClientController(host, puerto);
    }

    /**
     * Consulta si el controlador mantiene una conexion abierta.
     *
     * @return true cuando es posible enviar mensajes al servidor.
     */
    public boolean estaConectado() {
        return controller != null && controller.estaConectado();
    }

    /**
     * Cierra la conexion actual si existe.
     */
    public void desconectar() {
        if (controller != null) {
            controller.desconectar();
        }
    }

    /**
     * Intenta conectar al servidor en segundo plano y actualiza la vista con el resultado.
     */
    public void conectar() {
        vista.setBotonera(false);
        vista.setReconectarEnabled(false);
        desconectar();
        controller = new ClientController(host, puerto);
        AsyncTaskRunner.run(
            () -> {
                controller.conectar();
                return null;
            },
            ok -> conexionExitosa(),
            this::conexionFallida,
            () -> vista.setReconectarEnabled(true));
    }

    /**
     * Solicita al servidor registrar un disco nuevo.
     *
     * @param disco disco validado por el formulario.
     */
    public void registrarDisco(Disco disco) {
        enviar("REGISTRAR_DISCO", disco, this::mostrarRegistro, "Fallo en la comunicacion");
    }

    /**
     * Solicita la lista completa de discos guardados en el servidor.
     */
    public void listarColeccion() {
        enviar("LISTAR_DISCOS", null, r -> mostrarLista("Coleccion", r), "Fallo al listar");
    }

    /**
     * Envia una consulta de busqueda usando el titulo del disco como campo de filtro.
     *
     * @param consulta texto capturado en el panel de busqueda.
     */
    public void buscarAlbum(String consulta) {
        Disco filtro = new Disco();
        filtro.setTitulo(consulta);
        enviar("BUSCAR_ALBUM", filtro, r -> mostrarLista("Busqueda", r), "Fallo al buscar");
    }

    /**
     * Pide al servidor recomendaciones basadas en la coleccion registrada.
     */
    public void obtenerRecomendaciones() {
        enviar("OBTENER_RECOMENDACIONES", null,
                this::mostrarRecomendaciones, "Fallo al obtener recomendaciones");
    }

    /**
     * Informa al servidor que el usuario acepto una recomendacion.
     *
     * @param disco disco recomendado seleccionado.
     */
    public void aceptarRecomendacion(Disco disco) {
        enviar("ACEPTAR_RECOMENDACION", disco, this::mostrarFeedback, "Fallo al enviar retroalimentacion");
    }

    /**
     * Informa al servidor que el usuario rechazo una recomendacion.
     *
     * @param disco disco recomendado seleccionado.
     */
    public void rechazarRecomendacion(Disco disco) {
        enviar("RECHAZAR_RECOMENDACION", disco, this::mostrarFeedback, "Fallo al enviar retroalimentacion");
    }

    /**
     * Plantilla comun para mandar acciones al servidor con manejo uniforme de errores.
     *
     * @param accion nombre de accion reconocido por el servidor.
     * @param datos disco opcional asociado a la accion.
     * @param onSuccess callback que procesa una respuesta recibida.
     * @param error texto base para reportar fallas de red.
     */
    private void enviar(String accion, Disco datos, Consumer<RespuestaSocket> onSuccess, String error) {
        vista.setBotonera(false);
        MensajeSocket msg = new MensajeSocket(accion, datos);
        AsyncTaskRunner.run(
                () -> controller.enviarMensaje(msg),
                onSuccess,
                ex -> manejarFalloRed(error, ex),
                () -> vista.setBotonera(estaConectado()));
    }

    /**
     * Presenta el resultado del registro y limpia el formulario si fue exitoso.
     *
     * @param r respuesta del servidor.
     */
    private void mostrarRegistro(RespuestaSocket r) {
        if (!"OK".equals(r.getStatus())) {
            vista.log("[ERROR] " + r.getMensaje());
            return;
        }
        vista.log("[OK] " + r.getMensaje() + " -> " + r.getDatos());
        vista.limpiarFormulario();
    }

    /**
     * Escribe en el log una lista de discos recibida del servidor.
     *
     * @param titulo etiqueta de la operacion que genero la lista.
     * @param r respuesta con lista o error.
     */
    private void mostrarLista(String titulo, RespuestaSocket r) {
        if (!"OK".equals(r.getStatus())) {
            vista.log("[ERROR] " + r.getMensaje());
            return;
        }
        List<Disco> lista = r.getListaDiscos();
        vista.log("[" + titulo + "] " + r.getMensaje());
        if (lista != null) {
            // Cada disco se imprime en una linea para que la respuesta sea facil de leer.
            for (Disco d : lista) vista.log("  • " + d);
        }
    }

    /**
     * Muestra recomendaciones y las deja disponibles para aceptar o rechazar.
     *
     * @param r respuesta del servidor.
     */
    private void mostrarRecomendaciones(RespuestaSocket r) {
        mostrarLista("Recomendaciones", r);
        if ("OK".equals(r.getStatus())) {
            vista.actualizarRecomendaciones(r.getListaDiscos());
        }
    }

    /**
     * Reporta que el aprendizaje del recomendador recibio retroalimentacion.
     *
     * @param r respuesta del servidor.
     */
    private void mostrarFeedback(RespuestaSocket r) {
        if (!"OK".equals(r.getStatus())) {
            vista.log("[ERROR] " + r.getMensaje());
            return;
        }
        vista.log("[MarIA Aprendizaje] " + r.getMensaje() + " -> " + r.getDatos());
    }

    /**
     * Actualiza la interfaz despues de una conexion exitosa.
     */
    private void conexionExitosa() {
        vista.marcarConectado(host, puerto);
        vista.setBotonera(true);
        vista.log("[MarIA] Conexion establecida.");
    }

    /**
     * Reporta un intento de conexion fallido y deja la interfaz en modo desconectado.
     *
     * @param ex excepcion original del intento de red.
     */
    private void conexionFallida(Exception ex) {
        vista.marcarDesconectado();
        vista.log("[MarIA ERROR] No se pudo conectar a " + host + ":" + puerto
                + " - " + ErrorMessages.rootMessage(ex));
        vista.mostrarDialogoRed("No se pudo conectar con MarIA.\n\n"
                + "Verifica la IP, el puerto y que el servicio este iniciado.");
    }

    /**
     * Maneja errores posteriores a la conexion, como timeouts o cierres del servidor.
     *
     * @param contexto descripcion breve de la accion que fallo.
     * @param ex excepcion recibida.
     */
    private void manejarFalloRed(String contexto, Exception ex) {
        vista.marcarDesconectado();
        vista.log("[MarIA ERROR] " + contexto + ": " + ErrorMessages.rootMessage(ex));
        vista.mostrarDialogoRed(contexto + ".\n\n"
                + "La conexion con MarIA se perdio o expiro.\n"
                + "Revisa la red y presiona 'Reconectar a MarIA'.");
    }
}
