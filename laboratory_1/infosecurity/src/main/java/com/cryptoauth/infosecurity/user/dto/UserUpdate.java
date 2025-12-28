package com.cryptoauth.infosecurity.user.dto;

import java.time.LocalDateTime;

import com.cryptoauth.infosecurity.user.entity.Role;

public record UserUpdate(
    String email,
        Role role,
        String password_hash,
        String salt,
        Boolean accountLocked,
        Boolean passwordRestrictions,
        Integer passwordExpirationMonths,
        LocalDateTime passwordLastChanged,
        Integer minPasswordLength,
        Integer failedAttempts,
        Boolean firstLogin,
        Boolean emailConfirmed
) {}
