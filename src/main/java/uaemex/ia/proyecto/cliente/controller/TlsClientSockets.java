package uaemex.ia.proyecto.cliente.controller;

import uaemex.ia.proyecto.compartido.TlsConfig;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.TrustManagerFactory;
import java.io.FileInputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.security.KeyStore;

final class TlsClientSockets {
    private TlsClientSockets() {}

    static Socket conectar(String host, int puerto, int timeout, TlsConfig config) throws Exception {
        if (config == null || !config.isEnabled()) {
            Socket socket = new Socket();
            socket.connect(new InetSocketAddress(host, puerto), timeout);
            return socket;
        }
        SSLContext ctx = contexto(config);
        SSLSocket socket = (SSLSocket) ctx.getSocketFactory().createSocket();
        socket.setEnabledProtocols(new String[] {"TLSv1.3", "TLSv1.2"});
        socket.connect(new InetSocketAddress(host, puerto), timeout);
        socket.startHandshake();
        return socket;
    }

    private static SSLContext contexto(TlsConfig config) throws Exception {
        KeyStore ts = KeyStore.getInstance("PKCS12");
        try (FileInputStream in = new FileInputStream(config.getTrustStore())) {
            ts.load(in, config.getTrustPassword().toCharArray());
        }
        TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        tmf.init(ts);
        SSLContext ctx = SSLContext.getInstance("TLS");
        ctx.init(null, tmf.getTrustManagers(), null);
        return ctx;
    }
}
