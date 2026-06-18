package uaemex.ia.proyecto.servidor.controller;

import uaemex.ia.proyecto.compartido.TlsConfig;

import javax.net.ServerSocketFactory;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLServerSocket;
import java.io.FileInputStream;
import java.net.ServerSocket;
import java.security.KeyStore;

final class TlsServerSockets {
    private TlsServerSockets() {}

    static ServerSocket crear(int puerto, TlsConfig config) throws Exception {
        // Si la configuracion de TLS es nula o esta deshabilitada, abrimos un ServerSocket TCP estandar
        if (config == null || !config.isEnabled()) {
            return new ServerSocket(puerto);
        }
        
        // Cargamos el almacen de claves (KeyStore) que contiene la llave privada y certificado del servidor
        KeyStore ks = KeyStore.getInstance("PKCS12");
        try (FileInputStream in = new FileInputStream(config.getKeyStore())) {
            ks.load(in, config.getKeyPassword().toCharArray());
        }
        
        // Inicializamos la fabrica de administradores de claves (KeyManagerFactory) con la clave del almacen
        KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        kmf.init(ks, config.getKeyPassword().toCharArray());
        
        // Creamos e inicializamos el contexto SSL configurado para el servidor
        SSLContext ctx = SSLContext.getInstance("TLS");
        ctx.init(kmf.getKeyManagers(), null, null);
        
        // Obtenemos la fabrica de sockets y creamos el socket de servidor seguro SSLServerSocket
        ServerSocketFactory factory = ctx.getServerSocketFactory();
        SSLServerSocket socket = (SSLServerSocket) factory.createServerSocket(puerto);
        
        // Forzamos el uso de protocolos TLS de alta seguridad (1.2 y 1.3)
        socket.setEnabledProtocols(new String[] {"TLSv1.3", "TLSv1.2"});
        return socket;
    }
}
