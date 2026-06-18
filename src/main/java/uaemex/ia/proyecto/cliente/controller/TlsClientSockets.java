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
        // Si TLS no esta configurado o esta deshabilitado, retornamos un socket TCP estandar sin cifrado
        if (config == null || !config.isEnabled()) {
            Socket socket = new Socket();
            socket.connect(new InetSocketAddress(host, puerto), timeout);
            return socket;
        }
        
        // Creamos el contexto SSL usando la configuracion y el almacen de confianza (Truststore)
        SSLContext ctx = contexto(config);
        
        // Instanciamos el socket SSL seguro y habilitamos unicamente protocolos seguros (TLS v1.2 y v1.3)
        SSLSocket socket = (SSLSocket) ctx.getSocketFactory().createSocket();
        socket.setEnabledProtocols(new String[] {"TLSv1.3", "TLSv1.2"});
        
        // Conectamos el socket con el servidor remoto bajo el timeout establecido
        socket.connect(new InetSocketAddress(host, puerto), timeout);
        
        // Iniciamos el saludo de seguridad (handshake) para negociar cifrado antes del envio de bytes
        socket.startHandshake();
        return socket;
    }

    private static SSLContext contexto(TlsConfig config) throws Exception {
        // Cargamos el almacen de confianza (TrustStore) que valida el certificado del servidor
        KeyStore ts = KeyStore.getInstance("PKCS12");
        try (FileInputStream in = new FileInputStream(config.getTrustStore())) {
            ts.load(in, config.getTrustPassword().toCharArray());
        }
        
        // Inicializamos la fabrica de administradores de confianza con el truststore cargado
        TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        tmf.init(ts);
        
        // Inicializamos el SSLContext con el motor TLS y le inyectamos los administradores de confianza
        SSLContext ctx = SSLContext.getInstance("TLS");
        ctx.init(null, tmf.getTrustManagers(), null);
        return ctx;
    }
}
