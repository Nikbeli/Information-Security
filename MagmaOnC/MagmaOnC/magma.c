#include "magma.h"
#include <string.h>

// Реализация стандарта Магма (реализованы корректная упаковка/распаковка, S-блоки и 32 раунда сети Фейстеля
// Здесь используется представление: входной блок 64-бита разбивается на N1 и N2


static const uint8_t Sbox[8][16] = {
    {0xF,0xC,0x2,0xA,0x6,0x4,0x5,0x0,0x7,0x9,0xE,0xD,0x1,0xB,0x8,0x3},
    {0xB,0x6,0x3,0x4,0xC,0xF,0xE,0x2,0x7,0xD,0x8,0x0,0x5,0xA,0x9,0x1},
    {0x1,0xC,0xB,0x0,0xF,0xE,0x6,0x5,0xA,0xD,0x4,0x8,0x9,0x3,0x7,0x2},
    {0x1,0x5,0xE,0xC,0xA,0x7,0x0,0xD,0x6,0x2,0xB,0x4,0x9,0x3,0xF,0x8},
    {0x0,0xC,0x8,0x9,0xD,0x2,0xA,0xB,0x7,0x3,0x6,0x5,0x4,0xE,0xF,0x1},
    {0x8,0x0,0xF,0x3,0x2,0x5,0xE,0xB,0x1,0xA,0x4,0x7,0xC,0x9,0xD,0x6},
    {0x3,0x0,0x6,0xF,0x1,0xE,0x9,0x2,0xD,0x8,0xC,0x4,0xB,0xA,0x5,0x7},
    {0x1,0xA,0x6,0x8,0xF,0xB,0x0,0x4,0xC,0x3,0x5,0x9,0x7,0xD,0x2,0xE}
};

// Циклический поворот 32 бит
static inline uint32_t rol32(uint32_t x, unsigned n) {
    return (x << n) | (x >> (32 - n));
}

// S-образная замена 32-разрядного слова ( как 8 фрагментов)
static uint32_t substitute32(uint32_t x) {
    // блоки
    uint8_t nibbles[8];

    for (int i = 0; i < 8; ++i) {
        nibbles[i] = (x >> (28 - 4 * i)) & 0x0F;
    }

    for (int i = 0; i < 8; ++i) {
        nibbles[i] = Sbox[i][nibbles[i]];
    }

    uint32_t y = 0;

    for (int i = 0; i < 8; ++i) {
        y |= ((uint32_t)nibbles[i] << (28 - 4 * i));
    }

    return y;
}

// === F-функция Магмы === //
static uint32_t F(uint32_t x, uint32_t k) {
    // Сложение по модулю 2^32
    uint32_t t = x + k;
    uint32_t s = substitute32(t);
    return rol32(s, 11);
}

// Установка ключа
void MagmaSetKey(magma_key_t* key, const uint8_t user_key[32]) {
    // Ключ в порядке возрастания: ключ[0]
    for (int i = 0; i < 8; ++i) {
        uint32_t w = ((uint32_t)user_key[4 * i + 0] << 24) |
            ((uint32_t)user_key[4 * i + 1] << 16) |
            ((uint32_t)user_key[4 * i + 2] << 8) |
            ((uint32_t)user_key[4 * i + 3]);

        key->k[i] = w;
    }
}

// === Внутренний общий код 32-раундовой сети Фейстеля === //
static void MagmaFeistel32(const magma_key_t* key, uint32_t* N1, uint32_t* N2, int decrypt) {
    uint32_t n1 = *N1, n2 = *N2;

    if (!decrypt) {
        // Шифрование: K0-K7 повторяется 3 раза, затем K7-K0
        for (int r = 0; r < 24; ++r) {
            uint32_t t = n1;
            n1 = n2 ^ F(n1, key->k[r % 8]);
            n2 = t;
        }

        for (int r = 7; r >= 0; --r) {
            uint32_t t = n1;
            n1 = n2 ^ F(n1, key->k[r]);
            n2 = t;
        }
    }
    else {
        // Расшифровка выполняется по обратному процессу
        for (int r = 0; r < 8; ++r) {
            uint32_t t = n1;
            n1 = n2 ^ F(n1, key->k[r]);
            n2 = t;
        }

        for (int r = 31; r >= 8; --r) {
            uint32_t t = n1;
            n1 = n2 ^ F(n1, key->k[r % 8]);
            n2 = t;
        }
    }

    // После раундов результат запишем в регистры
    *N1 = n1;
    *N2 = n2;
}


// Упаковывать/распаковывать 32-разрядные слова с большими порядковыми номерами в байты или из них
static inline uint32_t be32(const uint8_t b[4]) {
    return ((uint32_t)b[0] << 24) | ((uint32_t)b[1] << 16) | ((uint32_t)b[2] << 8) | (uint32_t)b[3];
}

static inline void store_be32(uint8_t out[4], uint32_t v) {
    out[0] = (uint8_t)(v >> 24);
    out[1] = (uint8_t)(v >> 16);
    out[2] = (uint8_t)(v >> 8);
    out[3] = (uint8_t)(v);
}

// === Шифрование и расшифрование через сеть Фейстеля === //
void MagmaEncryptBlock(const magma_key_t* key, const uint8_t in[8], uint8_t out[8]) {
    uint32_t n1 = be32(in + 0);
    uint32_t n2 = be32(in + 4);
    MagmaFeistel32(key, &n1, &n2, 0);

    // Вывод в том же порядке: 8 байт
    store_be32(out + 0, n1);
    store_be32(out + 4, n2);
}

void MagmaDecryptBlock(const magma_key_t* key, const uint8_t in[8], uint8_t out[8]) {
    uint32_t n1 = be32(in + 0);
    uint32_t n2 = be32(in + 4);
    MagmaFeistel32(key, &n1, &n2, 1);

    store_be32(out + 0, n1);
    store_be32(out + 4, n2);
}

