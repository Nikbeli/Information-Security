package org.example;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;

public class MD5 {
    private static final int[] S = {
            7, 12, 17, 22, 7, 12, 17, 22, 7, 12, 17, 22, 7, 12, 17, 22,
            5, 9, 14, 20, 5, 9, 14, 20, 5, 9, 14, 20, 5, 9, 14, 20,
            4, 11, 16, 23, 4, 11, 16, 23, 4, 11, 16, 23, 4, 11, 16, 23,
            6, 10, 15, 21, 6, 10, 15, 21, 6, 10, 15, 21, 6, 10, 15, 21
    };

    private static final int[] K = new int[64];

    static {
        for (int i = 0; i < 64; i++)
            K[i] = (int) (long) ((1L << 32) * Math.abs(Math.sin(i + 1)));
    }

    public static String hashFromFile(File file) throws IOException {
        FileInputStream fis = new FileInputStream(file);
        byte[] data = fis.readAllBytes();
        fis.close();
        return hash(data);
    }

    public static String hash(byte[] input) {
        int messageLen = input.length;
        int numBlocks = ((messageLen + 8) >>> 6) + 1;
        int totalLen = numBlocks << 6;

        byte[] padding = new byte[totalLen - messageLen];
        padding[0] = (byte) 0x80;

        long messageLenBits = (long) messageLen << 3;
        for (int i = 0; i < 8; i++) {
            padding[padding.length - 8 + i] = (byte) (messageLenBits >>> (8 * i));
        }

        int A = 0x67452301;
        int B = 0xefcdab89;
        int C = 0x98badcfe;
        int D = 0x10325476;

        byte[] block = new byte[64];
        for (int i = 0; i < numBlocks; i++) {
            int index = i << 6;
            for (int j = 0; j < 64; j++) {
                block[j] = (index + j < messageLen) ? input[index + j] : padding[index + j - messageLen];
            }

            int[] M = new int[16];
            for (int j = 0; j < 16; j++) {
                M[j] = ((block[j * 4] & 0xff)) |
                        ((block[j * 4 + 1] & 0xff) << 8) |
                        ((block[j * 4 + 2] & 0xff) << 16) |
                        ((block[j * 4 + 3] & 0xff) << 24);
            }

            int a = A, b = B, c = C, d = D;

            for (int j = 0; j < 64; j++) {
                int F, g;
                if (j < 16) {
                    F = (b & c) | (~b & d);
                    g = j;
                } else if (j < 32) {
                    F = (d & b) | (~d & c);
                    g = (5 * j + 1) % 16;
                } else if (j < 48) {
                    F = b ^ c ^ d;
                    g = (3 * j + 5) % 16;
                } else {
                    F = c ^ (b | ~d);
                    g = (7 * j) % 16;
                }

                int temp = d;
                d = c;
                c = b;
                b = b + Integer.rotateLeft(a + F + K[j] + M[g], S[j]);
                a = temp;
            }

            A += a;
            B += b;
            C += c;
            D += d;
        }

        byte[] digest = new byte[16];
        int[] parts = {A, B, C, D};

        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                digest[i * 4 + j] = (byte) (parts[i] >>> (8 * j));
            }
        }

        StringBuilder sb = new StringBuilder();

        for (byte b : digest) {
            sb.append(String.format("%02x", b & 0xff));
        }

        return sb.toString();
    }

    public static void saveToFile(String hash) {
        try (FileWriter fw = new FileWriter("md5_result.txt", true)) {
            fw.write("MD5: " + hash + "\n");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
