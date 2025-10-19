package com.cryptoauth.infosecurity.user.dto;

import java.util.UUID;

import com.cryptoauth.infosecurity.user.entity.Role;

public record UserResponseProfile(UUID id, String email, Role role) {}