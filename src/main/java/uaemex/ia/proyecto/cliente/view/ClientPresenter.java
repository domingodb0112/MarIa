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

    public boolean estaConectado() {
        return controller != null && controller.estaConectado();
    }
    public void desconectar() {
        if (controller != null) {
            controller.desconectar();
        }
    }

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
        enviar("OBTENER_RECOMENDACIONES", null,
                r -> mostrarLista("Recomendaciones", r), "Fallo al obtener recomendaciones");
    }

    private void enviar(String accion, Disco datos, Consumer<RespuestaSocket> onSuccess, String error) {
        vista.setBotonera(false);
        MensajeSocket msg = new MensajeSocket(accion, datos);
        AsyncTaskRunner.run(
                () -> controller.enviarMensaje(msg),
                onSuccess,
                ex -> manejarFalloRed(error, ex),
                () -> vista.setBotonera(estaConectado()));
    }

    private void mostrarRegistro(RespuestaSocket r) {
        if (!"OK".equals(r.getStatus())) {
            vista.log("[ERROR] " + r.getMensaje());
            return;
        }
        vista.log("[OK] " + r.getMensaje() + " -> " + r.getDatos());
        vista.limpiarFormulario();
    }

    private void mostrarLista(String titulo, RespuestaSocket r) {
        if (!"OK".equals(r.getStatus())) {
            vista.log("[ERROR] " + r.getMensaje());
            return;
        }
        List<Disco> lista = r.getListaDiscos();
        vista.log("[" + titulo + "] " + r.getMensaje());
        if (lista != null) {
            for (Disco d : lista) vista.log("  • " + d);
        }
    }

    private void conexionExitosa() {
        vista.marcarConectado(host, puerto);
        vista.setBotonera(true);
        vista.log("Conexion establecida con el servidor.");
    }

    private void conexionFallida(Exception ex) {
        vista.marcarDesconectado();
        vista.log("[ERROR] No se pudo conectar a " + host + ":" + puerto
                + " - " + ErrorMessages.rootMessage(ex));
        vista.mostrarDialogoRed("No se pudo conectar con el servidor.\n\n"
                + "Verifica la IP, el puerto y que el servidor este iniciado.");
    }

    private void manejarFalloRed(String contexto, Exception ex) {
        vista.marcarDesconectado();
        vista.log("[ERROR] " + contexto + ": " + ErrorMessages.rootMessage(ex));
        vista.mostrarDialogoRed(contexto + ".\n\n"
                + "La conexion con el servidor se perdio o expiro.\n"
                + "Revisa la red y presiona 'Reconectar al Servidor'.");
    }
}
