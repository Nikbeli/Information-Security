package com.cryptoauth.infosecurity.user.config;

import org.flywaydb.core.Flyway;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import jakarta.annotation.PostConstruct;

@Configuration
public class FlywayConfig {

    @Value("${spring.datasource.url}")
    private String dbUrl;
    @Value("${spring.datasource.username}")
    private String dbUser;
    @Value("${spring.datasource.password}")
    private String dbPassword;

    @PostConstruct
    public void migrate() {
        System.out.println("Running Flyway migrations...");
        Flyway.configure()
                .dataSource(dbUrl, dbUser, dbPassword)
                .baselineOnMigrate(true)
                .load()
                .migrate();
    }
}
