package com.cryptoauth.infosecurity.auth.dto;

public record ResetPasswordRequest(String email, String oldPassword, String newPassword) {}
