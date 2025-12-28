package com.cryptoauth.infosecurity.user.utils;

import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import java.io.*;

public class DesFileCipher {

    private static final String TRANSFORMATION = "DES/CBC/PKCS5Padding";

    // фиксированный IV (8 байт), выполняем по тз.
    private static final byte[] FIXED_IV = new byte[] { 0, 0, 0, 0, 0, 0, 0, 1 };

    private DesFileCipher() {}

    // Генерация ключа DES из парольной фразы (берём первые 8 байт SHA-1 хэша)
    public static SecretKeySpec keyFromPassphrase(String passphrase) {
        try {
            MessageDigest messageDigest = MessageDigest.getInstance("SHA");
            byte[] digest = messageDigest.digest(passphrase.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            
            // DES требует 8-байтный ключ -> берём первые 8 байт хэша
            byte[] keyBytes = new byte[8];
            System.arraycopy(digest, 0, keyBytes, 0, 8);

            return new SecretKeySpec(keyBytes, "DES");
        } catch (Exception e) {
            throw new RuntimeException("Cannot generate key from passphrase", e);
        }
    }

    public static void processFile(int mode, File inFile, File outFile, String passphrase) {
        try {
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);

            cipher.init(mode, keyFromPassphrase(passphrase), new IvParameterSpec(FIXED_IV));

            if (!inFile.exists() && mode == Cipher.DECRYPT_MODE)
                throw new FileNotFoundException("Encrypted file not found: " + inFile.getAbsolutePath());

            try (FileInputStream fis = new FileInputStream(inFile);
                    FileOutputStream fos = new FileOutputStream(outFile);
                    CipherOutputStream cos = new CipherOutputStream(fos, cipher)) {

                byte[] buffer = new byte[8192];
                int r;

                while ((r = fis.read(buffer)) != -1)
                    cos.write(buffer, 0, r);
            }
        } catch (IOException | NoSuchAlgorithmException | NoSuchPaddingException
                | InvalidKeyException | InvalidAlgorithmParameterException e) {
            throw new RuntimeException((mode == Cipher.ENCRYPT_MODE ? "Encryption" : "Decryption") + " failed", e);
        }
    }

    public static void encryptFile(File inFile, File outFile, String passphrase) {
        processFile(Cipher.ENCRYPT_MODE, inFile, outFile, passphrase);
    }

    public static void decryptFile(File inFile, File outFile, String passphrase) {
        processFile(Cipher.DECRYPT_MODE, inFile, outFile, passphrase);
    }

    public static void overwriteAndDelete(File file) {
        if (file == null || !file.exists()) return;

        try (RandomAccessFile raf = new RandomAccessFile(file, "rw")) {
            long size = raf.length();
            raf.seek(0);
            byte[] zeros = new byte[8192];
            long written = 0;

            while (written < size) {
                int toWrite = (int)Math.min(zeros.length, size - written);
                raf.write(zeros, 0, toWrite);
                written += toWrite;
            }
        } catch (IOException ignored) {}
        file.delete();
    }
}
