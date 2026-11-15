package org.delcom.ifs25032springbootcashflow;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;

/** Memuat .env ke system properties (tanpa menimpa env / property yang sudah ada). */
public final class DotEnv {
    private DotEnv() {}

    public static void load() { load(Path.of(".env")); }

    public static void load(Path file) {
        if (!Files.exists(file)) return;
        try {
            for (String raw : Files.readAllLines(file)) {
                String line = raw.trim();
                int eq = line.indexOf('=');
                if (line.isEmpty() || line.startsWith("#") || eq <= 0) continue;
                String key = line.substring(0, eq).trim();
                String value = line.substring(eq + 1).trim();
                if (value.length() >= 2 && (value.startsWith("\"") && value.endsWith("\"")
                        || value.startsWith("'") && value.endsWith("'"))) {
                    value = value.substring(1, value.length() - 1);
                }
                if (System.getenv(key) == null && System.getProperty(key) == null) System.setProperty(key, value);
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
