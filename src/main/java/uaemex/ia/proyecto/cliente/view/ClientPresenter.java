package uaemex.ia.proyecto.cliente.view;

import uaemex.ia.proyecto.cliente.controller.ClientController;
import uaemex.ia.proyecto.compartido.Disco;
import uaemex.ia.proyecto.compartido.MensajeSocket;
import uaemex.ia.proyecto.compartido.RespuestaSocket;
import java.util.List;
import java.util.function.Consumer;

public class ClientPresenter {
    private final VentanaPrincipal vista;
    private final String host;
    private final int puerto;
    private ClientController controller;

    public ClientPresenter(VentanaPrincipal vista, String host, int puerto) {
        this.vista = vista;
        this.host = host;
        this.puerto = puerto;
        this.controller = new ClientController(host, puerto);
    }

    public boolean estaConectado() { return controller != null && controller.estaConectado(); }

    public void desconectar() { if (controller != null) controller.desconectar(); }

    public void conectar() {
        vista.setBotonera(false);
        vista.setReconectarEnabled(false);
        desconectar();
        controller = new ClientController(host, puerto);
        AsyncTaskRunner.run(() -> { controller.conectar(); return null; },
            ok -> conexionExitosa(), this::conexionFallida, () -> vista.setReconectarEnabled(true));
    }

    public void registrarDisco(Disco disco) {
        enviar("REGISTRAR_DISCO", disco, this::mostrarRegistro, "Fallo en la comunicacion");
    }

    public void listarColeccion() {
        enviar("LISTAR_DISCOS", null, r -> mostrarLista("Coleccion", r), "Fallo al listar");
    }

    public void buscarAlbum(String consulta) {
        Disco filtro = new Disco();
        filtro.setTitulo(consulta);
        enviar("BUSCAR_ALBUM", filtro, r -> mostrarLista("Busqueda", r), "Fallo al buscar");
    }

    public void obtenerRecomendaciones() {
        enviar("OBTENER_RECOMENDACIONES", null, this::mostrarRecomendaciones, "Fallo al obtener recomendaciones");
    }

    public void aceptarRecomendacion(Disco disco) {
        enviar("ACEPTAR_RECOMENDACION", disco, this::mostrarFeedback, "Fallo al enviar retroalimentacion");
    }

    public void rechazarRecomendacion(Disco disco) {
        enviar("RECHAZAR_RECOMENDACION", disco, this::mostrarFeedback, "Fallo al enviar retroalimentacion");
    }

    private void enviar(String accion, Disco datos, Consumer<RespuestaSocket> onSuccess, String error) {
        vista.setBotonera(false);
        AsyncTaskRunner.run(() -> controller.enviarMensaje(new MensajeSocket(accion, datos)),
                onSuccess, ex -> manejarFalloRed(error, ex), () -> vista.setBotonera(estaConectado()));
    }

    private void mostrarRegistro(RespuestaSocket r) {
        if (!"OK".equals(r.getStatus())) { vista.log("[ERROR] " + r.getMensaje()); return; }
        vista.log("[OK] " + r.getMensaje() + " -> " + r.getDatos());
        vista.agregarDiscoEstadisticas(r.getDatos());
        vista.limpiarFormulario();
    }

    private void mostrarLista(String titulo, RespuestaSocket r) {
        if (!"OK".equals(r.getStatus())) { vista.log("[ERROR] " + r.getMensaje()); return; }
        List<Disco> lista = r.getListaDiscos();
        vista.log("[" + titulo + "] " + r.getMensaje());
        if (lista != null) {
            for (Disco d : lista) vista.log("  • " + d);
        }
        if ("Coleccion".equals(titulo)) vista.actualizarEstadisticasColeccion(lista);
    }

    private void mostrarRecomendaciones(RespuestaSocket r) {
        mostrarLista("Recomendaciones", r);
        if ("OK".equals(r.getStatus())) vista.actualizarRecomendaciones(r.getListaDiscos());
    }

    private void mostrarFeedback(RespuestaSocket r) {
        if (!"OK".equals(r.getStatus())) { vista.log("[ERROR] " + r.getMensaje()); return; }
        vista.log("[MarIA Aprendizaje] " + r.getMensaje() + " -> " + r.getDatos());
    }

    private void conexionExitosa() {
        vista.marcarConectado(host, puerto);
        vista.setBotonera(true);
        vista.log("[MarIA] Conexion establecida.");
        listarColeccion();
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
