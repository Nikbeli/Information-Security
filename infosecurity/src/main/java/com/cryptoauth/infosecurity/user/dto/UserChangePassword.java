package com.cryptoauth.infosecurity.user.dto;

public record UserChangePassword(String oldPassword, String newPassword, String confirmPassword) {}
