package uaemex.ia.proyecto.servidor.model;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

final class DbConfig {
    private static final String CONFIG_FILE = "config.properties";
    private static final String DEFAULT_URL = "jdbc:sqlite:data/maria.sqlite";

    private DbConfig() {}

    static String url() {
        Properties props = new Properties();
        try (FileInputStream in = new FileInputStream(CONFIG_FILE)) {
            props.load(in);
        } catch (IOException ignored) {
            return DEFAULT_URL;
        }
        String value = props.getProperty("server.db.url", DEFAULT_URL).trim();
        return value.isEmpty() ? DEFAULT_URL : value;
    }
}
