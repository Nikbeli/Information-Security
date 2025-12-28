#define _CRT_SECURE_NO_WARNINGS
#define UNICODE
#define _WIN32_WINNT 0x0600

#include <windows.h>
#include <commdlg.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <stdarg.h>
#include <wctype.h>
#include <commctrl.h>

#include "ui.h"
#include "student_info.h"
#include "cipher.h"
#include "fileIO.h"
#include "kdf.h"

#pragma comment(linker, "\"/manifestdependency:type='win32' name='Microsoft.Windows.Common-Controls' version='6.0.0.0' processorArchitecture='*' publicKeyToken='6595b64144ccf1df' language='*'\"")
#pragma comment(lib, "comctl32.lib")

enum {
    IDC_EDIT_STUDENT = 1001,
    IDC_EDIT_VARIANT,
    IDC_EDIT_KEY,
    IDC_BTN_KEY_FILE,
    IDC_EDIT_IV,
    IDC_BTN_IN,
    IDC_BTN_OUT,
    IDC_STATIC_IN,
    IDC_STATIC_OUT,
    IDC_BTN_ENC,
    IDC_BTN_DEC,
    IDC_EDIT_LOG
};

static HINSTANCE ghInstance;
static HWND hEditStudent, hEditVariant, hEditKey, hBtnKeyFile, hEditIV;
static HWND hBtnIn, hBtnOut, hBtnEnc, hBtnDec, hEditLog, hStaticIn, hStaticOut;
static WCHAR inPath[MAX_PATH] = L"";
static WCHAR outPath[MAX_PATH] = L"";

// Вспомогательная функция: char* → wchar_t*
static wchar_t* ansi_to_wide(const char* src) {
    static wchar_t buf[512];

    if (!src) return L"";

    int len = MultiByteToWideChar(CP_UTF8, 0, src, -1, NULL, 0);
    if (len > 512) len = 512;

    MultiByteToWideChar(CP_UTF8, 0, src, -1, buf, len);
    return buf;
}

static void append_log(const wchar_t* fmt, ...) {
    wchar_t buf[1024];
    va_list ap;
    va_start(ap, fmt);
    vswprintf(buf, sizeof(buf) / sizeof(buf[0]), fmt, ap);
    va_end(ap);
    int len = GetWindowTextLengthW(hEditLog);
    SendMessageW(hEditLog, EM_SETSEL, (WPARAM)len, (LPARAM)len);
    SendMessageW(hEditLog, EM_REPLACESEL, FALSE, (LPARAM)buf);
    SendMessageW(hEditLog, EM_REPLACESEL, FALSE, (LPARAM)L"\r\n");
}

static int hex_nibble(wchar_t c) {
    if (c >= L'0' && c <= L'9') return c - L'0';
    if (c >= L'A' && c <= L'F') return c - L'A' + 10;
    if (c >= L'a' && c <= L'f') return c - L'a' + 10;

    return -1;
}

static int hexw_to_bytes(const wchar_t* whex, uint8_t* out, size_t expected_len) {
    if (!whex || !out) return 0;
    size_t byte_idx = 0;
    int hi = -1;

    for (size_t i = 0; whex[i] != L'\0' && byte_idx < expected_len; i++) {

        wchar_t wc = whex[i];
        if (wc > 127) continue;

        int nib = hex_nibble(wc);

        if (nib == -1) continue;

        if (hi == -1) {
            hi = nib;
        }
        else {
            out[byte_idx++] = (uint8_t)((hi << 4) | nib);
            hi = -1;
        }
    }

    if (hi != -1 || byte_idx != expected_len) return 0;
    return 1;
}

static size_t clean_hex_string(const wchar_t* src, wchar_t* dst, size_t dst_size) {
    size_t j = 0;

    for (size_t i = 0; src[i] != L'\0' && j + 1 < dst_size; i++) {
        wchar_t c = src[i];

        if ((c >= L'0' && c <= L'9') ||
            (c >= L'A' && c <= L'F') ||
            (c >= L'a' && c <= L'f')) {
            dst[j++] = c;
        }
    }

    dst[j] = L'\0';
    return j;
}

static void choose_open_file(HWND owner, WCHAR* outbuf) {
    OPENFILENAMEW ofn = { 0 };
    WCHAR szFile[MAX_PATH] = L"";
    ofn.lStructSize = sizeof(ofn);
    ofn.hwndOwner = owner;
    ofn.lpstrFile = szFile;
    ofn.nMaxFile = MAX_PATH;
    ofn.Flags = OFN_PATHMUSTEXIST | OFN_FILEMUSTEXIST;
    ofn.lpstrFilter = L"All Files\0*.*\0";

    if (GetOpenFileNameW(&ofn)) {
        wcscpy_s(outbuf, MAX_PATH, szFile);
    }
}

static void choose_save_file(HWND owner, WCHAR* outbuf) {
    OPENFILENAMEW ofn = { 0 };
    WCHAR szFile[MAX_PATH] = L"";
    ofn.lStructSize = sizeof(ofn);
    ofn.hwndOwner = owner;
    ofn.lpstrFile = szFile;
    ofn.nMaxFile = MAX_PATH;
    ofn.Flags = OFN_OVERWRITEPROMPT;
    ofn.lpstrFilter = L"All Files\0*.*\0";

    if (GetSaveFileNameW(&ofn)) {
        wcscpy_s(outbuf, MAX_PATH, szFile);
    }
}

typedef struct {
    magma_cfb_t* ctx;
} cb_ctx_t;

static void proc_cb_encrypt(void* ctxv, const uint8_t* in, uint8_t* out, size_t n) {
    cb_ctx_t* c = (cb_ctx_t*)ctxv;
    magma_cfb_encrypt_stream(c->ctx, in, out, n);
}
static void proc_cb_decrypt(void* ctxv, const uint8_t* in, uint8_t* out, size_t n) {
    cb_ctx_t* c = (cb_ctx_t*)ctxv;
    magma_cfb_decrypt_stream(c->ctx, in, out, n);
}

// ================= ОКНО "О СТУДЕНТЕ" =================
static void show_student_info(HWND parent) {
    WCHAR msg[1024];
    swprintf_s(msg, _countof(msg),
        L"Студент: %ls\n"
        L"Группа: %ls\n"
        L"Вариант: %ls\n\n"
        L"Задание:\n%ls",
        STUDENT_FIO, STUDENT_GROUP, STUDENT_VARIANT, STUDENT_TASK);

    TASKDIALOGCONFIG tdf = { 0 };
    tdf.cbSize = sizeof(tdf);
    tdf.hwndParent = parent;
    tdf.dwFlags = TDF_ALLOW_DIALOG_CANCELLATION | TDF_POSITION_RELATIVE_TO_WINDOW;
    tdf.pszWindowTitle = L"Курсовая работа — Магма (CFB)";
    tdf.pszMainIcon = TD_INFORMATION_ICON;
    tdf.pszMainInstruction = L"Сведения о студенте";
    tdf.pszContent = msg;
    tdf.pszExpandedInformation = L"Реализация криптографического алгоритма «Магма» (ГОСТ Р 34.12-2015) в режиме гаммирования с обратной связью.";
    tdf.pszFooter = L"ГОСТ Р 34.12-2015 | Учебное приложение";
    TaskDialogIndirect(&tdf, NULL, NULL, NULL);
}

// ===== ОСНОВНОЕ ОКНО =====
LRESULT CALLBACK WndProc(HWND hwnd, UINT msg, WPARAM wParam, LPARAM lParam) {
    switch (msg) {
    case WM_CREATE: {
        HFONT hFont = CreateFontW(16, 0, 0, 0, FW_NORMAL, FALSE, FALSE, FALSE,
            DEFAULT_CHARSET, OUT_DEFAULT_PRECIS, CLIP_DEFAULT_PRECIS, DEFAULT_QUALITY,
            DEFAULT_PITCH | FF_DONTCARE, L"Segoe UI");
        HFONT hBtnFont = CreateFontW(15, 0, 0, 0, FW_MEDIUM, FALSE, FALSE, FALSE,
            DEFAULT_CHARSET, OUT_DEFAULT_PRECIS, CLIP_DEFAULT_PRECIS, DEFAULT_QUALITY,
            DEFAULT_PITCH | FF_DONTCARE, L"Segoe UI");
        HFONT hLogFont = CreateFontW(14, 0, 0, 0, FW_NORMAL, FALSE, FALSE, FALSE,
            DEFAULT_CHARSET, OUT_DEFAULT_PRECIS, CLIP_DEFAULT_PRECIS, DEFAULT_QUALITY,
            FIXED_PITCH | FF_DONTCARE, L"Consolas");

        CreateWindowW(L"STATIC", L"Студент:", WS_VISIBLE | WS_CHILD, 10, 10, 80, 24, hwnd, NULL, ghInstance, NULL);
        hEditStudent = CreateWindowW(L"EDIT", STUDENT_FIO, WS_VISIBLE | WS_CHILD | WS_BORDER | ES_READONLY, 90, 10, 300, 24, hwnd, (HMENU)IDC_EDIT_STUDENT, ghInstance, NULL);
        SendMessageW(hEditStudent, WM_SETFONT, (WPARAM)hFont, TRUE);

        CreateWindowW(L"STATIC", L"Вариант:", WS_VISIBLE | WS_CHILD, 10, 42, 80, 24, hwnd, NULL, ghInstance, NULL);
        hEditVariant = CreateWindowW(L"EDIT", ansi_to_wide(STUDENT_VARIANT), WS_VISIBLE | WS_CHILD | WS_BORDER | ES_READONLY, 90, 42, 160, 24, hwnd, (HMENU)IDC_EDIT_VARIANT, ghInstance, NULL);
        SendMessageW(hEditVariant, WM_SETFONT, (WPARAM)hFont, TRUE);

        CreateWindowW(L"STATIC", L"Ключ (hex или пароль):", WS_VISIBLE | WS_CHILD, 10, 74, 200, 24, hwnd, NULL, ghInstance, NULL);
        hEditKey = CreateWindowW(L"EDIT", L"", WS_VISIBLE | WS_CHILD | WS_BORDER, 220, 74, 520, 24, hwnd, (HMENU)IDC_EDIT_KEY, ghInstance, NULL);
        SendMessageW(hEditKey, WM_SETFONT, (WPARAM)hFont, TRUE);

        hBtnKeyFile = CreateWindowW(L"BUTTON", L"Загрузить ключ...", WS_VISIBLE | WS_CHILD, 770, 74, 150, 26, hwnd, (HMENU)IDC_BTN_KEY_FILE, ghInstance, NULL);
        SendMessageW(hBtnKeyFile, WM_SETFONT, (WPARAM)hBtnFont, TRUE);

        CreateWindowW(L"STATIC", L"IV (hex, 16 символов):", WS_VISIBLE | WS_CHILD, 10, 106, 160, 24, hwnd, NULL, ghInstance, NULL);
        hEditIV = CreateWindowW(L"EDIT", L"0001020304050607", WS_VISIBLE | WS_CHILD | WS_BORDER, 180, 106, 180, 24, hwnd, (HMENU)IDC_EDIT_IV, ghInstance, NULL);
        SendMessageW(hEditIV, WM_SETFONT, (WPARAM)hFont, TRUE);
        SendMessageW(hEditIV, EM_SETLIMITTEXT, 16, 0);

        hBtnIn = CreateWindowW(L"BUTTON", L"Входной файл...", WS_VISIBLE | WS_CHILD, 10, 142, 160, 28, hwnd, (HMENU)IDC_BTN_IN, ghInstance, NULL);
        SendMessageW(hBtnIn, WM_SETFONT, (WPARAM)hBtnFont, TRUE);
        hBtnOut = CreateWindowW(L"BUTTON", L"Выходной файл...", WS_VISIBLE | WS_CHILD, 180, 142, 160, 28, hwnd, (HMENU)IDC_BTN_OUT, ghInstance, NULL);
        SendMessageW(hBtnOut, WM_SETFONT, (WPARAM)hBtnFont, TRUE);

        hBtnEnc = CreateWindowW(L"BUTTON", L"Зашифровать", WS_VISIBLE | WS_CHILD, 360, 142, 140, 32, hwnd, (HMENU)IDC_BTN_ENC, ghInstance, NULL);
        SendMessageW(hBtnEnc, WM_SETFONT, (WPARAM)hBtnFont, TRUE);
        hBtnDec = CreateWindowW(L"BUTTON", L"Расшифровать", WS_VISIBLE | WS_CHILD, 510, 142, 140, 32, hwnd, (HMENU)IDC_BTN_DEC, ghInstance, NULL);
        SendMessageW(hBtnDec, WM_SETFONT, (WPARAM)hBtnFont, TRUE);

        CreateWindowW(L"STATIC", L"Лог операций:", WS_VISIBLE | WS_CHILD, 10, 182, 120, 24, hwnd, NULL, ghInstance, NULL);
        hEditLog = CreateWindowW(L"EDIT", L"", WS_VISIBLE | WS_CHILD | WS_BORDER | ES_LEFT | ES_MULTILINE | ES_AUTOVSCROLL | ES_READONLY | WS_VSCROLL,
            10, 208, 910, 260, hwnd, (HMENU)IDC_EDIT_LOG, ghInstance, NULL);
        SendMessageW(hEditLog, WM_SETFONT, (WPARAM)hLogFont, TRUE);

        append_log(L"Приложение готово к работе.");
        break;
    }
    case WM_COMMAND: {
        int id = LOWORD(wParam);

        if (id == IDC_BTN_KEY_FILE) {

            WCHAR path[MAX_PATH] = L"";
            choose_open_file(hwnd, path);
            if (path[0]) {

                size_t keylen;
                uint8_t* kbuf = read_file_w(path, &keylen);

                if (!kbuf) {
                    append_log(L"Ошибка: не удалось прочитать файл ключа: %s", path);
                }
                else {
                    uint8_t final_key[32];
                    kdf_sha256(kbuf, keylen, final_key);
                    append_log(L"Ключ из файла нормируется через SHA-256 для соответствия hex-ключ (64 символа): %s", path);

                    WCHAR whex[65];

                    for (int i = 0; i < 32; i++) {
                        wsprintfW(&whex[i * 2], L"%02X", final_key[i]);
                    }

                    whex[64] = L'\0';
                    SetWindowTextW(hEditKey, whex);
                    free(kbuf);
                }
            }
        }
        else if (id == IDC_BTN_IN) {
            choose_open_file(hwnd, inPath);
            if (inPath[0]) append_log(L"Входной файл: %s", inPath);
        }
        else if (id == IDC_BTN_OUT) {
            choose_save_file(hwnd, outPath);
            if (outPath[0]) append_log(L"Выходной файл: %s", outPath);
        }
        else if (id == IDC_BTN_ENC || id == IDC_BTN_DEC) {
            if (!inPath[0] || !outPath[0]) {
                append_log(L"Ошибка: выберите входной и выходной файлы.");
                break;
            }

            WCHAR keyw[512];
            GetWindowTextW(hEditKey, keyw, _countof(keyw));
            uint8_t key_raw[32];

            WCHAR clean_key[128] = { 0 };
            size_t hex_count = clean_hex_string(keyw, clean_key, _countof(clean_key));

            if (hex_count == 64) {

                if (hexw_to_bytes(clean_key, key_raw, 32)) {
                    append_log(L"Используется введённый hex-ключ (64 символа).");
                }
                else {
                    append_log(L"Ошибка: не удалось распарсить hex-ключ.");
                    break;
                }
            }
            else {
                append_log(L"Введён не hex-ключ (%zu символов) → хэшируем как пароль.", hex_count);
                int len_utf8 = WideCharToMultiByte(CP_UTF8, 0, keyw, -1, NULL, 0, NULL, NULL);

                if (len_utf8 <= 1) {
                    append_log(L"Ошибка: пустой ввод.");
                    break;
                }

                char* key_utf8 = (char*)malloc(len_utf8);
                WideCharToMultiByte(CP_UTF8, 0, keyw, -1, key_utf8, len_utf8, NULL, NULL);
                kdf_sha256((uint8_t*)key_utf8, len_utf8 - 1, key_raw);
                free(key_utf8);
            }

            WCHAR ivw[64];
            GetWindowTextW(hEditIV, ivw, _countof(ivw));
            WCHAR clean_iv[32] = { 0 };
            size_t iv_hex = clean_hex_string(ivw, clean_iv, _countof(clean_iv));

            if (iv_hex != 16) {
                append_log(L"Ошибка: IV должен быть ровно 16 hex-символов. Фактически: %zu", iv_hex);
                break;
            }

            uint8_t iv[8];

            if (!hexw_to_bytes(clean_iv, iv, 8)) {
                append_log(L"Ошибка: не удалось распарсить IV.");
                break;
            }

            magma_cfb_t ctx;
            magma_cfb_initialization(&ctx, key_raw, iv);
            cb_ctx_t cbctx = { .ctx = &ctx };

            int ok = process_file_stream_w(inPath, outPath, &cbctx,
                (id == IDC_BTN_ENC) ? proc_cb_encrypt : proc_cb_decrypt);

            if (ok) {
                append_log(L"Успешно: %s", outPath);
            }
            else {
                append_log(L"Ошибка при обработке файлов!");
            }
        }
        break;
    }
    case WM_DESTROY:
        PostQuitMessage(0);
        break;
    default:
        return DefWindowProcW(hwnd, msg, wParam, lParam);
    }
    return 0;
}

int run_gui(HINSTANCE hInstance, int nCmdShow) {
    ghInstance = hInstance;
    WNDCLASSW wc = { 0 };
    wc.lpfnWndProc = WndProc;
    wc.hInstance = hInstance;
    wc.lpszClassName = L"MagmaCFBWin";
    wc.hCursor = LoadCursor(NULL, IDC_ARROW);
    wc.hbrBackground = CreateSolidBrush(RGB(248, 248, 250));
    RegisterClassW(&wc);

    HWND hwnd = CreateWindowW(
        wc.lpszClassName,
        L"Магма (CFB) — Демонстрация шифрования",
        WS_OVERLAPPEDWINDOW & ~WS_THICKFRAME,
        CW_USEDEFAULT, CW_USEDEFAULT, 950, 520,
        NULL, NULL, hInstance, NULL
    );

    if (!hwnd) return -1;

    // Показываем информацию о студенте
    show_student_info(hwnd);

    ShowWindow(hwnd, nCmdShow);
    UpdateWindow(hwnd);

    MSG msg;
    while (GetMessageW(&msg, NULL, 0, 0)) {
        TranslateMessage(&msg);
        DispatchMessageW(&msg);
    }

    return (int)msg.wParam;
}
