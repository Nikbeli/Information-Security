#pragma once

#ifndef CIPHER_H
#define CIPHER_H

#include <stdint.h>
#include <stddef.h>
#include "magma.h"

// === Структура CFB-контекста (сохранение в регистр) === //
typedef struct {
	magma_key_t key;
	uint8_t reg[MAGMA_BLOCK_SIZE]; // 8 байт регистр (IV)
} magma_cfb_t;


// === Инициализация key_rav 32 байта, IV 8 байт === //
void magma_cfb_initialization(magma_cfb_t *ctx, const uint8_t key_raw[32], const uint8_t iv[8]);

// === Шифрование/дешифрование потоков произвольной длины === //
// decrypt и encrypt используют разные обновления регистра 
void magma_cfb_encrypt_stream(magma_cfb_t *ctx, const uint8_t *in, uint8_t *out, size_t len);

void magma_cfb_decrypt_stream(magma_cfb_t* ctx, const uint8_t* in, uint8_t* out, size_t len);

#endif