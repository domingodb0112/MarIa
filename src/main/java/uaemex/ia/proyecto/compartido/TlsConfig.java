package uaemex.ia.proyecto.compartido;

import java.util.Properties;

public class TlsConfig {
    private final boolean enabled;
    private final String keyStore;
    private final String keyPassword;
    private final String trustStore;
    private final String trustPassword;

    public TlsConfig(Properties props) {
        enabled = Boolean.parseBoolean(props.getProperty("server.tls.enabled", "false"));
        keyStore = props.getProperty("server.tls.keystore", "certs/maria-keystore.p12");
        keyPassword = props.getProperty("server.tls.keystore.password", "changeit");
        trustStore = props.getProperty("server.tls.truststore", "certs/maria-truststore.p12");
        trustPassword = props.getProperty("server.tls.truststore.password", "changeit");
    }

    public boolean isEnabled() { return enabled; }
    public String getKeyStore() { return keyStore; }
    public String getKeyPassword() { return keyPassword; }
    public String getTrustStore() { return trustStore; }
    public String getTrustPassword() { return trustPassword; }
}
