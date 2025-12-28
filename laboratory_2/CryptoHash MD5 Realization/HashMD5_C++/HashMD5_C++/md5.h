#pragma once

#ifndef MD5_H
#define MD5_H

#include <string>
#include <array>
#include <cstdint>
#include <vector>

class MD5 {
public:
	MD5();
	void update(const std::string& data);
	std::string finalize();
	static std::string fromFile(const std::string& path);

private:
	void transform(const uint8_t block[64]);
	void encode(uint8_t* output, const uint32_t* input, size_t len);
	void decode(uint32_t* output, const uint8_t* input, size_t len);
	void reset();
	static std::string bytesToHex(const uint8_t* bytes, size_t length);

	bool finalized;
	uint64_t count;
	std::array<uint32_t, 4> state;
	std::array<uint8_t, 64> buffer;
	std::array<uint8_t, 16> digest;
};

#endif
