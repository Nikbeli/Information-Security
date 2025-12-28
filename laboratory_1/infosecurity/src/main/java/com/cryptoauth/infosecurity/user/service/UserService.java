package com.cryptoauth.infosecurity.user.service;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.cryptoauth.infosecurity.user.dto.UserCreate;
import com.cryptoauth.infosecurity.user.dto.UserGet;
import com.cryptoauth.infosecurity.user.dto.UserResponseProfile;
import com.cryptoauth.infosecurity.user.dto.UserUpdate;
import com.cryptoauth.infosecurity.user.entity.Role;
import com.cryptoauth.infosecurity.user.entity.User;
import com.cryptoauth.infosecurity.user.exception.UserNotFoundException;
import com.cryptoauth.infosecurity.user.mapper.UserMapper;
import com.cryptoauth.infosecurity.user.repository.UserRepository;
import com.cryptoauth.infosecurity.user.utils.PasswordHashUtil;

import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    private final UserMapper userMapper;

    // --- создание нового пользователя ---
    @Transactional()
    public UserGet createUser(UserCreate userCreate) {
        String email = userCreate.email().toLowerCase().trim();

        if (email.isBlank())
            throw new IllegalArgumentException("Email is required");

        if ("admin".equalsIgnoreCase(email) || "admin@example.com".equalsIgnoreCase(email)) {
            throw new IllegalArgumentException("Cannot create reserved ADMIN user");
        }

        if (userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("User already exists");
        }

        User user = userMapper.toEntity(userCreate);
        user.setRole(Role.USER);
        user.setFailedAttempts(0);
        user.setAccountLocked(false);
        user.setPasswordExpirationMonths(0);
        user.setMinPasswordLength(0);
        user.setPasswordRestrictions(false);

        return userMapper.toDto(userRepository.save(user));
    }

    @Transactional(readOnly = true)
    public Page<UserGet> getAllUsers(String search, Pageable pageable) {
        Page<User> page = (search == null || search.isBlank())
                ? userRepository.findAll(pageable)
                : userRepository.findByEmailContainingIgnoreCase(search, pageable);

        return page.map(userMapper::toDto);
    }

    @Transactional(readOnly = true)
    public UserGet getUser(UUID id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        return userMapper.toDto(user);
    }

    @Transactional(readOnly = true)
    public UserGet getUserByEmail(String email) {
        return userMapper.toDto(userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException(email)));
    }

    @Transactional
    public UserGet updateUser(UUID id, UserUpdate userUpdate) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found: " + id));

        // Обновление email (если задан)
        if (userUpdate.email() != null && !userUpdate.email().isBlank()) {
            String email = userUpdate.email().toLowerCase().trim();
            if (!email.equals(user.getEmail()) && userRepository.existsByEmail(email)) {
                throw new IllegalArgumentException("Email already in use: " + email);
            }
            user.setEmail(email);
        }

        // Обновление роли (если задана)
        if (userUpdate.role() != null) {
            user.setRole(userUpdate.role());
        }

        if (userUpdate.password_hash().length() < user.getMinPasswordLength()) {
            throw new IllegalArgumentException("Пароль слишком короткий. Минимальная длина: " + user.getMinPasswordLength() + " символов.");
        }

        // Обновление пароля (если задан)
        if (userUpdate.password_hash() != null && !userUpdate.password_hash().isBlank()) {
            String password = userUpdate.password_hash();

            // Проверка ограничений на пароль. Если включены ограничения на пароль
            if (user.isPasswordRestrictions()) {

                // Если включены ограничения на пароль
                if (user.isPasswordRestrictions()) {
                    // Проверяем отсутствие повторяющихся символов
                    for (int i = 0; i < password.length(); i++) {
                        for (int j = i + 1; j < password.length(); j++) {
                            if (password.charAt(i) == password.charAt(j)) {
                                throw new IllegalArgumentException(
                                        "Пароль содержит повторяющиеся символы. Задайте более сложный пароль.");
                            }
                        }
                    }
                }
            }

            // Хэшируем новый пароль
            String salt = PasswordHashUtil.generateSalt();
            String hashedPassword = PasswordHashUtil.sha1Hex(password, salt);

            user.setSalt(salt);
            user.setPassword_hash(hashedPassword);

            user.setPasswordLastChanged(LocalDateTime.now());
        }

        // Прочие обновления
        if (userUpdate.accountLocked() != null) {
            user.setAccountLocked(userUpdate.accountLocked());
        }

        if (userUpdate.failedAttempts() != null) {
            user.setFailedAttempts(userUpdate.failedAttempts());
        }

        if (userUpdate.passwordExpirationMonths() != null) {
            user.setPasswordExpirationMonths(userUpdate.passwordExpirationMonths());
        }

        if (userUpdate.minPasswordLength() != null) {
            user.setMinPasswordLength(userUpdate.minPasswordLength());
        }

        if (userUpdate.passwordRestrictions() != null) {
            user.setPasswordRestrictions(userUpdate.passwordRestrictions());
        }

        if (userUpdate.firstLogin() != null) {
            user.setFirstLogin(userUpdate.firstLogin());
        }

        if (userUpdate.emailConfirmed() != null) {
            user.setEmailConfirmed(userUpdate.emailConfirmed());
        }

        return userMapper.toDto(userRepository.save(user));
    }

    @Transactional
    public void deleteUser(String email) {
        if (!userRepository.existsByEmail(email)) {
            throw new UserNotFoundException(email);
        }
        userRepository.deleteByEmail(email);
    }

    @Transactional(readOnly = true)
    public UserResponseProfile getMe(String email) {
        UserGet userGet = userMapper.toDto(userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found with email: " + email)));

        UserResponseProfile userResponseProfile = new UserResponseProfile(userGet.id(), userGet.email(),
                userGet.role());
        return userResponseProfile;
    }
}
