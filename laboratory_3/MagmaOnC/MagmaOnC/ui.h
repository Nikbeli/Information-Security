#pragma once

#ifndef UI_H
#define UI_H

#include <Windows.h>
#include <stdint.h>

// Запуск GUI
int run_gui(HINSTANCE hInstance, int nCmdShow);

// Функция логирования для GUI
void append_log(const wchar_t* fmt, ...);

#endif
