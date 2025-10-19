package com.cryptoauth.infosecurity.auth.jwt;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import lombok.Getter;
import lombok.Setter;

@Configuration
@ConfigurationProperties(prefix = "jwt", ignoreInvalidFields = true)
@Getter
@Setter
public class JwtProperties {
    private String key = "";

    // Base64 encoded secret (256-bit min for HS256)
    private String secret;

    // Время истечения срока действия токена в миллисекундах
    private long expirationMs = 1000 * 60 * 60 * 10; // 10 часов
}
