package com.cryptoauth.infosecurity.user.entity;

import java.time.LocalDateTime;
import java.util.UUID;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(updatable = false, nullable = false)
    private UUID id;

    @Column(unique = true, nullable = false)
    private String email;

    // По ТЗ: пароль в базе хранится как хэш (алгоритм — SHA)
    @Column(name = "password_hash", length = 512)
    private String password_hash;

    private String salt;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private Role role;

    // блокировка учётки
    @Column(nullable = false)
    private boolean accountLocked;

    // включены ли ограничения
    @Column(nullable = false)
    private boolean passwordRestrictions;

    // срок действия пароля в месяцах (0 = бессрочно)
    @Column(nullable = false)
    private int passwordExpirationMonths;

    // дата смены пароля
    private LocalDateTime passwordLastChanged;

    // минимальная длина пароля (0 = разрешён пустой пароль)
    private int minPasswordLength;

    // счётчик неуспешных попыток входа
    @Column(nullable = false)
    private int failedAttempts;

    // первый вход
    @Column(nullable = false)
    private boolean firstLogin;

    // нужно ли подтверждение из почты
    @Column(nullable = false)
    private boolean emailConfirmed;
}
