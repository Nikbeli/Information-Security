#include "cipher.h"
#include <string.h>


// === Инициализация === //
void magma_cfb_initialization(magma_cfb_t *ctx, const uint8_t key_raw[32], const uint8_t iv[8]) {
	MagmaSetKey(&ctx->key, key_raw);
	memcpy(ctx->reg, iv, MAGMA_BLOCK_SIZE);
}


/* Шифрование Gamma = E(R) обрабатываем по-байтово, 
   регистр перемещается на 1 байт и добавляется новый шифротекст. */
void magma_cfb_encrypt_stream(magma_cfb_t* ctx, const uint8_t* in, uint8_t* out, size_t len) {
	uint8_t gamma[MAGMA_BLOCK_SIZE];

	for (size_t i = 0; i < len; ++i) {
		MagmaEncryptBlock(&ctx->key, ctx->reg, gamma);
		out[i] = in[i] ^ gamma[0];

		// сдвиг влево на один байт
		memmove(ctx->reg, ctx->reg + 1, MAGMA_BLOCK_SIZE - 1);
		ctx->reg[MAGMA_BLOCK_SIZE - 1] = out[i];
	}
}


/* Расшифрование Gamma = E(R) отличие в том, что при обновлении регистра добавляем входной C(i)*/
void magma_cfb_decrypt_stream(magma_cfb_t* ctx, const uint8_t* in, uint8_t* out, size_t len) {
	uint8_t gamma[MAGMA_BLOCK_SIZE];

	for (size_t i = 0; i < len; ++i) {
		MagmaEncryptBlock(&ctx->key, ctx->reg, gamma);
		out[i] = in[i] ^ gamma[0];
		memmove(ctx->reg, ctx->reg + 1, MAGMA_BLOCK_SIZE - 1);
		
		// используем байт зашифрованного текста
		ctx->reg[MAGMA_BLOCK_SIZE - 1] = in[i];
	}
}