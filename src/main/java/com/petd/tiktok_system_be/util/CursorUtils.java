package com.petd.tiktok_system_be.util;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class CursorUtils {

    public static String encodeCursor(String field, String value) {
        String raw = field + ":" + value;
        return Base64.getUrlEncoder().encodeToString(raw.getBytes(StandardCharsets.UTF_8));
    }

    public static Cursor decodeCursor(String token) {
        if (token == null || token.isBlank()) return null;
        String decoded = new String(Base64.getUrlDecoder().decode(token), StandardCharsets.UTF_8);
        String[] parts = decoded.split(":", 2);
        return new Cursor(parts[0], parts[1]);
    }

    public record Cursor(String field, String value) {}
}
