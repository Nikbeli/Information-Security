#define _CRT_SECURE_NO_WARNINGS

#include <windows.h>
#include "md5.h"
#include <stdio.h>

#define ID_TEXTBOX 1
#define ID_BUTTON_TEXT 2
#define ID_BUTTON_FILE 3
#define ID_RESULT 4
#define ID_BUTTON_SAVE 5
#define ID_BUTTON_NEXT 6

LRESULT CALLBACK WndProc(HWND, UINT, WPARAM, LPARAM);
void ComputeMD5Text(HWND);
void ComputeMD5File(HWND);
void SaveResultToFile(HWND);
void ShowInfoScreen(HWND);
void ShowMainScreen(HWND);

BOOL mainScreenShown = FALSE;

char lastHash[256] = "";


// --- Точка входа программы с GUI (Регистрация окна) --- //
int WINAPI WinMain(HINSTANCE hInst, HINSTANCE hPrev, LPSTR args, int ncmdshow) {
	WNDCLASSW wc = { 0 };
	wc.hbrBackground = (HBRUSH)COLOR_WINDOW;
	wc.hCursor = LoadCursor(NULL, IDC_ARROW);
	wc.hInstance = hInst;
	wc.lpszClassName = L"MD5Application";
	wc.lpfnWndProc = WndProc;
	RegisterClassW(&wc);

	CreateWindowW(L"MD5Application", L"MD5 Hash Analyzer (C)",
		WS_OVERLAPPEDWINDOW | WS_VISIBLE,
		100, 100, 600, 400, NULL, NULL, NULL, NULL);

	MSG msg = { 0 };
	while (GetMessage(&msg, NULL, 0, 0)) {
		TranslateMessage(&msg);
		DispatchMessage(&msg);
	}

	return 0;
}


// --- Обработчик событий (кнопки, окна, ввод) --- //
LRESULT CALLBACK WndProc(HWND hwnd, UINT msg, WPARAM wp, LPARAM lp) {
	static HWND hTextBox, hButtonText, hButtonFile, hResult, hButtonSave, hInfo;

	switch (msg) {
	case WM_CREATE:
		ShowInfoScreen(hwnd);
		break;

	case WM_COMMAND:
		switch (LOWORD(wp)) {
		case ID_BUTTON_NEXT:
			if (!mainScreenShown) {
				mainScreenShown = TRUE;
				ShowMainScreen(hwnd);
			}

			break;

		case ID_BUTTON_TEXT:
			ComputeMD5Text(hwnd);
			break;

		case ID_BUTTON_FILE:
			ComputeMD5File(hwnd);
			break;

		case ID_BUTTON_SAVE:
			SaveResultToFile(hwnd);
			break;
		}

		break;

	case WM_DESTROY:
		PostQuitMessage(0);
		break;
	}

	return DefWindowProcW(hwnd, msg, wp, lp);
}


// === Экран со справочной информацией === //
void ShowInfoScreen(HWND hwnd) {
	CreateWindowW(L"static",
		L"Студент гр. ПИбд-41 Белянин Никита Николаевич\nВариант: №6\n\n"
		L"\nАлгоритм MD5 — это криптографическая хэш-функция,\n"
		L"создающая 128-битное (32-символьное) представление данных.\n\n"
		L"Принцип работы:\n"
		L"1️. Дополнение входного сообщения (padding)\n"
		L"2️. Добавление длины сообщения\n"
		L"3️. Инициализация буфера (A, B, C, D)\n"
		L"4️. Обработка по блокам по 512 бит\n"
		L"5️. Формирование 128-битного хэша\n\n"
		L"Используется для проверки целостности данных и цифровых подписей.",
		WS_VISIBLE | WS_CHILD | ES_LEFT, 20, 20, 540, 240,
		hwnd, NULL, NULL, NULL);

	CreateWindowW(L"button", L"Перейти к программе",
		WS_VISIBLE | WS_CHILD | WS_BORDER,
		200, 280, 180, 35, hwnd, (HMENU)ID_BUTTON_NEXT, NULL, NULL);
}


// === Основное окно для хэширования === //
void ShowMainScreen(HWND hwnd) {
	// Очистить предыдущее содержимое
	HWND hChild = GetWindow(hwnd, GW_CHILD);

	while (hChild) {
		DestroyWindow(hChild);
		hChild = GetWindow(hwnd, GW_CHILD);
	}

	// === Поле ввода текста === //
	CreateWindowW(L"static", L"Введите текст: ",
		WS_VISIBLE | WS_CHILD, 20, 20, 120, 20, hwnd, NULL, NULL, NULL);

	CreateWindowW(L"edit", L"",
		WS_VISIBLE | WS_CHILD | WS_BORDER, 20, 50, 440, 25, hwnd, (HMENU)ID_TEXTBOX, NULL, NULL);

	// --- Кнопки --- //
	CreateWindowW(L"button", L"Хэшировать текст",
		WS_VISIBLE | WS_CHILD, 20, 90, 180, 30, hwnd, (HMENU)ID_BUTTON_TEXT, NULL, NULL);

	CreateWindowW(L"button", L"Хэшировать файл",
		WS_VISIBLE | WS_CHILD, 220, 90, 180, 30, hwnd, (HMENU)ID_BUTTON_FILE, NULL, NULL);

	CreateWindowW(L"button", L"Сохранить результат", WS_VISIBLE | WS_CHILD,
		400, 200, 160, 30, hwnd, (HMENU)ID_BUTTON_SAVE, NULL, NULL);

	// --- Результат --- //
	CreateWindowW(L"edit", L"", WS_VISIBLE | WS_CHILD | WS_BORDER | ES_READONLY,
		20, 140, 440, 25, hwnd, (HMENU)ID_RESULT, NULL, NULL);
}


// --- Обработка действий пользователя --- //
void ComputeMD5Text(HWND hwnd) {
	wchar_t input[256];
	char buffer[512];
	GetDlgItemTextW(hwnd, ID_TEXTBOX, input, 256);

	char text[256];
	wcstombs(text, input, sizeof(text));

	char* hash = MD5String(text);
	wsprintfA(buffer, "%s", hash);
	SetDlgItemTextA(hwnd, ID_RESULT, buffer);
	strncpy_s(lastHash, sizeof(lastHash), hash, _TRUNCATE);
	free(hash);
}


void ComputeMD5File(HWND hwnd) {
	OPENFILENAMEW ofn = { 0 };
	wchar_t file[260] = L"";
	file[0] = '\0';

	ofn.lStructSize = sizeof(ofn);
	ofn.hwndOwner = hwnd;
	ofn.lpstrFilter = L"All Files\0*.*\0";
	ofn.lpstrFile = file;
	ofn.nMaxFile = sizeof(file);
	ofn.Flags = OFN_FILEMUSTEXIST | OFN_PATHMUSTEXIST;

	if (GetOpenFileNameW(&ofn)) {
		char path[260];
		wcstombs(path, file, sizeof(path));
		char* hash = MD5File(path);
		SetDlgItemTextA(hwnd, ID_RESULT, hash);
		strncpy_s(lastHash, sizeof(lastHash), hash, _TRUNCATE);
		free(hash);
	}
}


void SaveResultToFile(HWND hwnd) {
	if (strlen(lastHash) == 0) {
		MessageBoxW(hwnd, L"Нет данных для сохранения!", L"Ошибка", MB_ICONERROR);
		return;
	}

	OPENFILENAMEW ofn = { 0 };
	wchar_t file[260] = L"hash_result.txt";
	ofn.lStructSize = sizeof(ofn);
	ofn.hwndOwner = hwnd;
	ofn.lpstrFilter = L"Text Files\0*.txt\0All Files\0*.*\0";
	ofn.lpstrFile = file;
	ofn.nMaxFile = sizeof(file);
	ofn.Flags = OFN_OVERWRITEPROMPT;

	if (GetSaveFileNameW(&ofn)) {
		FILE* f;
		char path[260];
		wcstombs(path, file, sizeof(path));
		f = fopen(path, "w");

		if (f) {
			fprintf(f, "MD5 Hash Result:\n%s\n", lastHash);
			fclose(f);
			MessageBoxW(hwnd, L"Результат успешно сохранён!", L"Готово", MB_ICONINFORMATION);
		}
	}
}
