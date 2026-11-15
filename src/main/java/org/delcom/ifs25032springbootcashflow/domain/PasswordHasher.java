package org.delcom.ifs25032springbootcashflow.domain;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

public final class PasswordHasher {
    private PasswordHasher() {}

    public static String hash(String raw) { return hashWith(raw, "SHA-256"); }

    public static String hashWith(String raw, String algorithm) {
        try {
            byte[] d = MessageDigest.getInstance(algorithm).digest(raw.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(d);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }

    public static boolean verify(String raw, String hash) {
        return hash != null && hash(raw).equals(hash);
    }
}
