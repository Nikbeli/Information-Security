#include <iostream>

unsigned int crc32(const unsigned char *buffer, unsigned int len) {
    unsigned int crc = 0xFFFFFFFF;

    while(len--) {
        crc ^= *buffer++;
        for(int bit = 0; bit < 8; bit++) {
            if(crc & 1) crc = (crc >> 1) ^ 0xEDB88320;
            else crc = (crc >> 1);
        }
    }
    return crc ^ 0xFFFFFFFF;
}

int main() {
    const unsigned char *buffer = new unsigned char[6]{0x41, 0x6C, 0x65, 0x6B, 0x4F, 0x53};
    std::cout << crc32(buffer, 6);
    return 0;
}