package com.cryptoauth.infosecurity.user.dto;

public record UserUpdatePolicy(boolean accountLocked, boolean passwordRestricted, 
    Integer minPasswordLength, Integer passwordExpiryMonths) {}
