package com.cryptoauth.infosecurity.auth.dto;

public record LoginResult(boolean requiresOtp, String email, String token) {}