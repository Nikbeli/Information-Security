package com.cryptoauth.infosecurity.auth.utils;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;

@Configuration
public class OpenApiConfig {

    private static final String SCHEME_NAME = "JWT";
    private static final String SCHEME = "bearer";

    private SecurityScheme createBearerScheme() {
        return new SecurityScheme()
                .type(SecurityScheme.Type.HTTP)
                .scheme(SCHEME);

    }

    @Bean
    OpenAPI customOpenApi() {
        return new OpenAPI()
                .components(new Components()
                        .addSecuritySchemes(SCHEME_NAME, createBearerScheme()))
                .addSecurityItem(new SecurityRequirement().addList(SCHEME_NAME));
    }

}
