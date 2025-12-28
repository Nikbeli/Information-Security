package com.cryptoauth.infosecurity.user.utils;

import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;

public class PasswordHashUtil {

    private PasswordHashUtil() {}

    // Генерация соли (16 байт)
    public static String generateSalt() {
        byte[] salt = new byte[16];
        new SecureRandom().nextBytes(salt);
        return Base64.getEncoder().encodeToString(salt);
    }

    // Хэширование с солью
    public static String sha1Hex(String input, String salt) {
        try {
            // "SHA" - SHA-1
            MessageDigest messageDigest = MessageDigest.getInstance("SHA");
            messageDigest.update(salt.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            byte[] digest = messageDigest.digest(input.getBytes(java.nio.charset.StandardCharsets.UTF_8));

            StringBuilder stringBuilder = new StringBuilder();
            for (byte b : digest) {
                stringBuilder.append(String.format("%02x", b & 0xff));
            }

            return stringBuilder.toString();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static boolean matches(String raw, String salt, String hashed) {
        if (raw == null && hashed == null) return true;
        if (raw == null || hashed == null) return false;
        return sha1Hex(raw, salt).equals(hashed);
    }
}
