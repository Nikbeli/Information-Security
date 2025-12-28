#pragma once

#ifndef  MD5_H
#define MD5_H

#include <stdint.h>
#include <stddef.h>

// === Контекст MD5 (состояние вычислений) === //
/*	state - 4 регистра (a,b,c,d); 
	count - количество обработанных бит 
	buffer - текущий блок данных (512 бит) */
typedef struct {
	uint32_t state[4];
	uint32_t count[2];
	unsigned char buffer[64];
} MD5_CTX;


// ===== Основные функции ===== //

// Инициализация контекста (регистры)
void MD5Init(MD5_CTX* context);

// Добавление данных
void MD5Update(MD5_CTX* context, const unsigned char* input, size_t inputLen);

// Завершение вычисления
void MD5Final(unsigned char digest[16], MD5_CTX* context);

// Один раунд преобразования блока
void MD5Transform(uint32_t state[4], const unsigned char block[64]);

// Хэширование строки и хэширование файла
void *MD5String(const char* string);
void *MD5File(const char* filename);

#endif
