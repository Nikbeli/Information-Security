#pragma once

#include <stdint.h>
#include <stddef.h>

// Безопасное получение 32-байтного ключа через SHA-256
void kdf_sha256(const uint8_t* input, size_t input_len, uint8_t out[32]);