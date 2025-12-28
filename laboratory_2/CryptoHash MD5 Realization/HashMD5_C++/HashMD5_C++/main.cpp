#define _CRT_SECURE_NO_WARNINGS
#include <windows.h>
#include <commdlg.h>
#include <string>
#include <fstream>
#include <chrono>
#include <ctime>
#include "md5.h"

LRESULT CALLBACK WndProc(HWND, UINT, WPARAM, LPARAM);
void ShowReference();
void SaveHashToFile(const std::string& hash, const std::wstring& source);

// элементы интерфейса
HWND hEditInput, hEditOutput, hBtnText, hBtnFile, hBtnInfo;

int WINAPI WinMain(HINSTANCE hInstance, HINSTANCE, LPSTR, int nCmdShow) {
    const wchar_t CLASS_NAME[] = L"MD5HashApp";

    WNDCLASS wc = {};
    wc.lpfnWndProc = WndProc;
    wc.hInstance = hInstance;
    wc.lpszClassName = CLASS_NAME;
    wc.hbrBackground = (HBRUSH)(COLOR_WINDOW + 1);

    RegisterClass(&wc);

    HWND hwnd = CreateWindowEx(
        0,
        CLASS_NAME,
        L"MD5 Hash Analyzer (WinAPI)",
        WS_OVERLAPPEDWINDOW ^ WS_THICKFRAME,
        CW_USEDEFAULT, CW_USEDEFAULT, 500, 300,
        nullptr, nullptr, hInstance, nullptr
    );

    if (!hwnd) return 0;
    ShowWindow(hwnd, nCmdShow);

    MSG msg = {};
    while (GetMessage(&msg, nullptr, 0, 0)) {
        TranslateMessage(&msg);
        DispatchMessage(&msg);
    }
    return (int)msg.wParam;
}

// === создание окна и элементов ===
LRESULT CALLBACK WndProc(HWND hwnd, UINT msg, WPARAM wParam, LPARAM lParam) {
    switch (msg) {
    case WM_CREATE: {
        CreateWindow(L"STATIC", L"Введите текст:", WS_VISIBLE | WS_CHILD,
            20, 20, 100, 20, hwnd, nullptr, nullptr, nullptr);

        hEditInput = CreateWindow(L"EDIT", L"", WS_VISIBLE | WS_CHILD | WS_BORDER,
            130, 20, 320, 25, hwnd, nullptr, nullptr, nullptr);

        hBtnText = CreateWindow(L"BUTTON", L"Хэшировать текст", WS_VISIBLE | WS_CHILD,
            20, 60, 150, 30, hwnd, (HMENU)1, nullptr, nullptr);

        hBtnFile = CreateWindow(L"BUTTON", L"Хэшировать файл", WS_VISIBLE | WS_CHILD,
            180, 60, 150, 30, hwnd, (HMENU)2, nullptr, nullptr);

        hBtnInfo = CreateWindow(L"BUTTON", L"Справка", WS_VISIBLE | WS_CHILD,
            340, 60, 110, 30, hwnd, (HMENU)3, nullptr, nullptr);

        CreateWindow(L"STATIC", L"Результат MD5:", WS_VISIBLE | WS_CHILD,
            20, 110, 120, 20, hwnd, nullptr, nullptr, nullptr);

        hEditOutput = CreateWindow(L"EDIT", L"", WS_VISIBLE | WS_CHILD | WS_BORDER | ES_AUTOHSCROLL,
            150, 110, 300, 25, hwnd, nullptr, nullptr, nullptr);
        break;
    }

    case WM_COMMAND: {
        switch (LOWORD(wParam)) {
        case 1: { // хэшировать текст
            wchar_t buffer[1024];
            GetWindowText(hEditInput, buffer, 1024);
            std::wstring text(buffer);
            if (text.empty()) {
                MessageBox(hwnd, L"Введите текст для хэширования!", L"Ошибка", MB_ICONWARNING);
                break;
            }

            MD5 md5;
            md5.update(std::string(text.begin(), text.end()));
            std::string result = md5.finalize();
            std::wstring wresult(result.begin(), result.end());
            SetWindowText(hEditOutput, wresult.c_str());
            SaveHashToFile(result, L"Введённый текст");
            break;
        }

        case 2: { // хэшировать файл
            OPENFILENAME ofn = {};
            wchar_t fileName[MAX_PATH] = L"";
            ofn.lStructSize = sizeof(ofn);
            ofn.hwndOwner = hwnd;
            ofn.lpstrFile = fileName;
            ofn.nMaxFile = MAX_PATH;
            ofn.lpstrFilter = L"Все файлы\0*.*\0";
            ofn.Flags = OFN_FILEMUSTEXIST;

            if (GetOpenFileName(&ofn)) {
                try {
                    std::string path(fileName, fileName + wcslen(fileName));
                    std::string result = MD5::fromFile(path);
                    std::wstring wresult(result.begin(), result.end());
                    SetWindowText(hEditOutput, wresult.c_str());
                    SaveHashToFile(result, fileName);
                }
                catch (const std::exception& e) {
                    MessageBoxA(hwnd, e.what(), "Ошибка", MB_ICONERROR);
                }
            }
            break;
        }

        case 3:
            ShowReference();
            break;
        }
        break;
    }

    case WM_DESTROY:
        PostQuitMessage(0);
        break;
    }
    return DefWindowProc(hwnd, msg, wParam, lParam);
}

// === окно со справочной информацией ===
void ShowReference() {
    MessageBox(
        nullptr,
        L"Студент гр. ПИбд-41 Белянин Никита Николаевич\nВариант: №6\n\n"
        L"\nАлгоритм MD5 — это криптографическая хэш-функция,\n"
        L"создающая 128-битное (32-символьное) представление данных.\n\n"
        L"MD5 (Message Digest 5)\n\n"
        L"• Разработан Рональдом Ривестом в 1991 году\n"
        L"• Длина хэша: 128 бит (32 шестнадцатеричных символа)\n"
        L"• Применяется для проверки целостности данных\n"
        L"• Не рекомендуется для криптографической защиты\n"
        L"\nПрограмма позволяет вычислить MD5 хэш строки или файла.",
        L"Справочная информация",
        MB_ICONINFORMATION
    );
}

// === сохранение хэша в файл ===
void SaveHashToFile(const std::string& hash, const std::wstring& source) {
    std::ofstream file("md5_result.txt", std::ios::app);
    if (!file.is_open()) return;

    // текущее время
    auto now = std::chrono::system_clock::now();
    std::time_t now_c = std::chrono::system_clock::to_time_t(now);

    file << "=== Result MD5 ===\n";
    file << "Date and time: " << std::ctime(&now_c);

    file << "hash: " << hash << "\n";
    file << "=======================\n\n";

    file.close();

    MessageBox(nullptr, L"Результат успешно сохранён в файлик",
        L"Сохранение", MB_OK | MB_ICONINFORMATION);
    file << "MD5: " << hash << "\n";
}
