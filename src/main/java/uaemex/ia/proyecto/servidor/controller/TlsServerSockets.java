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
        if (config == null || !config.isEnabled()) {
            return new ServerSocket(puerto);
        }
        KeyStore ks = KeyStore.getInstance("PKCS12");
        try (FileInputStream in = new FileInputStream(config.getKeyStore())) {
            ks.load(in, config.getKeyPassword().toCharArray());
        }
        KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        kmf.init(ks, config.getKeyPassword().toCharArray());
        SSLContext ctx = SSLContext.getInstance("TLS");
        ctx.init(kmf.getKeyManagers(), null, null);
        ServerSocketFactory factory = ctx.getServerSocketFactory();
        SSLServerSocket socket = (SSLServerSocket) factory.createServerSocket(puerto);
        socket.setEnabledProtocols(new String[] {"TLSv1.3", "TLSv1.2"});
        return socket;
    }
}
