#pragma once

#ifndef FILE_IO_H
#define FILE_IO_H

#include <stddef.h>
#include <stdint.h>

/* Простая функция чтения файла в динамический буфер.
   Возвращает указатель длину через outlen. Возвращает NULL при ошибке. */
uint8_t* read_file(const char *path, size_t *outlen);

uint8_t* read_file_w(const wchar_t* path, size_t* outlen);

// Запись буфера в файл. Возвращает 1 при успехе, 0 при ошибке
int write_file(const char *path, const uint8_t *buf, size_t len);

/* Поблочная обработка (считывание входа и запись резульатат с вызовом 
   для обработки блоков */
typedef void (*process_block_cb)(void *ctx, const uint8_t *in, uint8_t *out, size_t n);

int process_file_stream_w(const wchar_t *inpath, const wchar_t *outpath, void *ctx_cb, process_block_cb cb);

#endif