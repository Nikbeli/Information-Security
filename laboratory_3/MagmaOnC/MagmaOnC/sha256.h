#pragma once

#include <stdint.h>
#include <stddef.h>

#define SHA256_DIGEST_SIZE 32

typedef struct {
    uint32_t state[8];
    uint64_t count;
    uint8_t buffer[64];
} SHA256_CTX;

void sha256_init(SHA256_CTX* ctx);
void sha256_update(SHA256_CTX* ctx, const uint8_t* data, size_t len);
void sha256_final(SHA256_CTX* ctx, uint8_t digest[SHA256_DIGEST_SIZE]);

// Удобная обёртка: хэширует всё за один вызов
void sha256_hash(const uint8_t* data, size_t len, uint8_t out[SHA256_DIGEST_SIZE]);