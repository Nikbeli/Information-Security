#include "fileIO.h"
#include <stdio.h>
#include <stdlib.h>

uint8_t* read_file(const char *path, size_t *outlen) {
    FILE* f = fopen(path, "rb");

    if (!f) return NULL;

    if (fseek(f, 0, SEEK_END) != 0) { 
        fclose(f); return NULL; 
    }

    long sz = ftell(f);

    if (sz < 0) { 
        fclose(f); return NULL; 
    }

    rewind(f);
    uint8_t* buf = (uint8_t*)malloc((size_t)sz + 1);

    if (!buf) { 
        fclose(f); return NULL; 
    }

    size_t r = fread(buf, 1, (size_t)sz, f);
    fclose(f);

    if (r != (size_t)sz) { 
        free(buf); return NULL; 
    }

    *outlen = (size_t)sz;
    return buf;
}

uint8_t* read_file_w(const wchar_t* path, size_t* outlen) {
    if (!path || !outlen) return NULL;
    FILE* f = _wfopen(path, L"rb");
    if (!f) return NULL;

    if (_fseeki64(f, 0, SEEK_END) != 0) {
        fclose(f);
        return NULL;
    }

    __int64 sz = _ftelli64(f);
    if (sz < 0 || sz > SIZE_MAX) {
        fclose(f);
        return NULL;
    }

    _fseeki64(f, 0, SEEK_SET);

    uint8_t* buf = (uint8_t*)malloc((size_t)sz + 1);
    if (!buf) {
        fclose(f);
        return NULL;
    }

    size_t r = fread(buf, 1, (size_t)sz, f);
    fclose(f);

    if (r != (size_t)sz) {
        free(buf);
        return NULL;
    }

    *outlen = (size_t)sz;
    return buf;
}

int write_file(const char* path, const uint8_t* buf, size_t len) {
    FILE* f = fopen(path, "wb");
    if (!f) return 0;
    size_t w = fwrite(buf, 1, len, f);
    fclose(f);
    return w == len;
}

// Тип callback остаётся прежним
typedef void (*process_block_cb)(void* ctx, const uint8_t* in, uint8_t* out, size_t n);

int process_file_stream_w(const wchar_t* inpath, const wchar_t* outpath, void* ctx_cb, process_block_cb cb) {
    FILE* fin = _wfopen(inpath, L"rb");
    if (!fin) return 0;

    FILE* fout = _wfopen(outpath, L"wb");
    if (!fout) {
        fclose(fin);
        return 0;
    }

    const size_t BUF = 4096;
    uint8_t* inbuf = (uint8_t*)malloc(BUF);
    uint8_t* outbuf = (uint8_t*)malloc(BUF);
    if (!inbuf || !outbuf) {
        fclose(fin); fclose(fout);
        free(inbuf); free(outbuf);
        return 0;
    }

    size_t n;
    int ok = 1;
    while ((n = fread(inbuf, 1, BUF, fin)) > 0) {
        cb(ctx_cb, inbuf, outbuf, n);
        if (fwrite(outbuf, 1, n, fout) != n) {
            ok = 0;
            break;
        }
    }

    fclose(fin);
    fclose(fout);
    free(inbuf);
    free(outbuf);
    return ok;
}