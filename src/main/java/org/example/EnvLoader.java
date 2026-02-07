package org.example;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Loads KEY=VALUE pairs from a .env file. Use {@link #get(String)} to read
 * a value (from the loaded file first, then from system environment).
 */
public final class EnvLoader {
    private static Map<String, String> env = Collections.emptyMap();

    /**
     * Load a .env-style file from the given path (e.g. ".env").
     * Lines are KEY=VALUE; leading/trailing whitespace and empty lines are skipped.
     * Does not modify system environment; use {@link #get(String)} to read values.
     */
    public static void load(String path) {
        Path p = Path.of(path);
        if (!Files.isRegularFile(p)) {
            return;
        }
        Map<String, String> map = new HashMap<>();
        try {
            for (String line : Files.readAllLines(p)) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) continue;
                int eq = line.indexOf('=');
                if (eq <= 0) continue;
                String key = line.substring(0, eq).trim();
                String value = line.substring(eq + 1).trim();
                if (value.startsWith("\"") && value.endsWith("\"") && value.length() >= 2)
                    value = value.substring(1, value.length() - 1);
                if (!key.isEmpty())
                    map.put(key, value);
            }
        } catch (IOException ignored) {
            // leave env empty
        }
        env = map;
    }

    /**
     * Returns the value for the given key: first from the loaded .env, then from the system environment.
     */
    public static String get(String key) {
        if (env.containsKey(key) && env.get(key) != null && !env.get(key).isBlank())
            return env.get(key);
        return System.getenv(key);
    }
}
