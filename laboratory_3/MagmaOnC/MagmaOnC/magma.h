#pragma once

#ifndef MAGMA_H
#define MAGMA_H

#include <stdint.h>
#include <stddef.h>

#define MAGMA_BLOCK_SIZE 8U

typedef struct {
	// 8 * 32 = 256 бит
	uint32_t k[8];
} magma_key_t;


// === Инициализация ключа (32 байта) === //
void MagmaSetKey(magma_key_t* key, const uint8_t user_key[32]);

// === Шифрование одного блока (8 байт) === //
void MagmaEncryptBlock(const magma_key_t* key, const uint8_t in[8], uint8_t out[8]);

// === Расшифровка одного блока (8 байт) === //
void MagmaDecryptBlock(const magma_key_t* key, const uint8_t in[8], uint8_t out[8]);

#endif