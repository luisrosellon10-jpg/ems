package ems.config;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

public final class AppConfig {
    
    public static String getOrEnv(String key, String envVarName) {
    String v = get(key);
    if (v != null && !v.isBlank()) return v;

    String env = System.getenv(envVarName);
    if (env != null && !env.isBlank()) return env;

    return null;
}
    
    private static final Properties PROPS = new Properties();
    private static volatile boolean loaded = false;

    private AppConfig() {}

    public static void loadOnce() {
        if (loaded) return;
        synchronized (AppConfig.class) {
            if (loaded) return;

            // 1) Try classpath root (/config.properties)
            try (InputStream in = AppConfig.class.getResourceAsStream("/config.properties")) {
                if (in != null) {
                    PROPS.load(new InputStreamReader(in, StandardCharsets.UTF_8));
                    loaded = true;
                    return;
                }
            } catch (IOException e) {
                throw new RuntimeException("Failed reading /config.properties from classpath", e);
            }

            // 2) Try working directory file (useful in NetBeans / runnable jar)
            File f = new File("config.properties");
            if (f.exists()) {
                try (InputStream in = new FileInputStream(f)) {
                    PROPS.load(new InputStreamReader(in, StandardCharsets.UTF_8));
                    loaded = true;
                    return;
                } catch (IOException e) {
                    throw new RuntimeException("Failed reading config.properties from working directory", e);
                }
            }

            throw new IllegalStateException(
                    "config.properties not found.\n" +
                    "Fix: put it in Source Packages (so it becomes /config.properties) OR place config.properties next to the jar."
            );
        }
    }

    public static String get(String key) {
        loadOnce();
        String v = PROPS.getProperty(key);
        if (v == null) throw new IllegalStateException("Missing config key: " + key);
        return v.trim();
    }

    public static int getInt(String key) {
        return Integer.parseInt(get(key));
    }
}
