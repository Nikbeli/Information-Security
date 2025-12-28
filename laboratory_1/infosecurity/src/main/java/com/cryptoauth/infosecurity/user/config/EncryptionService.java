package com.cryptoauth.infosecurity.user.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.nio.file.*;
import java.security.Key;
import java.util.Base64;

@Service
public class EncryptionService {

    private final String secret;

    public EncryptionService(@Value("${app.encryption.key}") String secret) {
        this.secret = secret;
    }

    private Cipher initCipher(int mode) throws Exception {
        Key aesKey = new SecretKeySpec(secret.getBytes(), "AES");
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(mode, aesKey);
        return cipher;
    }

    public void encryptToFile(String data, Path path) throws Exception {
        Cipher cipher = initCipher(Cipher.ENCRYPT_MODE);
        byte[] encrypted = cipher.doFinal(data.getBytes());
        Files.createDirectories(path.getParent());
        Files.write(path, Base64.getEncoder().encode(encrypted));
    }

    public String decryptFromFile(Path path) throws Exception {
        if (!Files.exists(path)) return "";
        byte[] content = Files.readAllBytes(path);
        byte[] decoded = Base64.getDecoder().decode(content);
        Cipher cipher = initCipher(Cipher.DECRYPT_MODE);
        byte[] decrypted = cipher.doFinal(decoded);
        return new String(decrypted);
    }
}
