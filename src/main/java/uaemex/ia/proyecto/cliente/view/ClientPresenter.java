package uaemex.ia.proyecto.cliente.view;

import uaemex.ia.proyecto.cliente.controller.ClientController;
import uaemex.ia.proyecto.compartido.Disco;
import uaemex.ia.proyecto.compartido.MensajeSocket;
import uaemex.ia.proyecto.compartido.RespuestaSocket;
import uaemex.ia.proyecto.compartido.TlsConfig;
import java.util.List;
import java.util.function.Consumer;

/**
 * Presentador de la ventana principal.
 * Coordina la comunicación de red con las actualizaciones visuales del Swing.
 */
public class ClientPresenter {
    private final VentanaPrincipal vista;
    private final String host;
    private final int puerto;
    private final TlsConfig tlsConfig;
    private ClientController controller;

    public ClientPresenter(VentanaPrincipal vista, String host, int puerto) {
        this(vista, host, puerto, null);
    }

    public ClientPresenter(VentanaPrincipal vista, String host, int puerto, TlsConfig tlsConfig) {
        this.vista = vista;
        this.host = host;
        this.puerto = puerto;
        this.tlsConfig = tlsConfig;
        this.controller = new ClientController(host, puerto, tlsConfig);
    }

    public boolean estaConectado() { return controller != null && controller.estaConectado(); }

    public void desconectar() { if (controller != null) controller.desconectar(); }

    /**
     * Intenta conectar con el servidor de sockets de forma asíncrona (segundo plano).
     */
    public void conectar() {
        vista.setBotonera(false);
        vista.setReconectarEnabled(false);
        desconectar();
        controller = new ClientController(host, puerto, tlsConfig);
        // Evita colgar el EDT al realizar la conexión de red
        AsyncTaskRunner.run(() -> { controller.conectar(); return null; },
            ok -> conexionExitosa(), this::conexionFallida, () -> vista.setReconectarEnabled(true));
    }

    // Pide registrar un disco físico
    public void registrarDisco(Disco disco) {
        enviar("REGISTRAR_DISCO", disco, this::mostrarRegistro, "Fallo en la comunicacion");
    }

    // Pide la colección completa para mostrarla y actualizar el tablero
    public void listarColeccion() {
        enviar("LISTAR_DISCOS", null, r -> mostrarLista("Coleccion", r), "Fallo al listar");
    }

    // Pide buscar un álbum por texto de coincidencia difusa
    public void buscarAlbum(String consulta) {
        Disco filtro = new Disco();
        filtro.setTitulo(consulta);
        enviar("BUSCAR_ALBUM", filtro, this::mostrarBusqueda, "Fallo al buscar");
    }

    // Solicita recomendaciones personalizadas al servidor
    public void obtenerRecomendaciones() {
        enviar("OBTENER_RECOMENDACIONES", null, this::mostrarRecomendaciones, "Fallo al obtener recomendaciones");
    }

    // Informa feedback de aceptación
    public void aceptarRecomendacion(Disco disco) {
        enviar("ACEPTAR_RECOMENDACION", disco, this::mostrarFeedback, "Fallo al enviar retroalimentacion");
    }

    // Informa feedback de rechazo
    public void rechazarRecomendacion(Disco disco) {
        enviar("RECHAZAR_RECOMENDACION", disco, this::mostrarFeedback, "Fallo al enviar retroalimentacion");
    }

    // Envía la trama en un hilo asíncrono y bloquea la botonera temporalmente
    private void enviar(String accion, Disco datos, Consumer<RespuestaSocket> onSuccess, String error) {
        vista.setBotonera(false);
        AsyncTaskRunner.run(() -> controller.enviarMensaje(new MensajeSocket(accion, datos)),
                onSuccess, ex -> manejarFalloRed(error, ex), () -> vista.setBotonera(estaConectado()));
    }

    private void mostrarRegistro(RespuestaSocket r) {
        if (!"OK".equals(r.getStatus())) { vista.log("[ERROR] " + r.getMensaje()); return; }
        vista.log("[OK] " + r.getMensaje() + " -> " + r.getDatos());
        vista.agregarDiscoEstadisticas(r.getDatos()); // Añade al dashboard
        vista.limpiarFormulario();
    }

    private void mostrarLista(String titulo, RespuestaSocket r) {
        if (!"OK".equals(r.getStatus())) { vista.log("[ERROR] " + r.getMensaje()); return; }
        List<Disco> lista = r.getListaDiscos();
        vista.log("[" + titulo + "] " + r.getMensaje());
        if (lista != null) {
            for (Disco d : lista) vista.log("  • " + d);
        }
        if ("Coleccion".equals(titulo)) vista.actualizarEstadisticasColeccion(lista); // Actualiza dashboard
    }

    private void mostrarBusqueda(RespuestaSocket r) {
        mostrarLista("Busqueda", r);
        if ("OK".equals(r.getStatus())) vista.actualizarResultadosBusqueda(r.getListaDiscos());
    }

    private void mostrarRecomendaciones(RespuestaSocket r) {
        mostrarLista("Recomendaciones", r);
        if ("OK".equals(r.getStatus())) vista.actualizarRecomendaciones(r.getListaDiscos());
    }

    private void mostrarFeedback(RespuestaSocket r) {
        if (!"OK".equals(r.getStatus())) { vista.log("[ERROR] " + r.getMensaje()); return; }
        vista.log("[MarIA] " + r.getMensaje() + " -> " + r.getDatos());
    }

    private void conexionExitosa() {
        vista.marcarConectado(host, puerto);
        vista.setBotonera(true);
        vista.log("[MarIA] Conexion establecida.");
    }

    private void conexionFallida(Exception ex) {
        vista.marcarDesconectado();
        vista.log("[MarIA ERROR] No se pudo conectar a " + host + ":" + puerto + " - " + ErrorMessages.rootMessage(ex));
        vista.mostrarDialogoRed("No se pudo conectar con MarIA.\n\nVerifica la IP, el puerto y que el servicio este iniciado.");
    }

    private void manejarFalloRed(String contexto, Exception ex) {
        vista.marcarDesconectado();
        vista.log("[MarIA ERROR] " + contexto + ": " + ErrorMessages.rootMessage(ex));
        vista.mostrarDialogoRed(contexto + ".\n\nLa conexion con MarIA se perdio o expiro.\nRevisa la red y presiona 'Reconectar a MarIA'.");
    }
}
