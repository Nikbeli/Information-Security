#define UNICODE
#define _WIN32_WINNT 0x0600

#include <Windows.h>
#include <commctrl.h> 
#include "ui.h"

#pragma comment(lib, "comctl32.lib")  // ← линковка библиотеки

int WINAPI wWinMain(HINSTANCE hInstance, HINSTANCE hPrevInstance, PWSTR pCmdLine, int nCmdShow) {
	(void)hPrevInstance;
	(void)pCmdLine;

	// Инициализация Common Controls (для TaskDialog)
	INITCOMMONCONTROLSEX icc = { sizeof(icc), ICC_STANDARD_CLASSES };
	InitCommonControlsEx(&icc);

	return run_gui(hInstance, nCmdShow);
}