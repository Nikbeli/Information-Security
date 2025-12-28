package org.encrypting;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;

// Реализация блочного шифра "Магма" (ГОСТ Р 34.12-2015) в режиме гаммирования с обратной связью (CFB).
/* Параметры алгоритма: Длина блока 64 бита (8 байт), длина ключа 256 бит (32 байта). Количество раундов: 32
   Режим работы: CFB (Cipher Feedback) — потоковый режим, не требует дополнения (padding) */
public class MagmaCFB {

    /* S-блоки (таблицы замен) — фиксированная часть стандарта.
     Используются 8 таблиц по 16 значений (4-битная замена). Значения официально заданы в ГОСТ */
    private static final int[][] S = {
            {12, 8, 2, 10, 6, 4, 14, 5, 11, 13, 9, 7, 15, 3, 1, 0},
            {8, 10, 12, 6, 2, 11, 0, 7, 4, 13, 15, 1, 9, 5, 3, 14},
            {12, 14, 1, 13, 10, 8, 9, 15, 2, 0, 6, 4, 11, 3, 7, 5},
            {13, 7, 5, 12, 10, 9, 0, 6, 1, 11, 14, 8, 3, 15, 2, 4},
            {10, 14, 13, 8, 2, 11, 7, 15, 12, 6, 3, 9, 0, 5, 4, 1},
            {7, 9, 11, 15, 12, 2, 14, 13, 10, 3, 8, 4, 1, 5, 6, 0},
            {14, 6, 3, 5, 10, 15, 8, 12, 11, 1, 4, 7, 9, 13, 0, 2},
            {5, 0, 15, 10, 3, 12, 9, 6, 8, 13, 2, 11, 14, 4, 1, 7}
    };

    // Массив раундовых ключей
    private final int[] roundKeys = new int[32];

    // Вектор инициализации (IV) — 64 бита (8 байт) обеспечивает уникальность гаммы при одинаковом ключе.
    private byte[] iv;

    public MagmaCFB(byte[] userKey, byte[] iv) {
        if (userKey.length != 32) throw new IllegalArgumentException("Ключ должен быть 32 байта");
        if (iv.length != 8) throw new IllegalArgumentException("IV должен быть 8 байт");
        this.iv = iv.clone();
        expandKey(userKey);
    }

    // Расширение 256-битного ключа до 32 раундовых 32-битных ключей.
    private void expandKey(byte[] key) {
        // Интерпретируем байты ключа как 8 32-битных слов
        ByteBuffer bb = ByteBuffer.wrap(key).order(ByteOrder.BIG_ENDIAN);
        int[] k = new int[8];
        for (int i = 0; i < 8; i++) k[i] = bb.getInt();

        // Формируем 32 раундовых ключа K0..K7, K0..K7, K0..K7, K7..K0
        for (int i = 0; i < 24; i++) roundKeys[i] = k[i % 8];
        for (int i = 0; i < 8; i++) roundKeys[24 + i] = k[7 - i];
    }

    // F-функция (раундовая функция сети Фейстеля).
    private int F(int x) {
        int y = 0;

        // Обрабатываем 8 4-битных фрагментов слова (слева направо)
        for (int i = 0; i < 8; i++) {
            int nibble = (x >> (28 - 4 * i)) & 0xF;

            // Применяем замену через i-й S-блок
            int s = S[i][nibble];
            y |= (s << (28 - 4 * i));
        }

        // Циклический сдвиг влево на 11 бит
        return Integer.rotateLeft(y, 11);
    }

    // Шифрование одного 64-битного блока (основная функция сети Фейстеля).
    private void encryptBlock(byte[] in, int inOff, byte[] out, int outOff) {
        ByteBuffer bb = ByteBuffer.wrap(in, inOff, 8).order(ByteOrder.BIG_ENDIAN);
        int n1 = bb.getInt();
        int n2 = bb.getInt();

        // 32 раунда сети Фейстеля
        for (int i = 0; i < 32; i++) {
            int t = n1;
            n1 = n2 ^ F(n1 ^ roundKeys[i]);
            n2 = t;
        }

        bb = ByteBuffer.wrap(out, outOff, 8).order(ByteOrder.BIG_ENDIAN);
        bb.putInt(n1);
        bb.putInt(n2);
    }

    // Шифрование данных в режиме CFB (гаммирование с обратной связью).
    /* Гамма генерируется шифрованием регистра (начинается с IV)
     Каждый байт открытого текста XOR-ится с первым байтом гаммы
     Регистр сдвигается, и в конец добавляется байт шифротекста */
    public byte[] encrypt(byte[] data) {
        if (data == null || data.length == 0) return new byte[0];

        byte[] result = new byte[data.length];

        // Инифиализация регистра IV
        byte[] reg = iv.clone();

        for (int i = 0; i < data.length; i++) {

            // Генерация 8-байтной гаммы
            byte[] gamma = new byte[8];
            encryptBlock(reg, 0, gamma, 0);

            // XOR открытого текста с первым байтом гаммы
            result[i] = (byte) (data[i] ^ gamma[0]);

            // Обновление регистра
            System.arraycopy(reg, 1, reg, 0, 7);
            reg[7] = result[i];
        }
        return result;
    }

    // Расшифрование данных в режиме CFB.
    public byte[] decrypt(byte[] data) {
        if (data == null || data.length == 0) return new byte[0];
        byte[] result = new byte[data.length];

        // Инициализируем регистр IV
        byte[] reg = iv.clone();

        for (int i = 0; i < data.length; i++) {

            // Генерация гаммы (аналогично шифрованию)
            byte[] gamma = new byte[8];
            encryptBlock(reg, 0, gamma, 0);

            // XOR шифротекста с первым байтом гаммы
            result[i] = (byte) (data[i] ^ gamma[0]);

            // Обновление регистра
            System.arraycopy(reg, 1, reg, 0, 7);
            reg[7] = data[i];
        }
        return result;
    }
}
