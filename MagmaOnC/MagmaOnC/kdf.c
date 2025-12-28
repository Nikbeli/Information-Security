#include "kdf.h"
#include "sha256.h"


void kdf_sha256(const uint8_t* input, size_t input_len, uint8_t out[32]) {
    sha256_hash(input, input_len, out);
}