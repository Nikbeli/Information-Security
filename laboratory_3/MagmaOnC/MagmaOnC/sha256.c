#include "sha256.h"
#include <string.h>

static const uint32_t K[64] = {
    0x428a2f98, 0x71374491, 0xb5c0fbcf, 0xe9b5dba5, 0x3956c25b, 0x59f111f1, 0x923f82a4, 0xab1c5ed5,
    0xd807aa98, 0x12835b01, 0x243185be, 0x550c7dc3, 0x72be5d74, 0x80deb1fe, 0x9bdc06a7, 0xc19bf174,
    0xe49b69c1, 0xefbe4786, 0x0fc19dc6, 0x240ca1cc, 0x2de92c6f, 0x4a7484aa, 0x5cb0a9dc, 0x76f988da,
    0x983e5152, 0xa831c66d, 0xb00327c8, 0xbf597fc7, 0xc6e00bf3, 0xd5a79147, 0x06ca6351, 0x14292967,
    0x27b70a85, 0x2e1b2138, 0x4d2c6dfc, 0x53380d13, 0x650a7354, 0x766a0abb, 0x81c2c92e, 0x92722c85,
    0xa2bfe8a1, 0xa81a664b, 0xc24b8b70, 0xc76c51a3, 0xd192e819, 0xd6990624, 0xf40e3585, 0x106aa070,
    0x19a4c116, 0x1e376c08, 0x2748774c, 0x34b0bcb5, 0x391c0cb3, 0x4ed8aa4a, 0x5b9cca4f, 0x682e6ff3,
    0x748f82ee, 0x78a5636f, 0x84c87814, 0x8cc70208, 0x90befffa, 0xa4506ceb, 0xbef9a3f7, 0xc67178f2
};

#define Ch(x, y, z)  ((x & y) ^ (~x & z))
#define Maj(x, y, z) ((x & y) ^ (x & z) ^ (y & z))
#define RotR(x, n)   ((x >> n) | (x << (32 - n)))
#define Sigma0(x)    (RotR(x, 2) ^ RotR(x, 13) ^ RotR(x, 22))
#define Sigma1(x)    (RotR(x, 6) ^ RotR(x, 11) ^ RotR(x, 25))
#define sigma0(x)    (RotR(x, 7) ^ RotR(x, 18) ^ (x >> 3))
#define sigma1(x)    (RotR(x, 17) ^ RotR(x, 19) ^ (x >> 10))

static const uint32_t H[8] = {
    0x6a09e667, 0xbb67ae85, 0x3c6ef372, 0xa54ff53a,
    0x510e527f, 0x9b05688c, 0x1f83d9ab, 0x5be0cd19
};

void sha256_init(SHA256_CTX* ctx) {
    memcpy(ctx->state, H, sizeof(H));
    ctx->count = 0;
    memset(ctx->buffer, 0, sizeof(ctx->buffer));
}

void sha256_transform(SHA256_CTX* ctx, const uint8_t* block) {
    uint32_t W[64];
    uint32_t S[8];
    int i;

    for (i = 0; i < 16; ++i) {
        W[i] = ((uint32_t)block[i * 4 + 0] << 24) |
            ((uint32_t)block[i * 4 + 1] << 16) |
            ((uint32_t)block[i * 4 + 2] << 8) |
            ((uint32_t)block[i * 4 + 3]);
    }

    for (; i < 64; ++i) {
        W[i] = sigma1(W[i - 2]) + W[i - 7] + sigma0(W[i - 15]) + W[i - 16];
    }

    memcpy(S, ctx->state, 32);

    for (i = 0; i < 64; ++i) {
        uint32_t T1 = S[7] + Sigma1(S[4]) + Ch(S[4], S[5], S[6]) + K[i] + W[i];
        uint32_t T2 = Sigma0(S[0]) + Maj(S[0], S[1], S[2]);
        memmove(&S[1], &S[0], 7 * sizeof(uint32_t));
        S[4] += T1;
        S[0] = T1 + T2;
    }

    for (i = 0; i < 8; ++i) {
        ctx->state[i] += S[i];
    }
}

void sha256_update(SHA256_CTX* ctx, const uint8_t* data, size_t len) {
    size_t i, j;

    j = (size_t)((ctx->count >> 3) & 0x3F);
    ctx->count += (uint64_t)len << 3;

    if ((j + len) > 63) {

        memcpy(&ctx->buffer[j], data, (i = 64 - j));
        sha256_transform(ctx, ctx->buffer);

        for (; i + 63 < len; i += 64) {
            sha256_transform(ctx, &data[i]);
        }
        j = 0;
    }
    else {
        i = 0;
    }

    memcpy(&ctx->buffer[j], &data[i], len - i);
}

void sha256_final(SHA256_CTX* ctx, uint8_t digest[SHA256_DIGEST_SIZE]) {
    uint64_t bitlen = ctx->count;
    size_t i = (size_t)((bitlen >> 3) & 0x3F);
    ctx->buffer[i++] = 0x80;

    if (i > 56) {
        memset(&ctx->buffer[i], 0, 64 - i);
        sha256_transform(ctx, ctx->buffer);
        memset(ctx->buffer, 0, 56);
    }
    else {
        memset(&ctx->buffer[i], 0, 56 - i);
    }

    ctx->buffer[56] = (uint8_t)(bitlen >> 56);
    ctx->buffer[57] = (uint8_t)(bitlen >> 48);
    ctx->buffer[58] = (uint8_t)(bitlen >> 40);
    ctx->buffer[59] = (uint8_t)(bitlen >> 32);
    ctx->buffer[60] = (uint8_t)(bitlen >> 24);
    ctx->buffer[61] = (uint8_t)(bitlen >> 16);
    ctx->buffer[62] = (uint8_t)(bitlen >> 8);
    ctx->buffer[63] = (uint8_t)(bitlen);

    sha256_transform(ctx, ctx->buffer);

    for (i = 0; i < 8; ++i) {
        digest[i * 4 + 0] = (uint8_t)(ctx->state[i] >> 24);
        digest[i * 4 + 1] = (uint8_t)(ctx->state[i] >> 16);
        digest[i * 4 + 2] = (uint8_t)(ctx->state[i] >> 8);
        digest[i * 4 + 3] = (uint8_t)(ctx->state[i]);
    }
}

void sha256_hash(const uint8_t* data, size_t len, uint8_t out[SHA256_DIGEST_SIZE]) {
    SHA256_CTX ctx;
    sha256_init(&ctx);
    sha256_update(&ctx, data, len);
    sha256_final(&ctx, out);
}