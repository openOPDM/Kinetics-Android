package org.kineticsfoundation.net;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Network configuration params provided by Maven
 * Created by akaverin on 10/7/13.
 */
public class NetworkConfig {

    public static final String URL;

    static {
        Properties prop = new Properties();
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        InputStream stream = loader.getResourceAsStream("site.properties");
        try {
            prop.load(stream);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load resources!");
        }
        URL = prop.getProperty("url");
    }
}
