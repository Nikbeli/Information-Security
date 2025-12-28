package org.encrypting;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

public class FileUtils {
    public static byte[] readFile(File file) throws IOException {
        return Files.readAllBytes(file.toPath());
    }

    // Чтение файла как текста в UTF-8 (только для ключей!)
    public static String readTextFile(File file) throws IOException {
        return Files.readString(file.toPath(), StandardCharsets.UTF_8);
    }

    public static void writeFile(File file, byte[] data) throws IOException {
        Files.write(file.toPath(), data);
    }
}
