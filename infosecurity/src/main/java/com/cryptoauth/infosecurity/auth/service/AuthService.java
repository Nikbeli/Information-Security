package com.cryptoauth.infosecurity.auth.service;

import com.cryptoauth.infosecurity.auth.dto.*;
import com.cryptoauth.infosecurity.auth.jwt.JwtService;
import com.cryptoauth.infosecurity.user.entity.User;
import com.cryptoauth.infosecurity.user.repository.UserRepository;
import com.cryptoauth.infosecurity.user.utils.PasswordHashUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final JwtService jwtService;
    private final OtpService otpService;
    private final UserRepository userRepository;

    public LoginResult login(UserLogin userLogin) {

        // Проверка email и существования пользователя
        if (userLogin.email() == null || userLogin.email().isBlank()) {
            throw new RuntimeException("Email cannot be empty");
        }

        User user = userRepository.findByEmail(userLogin.email())
                .orElseThrow(() -> new RuntimeException("Пользователь не найден!"));

        if (user.isAccountLocked()) {
            throw new RuntimeException("Аккаунт заблокировн");
        }

        log.info("Попытка входа пользователя: {}", user.getEmail());

        // Проверка срока действия пароля (опционально)
        if (user.getPasswordLastChanged() != null && user.getPasswordExpirationMonths() > 0) {
            long monthsSinceChange = java.time.temporal.ChronoUnit.MONTHS.between(
                    user.getPasswordLastChanged(), LocalDateTime.now());
            if (monthsSinceChange >= user.getPasswordExpirationMonths()) {
                log.warn("Срок действия пароля истёк для: {}", user.getEmail());
                // Можно отправлять уведомление или требовать сброс пароля
            }
        }

        // Первый вход — пароль не проверяется, сразу токен
        if (user.isFirstLogin()) {
            user.setFirstLogin(false);
            userRepository.save(user);
            String token = jwtService.generateToken(user.getEmail(), user.getRole().name());
            return new LoginResult(false, user.getEmail(), token);
        }

        // Проверка пароля
        boolean passwordOk = PasswordHashUtil.matches(
                userLogin.password(),
                user.getSalt(),
                user.getPassword_hash());

        if (!passwordOk) {
            int failedAttempts = user.getFailedAttempts() + 1;
            user.setFailedAttempts(failedAttempts);
            if (failedAttempts >= 3) {
                user.setAccountLocked(true);
                log.warn("Аккаунт заблокирован: {}", user.getEmail());
                userRepository.save(user);
                throw new RuntimeException("Учётная запись заблокирована из-за 3 неудачных попыток входа.");
            }
            userRepository.save(user);
            throw new RuntimeException("Неверный пароль или логин");
        }

        // Сбрасываем счётчик неудачных попыток при успешном входе
        if (user.getFailedAttempts() > 0) {
            user.setFailedAttempts(0);
        }

        userRepository.save(user);

        // Определяем логику OTP
        if (user.isEmailConfirmed()) {
            // Генерируем OTP и сообщаем контроллеру, что нужен OTP
            otpService.generateAndSendOtp(user.getEmail());
            log.info("OTP отправлен пользователю {}", user.getEmail());
            return new LoginResult(true, user.getEmail(), null);
        } else {
            // Email не подтверждён — сразу выдаём токен
            String token = jwtService.generateToken(user.getEmail(), user.getRole().name());
            return new LoginResult(false, user.getEmail(), token);
        }
    }

    public String verifyAndGenerateToken(OtpVerifyRequest request) {
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!user.isEmailConfirmed()) {
            log.info("Email не подтверждён — OTP не требуется");
            return jwtService.generateToken(user.getEmail(), user.getRole().name());
        }

        boolean verified = otpService.verifyOtp(request.email(), request.otp());
        if (!verified)
            throw new RuntimeException("Invalid or expired OTP");

        log.info("OTP успешно подтверждён — выдаём токен для {}", user.getEmail());
        return jwtService.generateToken(user.getEmail(), user.getRole().name());
    }

    public void resetPassword(ResetPasswordRequest request) {
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Проверяем старый пароль
        String oldHash = PasswordHashUtil.sha1Hex(request.oldPassword(), user.getSalt());
        if (!user.getPassword_hash().equals(oldHash)) {
            throw new RuntimeException("Старый пароль введён неверно.");
        }

        // Проверяем, что новый пароль отличается от старого
        if (request.newPassword().equals(request.oldPassword())) {
            throw new RuntimeException("Новый пароль не должен совпадать со старым.");
        }
        
        if (request.newPassword().length() < user.getMinPasswordLength()) {
            throw new IllegalArgumentException("Пароль слишком короткий. Минимальная длина: " + user.getMinPasswordLength() + " символов.");
        }


        // Проверка уникальности символов
        if (user.isPasswordRestrictions()) {
            Set<Character> chars = new HashSet<>();
            for (char c : request.newPassword().toCharArray()) {
                if (!chars.add(c)) {
                    throw new IllegalArgumentException(
                            "Пароль содержит повторяющиеся символы. Задайте более сложный пароль.");
                }
            }
        }

        String newSalt = PasswordHashUtil.generateSalt();
        String newHash = PasswordHashUtil.sha1Hex(request.newPassword(), newSalt);

        user.setSalt(newSalt);
        user.setPassword_hash(newHash);
        user.setPasswordLastChanged(LocalDateTime.now());
        user.setPasswordLastChanged(LocalDateTime.now());
        user.setFailedAttempts(0);
        user.setAccountLocked(false);
        userRepository.save(user);

        log.info("Пароль успешно обновлён для пользователя {}", user.getEmail());
    }

    public void validToken(String token) {
        try {
            var claims = jwtService.extractAllClaim(token);
            if (claims.getExpiration().before(new Date())) {
                throw new RuntimeException("Token expired");
            }
            log.info("Токен действителен для {}", claims.getSubject());
        } catch (Exception e) {
            throw new RuntimeException("Invalid token", e);
        }
    }

    // ---- Дополнительные методы для контроллера ----

    public void repeatOtpCode(ResendOtpRequest request) {
        otpService.generateAndSendOtp(request.email());
        log.info("Повтор OTP для {}", request.email());
    }

    public void forgotPassword(ForgotPasswordRequest request) {
        otpService.generateAndSendOtp(request.email());
        log.info("Отправлен OTP для восстановления пароля {}", request.email());
    }

    public void verifyOtpForgotPassword(OtpVerifyRequest request) {
        boolean verified = otpService.verifyOtp(request.email(), request.otp());
        if (!verified)
            throw new RuntimeException("Invalid OTP for password reset");
        log.info("OTP успешно подтверждён для восстановления {}", request.email());
    }
}
