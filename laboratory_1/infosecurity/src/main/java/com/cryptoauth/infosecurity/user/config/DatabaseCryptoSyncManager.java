package com.cryptoauth.infosecurity.user.config;

import com.cryptoauth.infosecurity.user.entity.Role;
import com.cryptoauth.infosecurity.user.entity.User;
import com.cryptoauth.infosecurity.user.repository.UserRepository;
import com.cryptoauth.infosecurity.user.utils.DesFileCipher;
import com.cryptoauth.infosecurity.user.utils.PasswordHashUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.stereotype.Component;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.context.event.EventListener;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.util.*;

@Component
@RequiredArgsConstructor
@Slf4j
public class DatabaseCryptoSyncManager implements CommandLineRunner {

    private final UserRepository userRepository;

    private final JdbcTemplate jdbcTemplate;

    // Сохраним passphrase при старте, чтобы использовать при закрытии
    private volatile String passphrase;

    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule())
        .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    private static final String JSON_FILE = "userdata.json";
    private static final String ENC_FILE = "userdata.enc";

    private final File jsonFile = new File(JSON_FILE);
    private final File encFile = new File(ENC_FILE);

    @Override
    public void run(String... args) throws Exception {
        File jsonFile = new File(JSON_FILE);
        File encFile = new File(ENC_FILE);

        // === Запрос парольной фразы === //
        Scanner scanner = new Scanner(System.in);
        System.out.print("Введите парольную фразу для доступа к данным (Enter = Init!): ");
        this.passphrase = scanner.nextLine().trim();

        // Расшифровка или создание нового файла ===
        if (encFile.exists()) {
            try {
                DesFileCipher.decryptFile(encFile, jsonFile, passphrase);
                log.info("Файл userdata.enc расшифрован для работы");
            } catch (Exception e) {
                log.error("Ошибка расшифровки userdata.enc: неверный пароль или повреждён файл", e);
                return;
            }
        } else {
            log.warn("Файл userdata.enc не найден. Создаётся новый JSON с администратором по умолчанию");
            createDefaultAdmin(jsonFile);
            DesFileCipher.encryptFile(jsonFile, encFile, passphrase);
            Files.deleteIfExists(jsonFile.toPath());
            log.info("Создан новый зашифрованный файл userdata.enc");
        }

        // === Синхронизация JSON → PostgreSQL === //
        if (jsonFile.exists() && Files.size(jsonFile.toPath()) > 0) {
            try {
                User[] usersFromFile = objectMapper.readValue(jsonFile, User[].class);

                for (User u : usersFromFile) {
                    u.setId(null); // JPA сгенерирует новый UUID
                }

                // Полностью перезаписываем таблицу
                userRepository.deleteAll();
                userRepository.saveAll(Arrays.asList(usersFromFile));
                log.info("Все пользователи из JSON перезаписаны в базе данных");

            } catch (Exception e) {
                log.error("Ошибка загрузки пользователей из JSON", e);
            }
        } else {
            log.warn("JSON-файл отсутствует или пуст. Тогда создаётся администратор по умолчанию");
            if (userRepository.count() == 0) {
                createDefaultAdmin(jsonFile);
                DesFileCipher.encryptFile(jsonFile, encFile, passphrase);
                Files.deleteIfExists(jsonFile.toPath());
                log.info("Создан администратор по умолчанию и зашифрованный файл.");
            }
        }

        // === Экспорт PostgreSQL -> JSON -> ENC при завершении === //
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                List<User> allUsers = userRepository.findAll();
                objectMapper.writerWithDefaultPrettyPrinter().writeValue(jsonFile, allUsers);
                DesFileCipher.encryptFile(jsonFile, encFile, passphrase);
                Files.deleteIfExists(jsonFile.toPath());
                log.info("Данные PostgreSQL экспортированы и зашифрованы в userdata.enc");
            } catch (Exception e) {
                log.error("Ошибка при экспорте PostgreSQL -> ENC", e);
            }
        }));
    }

    @EventListener(ContextClosedEvent.class)
    @Transactional
    public void onContextClosed(ContextClosedEvent ev) {
        // безопасность, если passphrase не установлен — ничего не делаем
        if (this.passphrase == null) {
            log.warn("Passphrase отсутствует — пропускаем экспорт/очистку.");
            return;
        }

        try {
            // Экспортируем snapshot из БД в JSON
            List<User> allUsers = userRepository.findAll();
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(jsonFile, allUsers);
            log.info("Экспортировано {} пользователей в {}", allUsers.size(), jsonFile.getAbsolutePath());

            // Шифруем JSON -> userdata.enc
            DesFileCipher.encryptFile(jsonFile, encFile, this.passphrase);
            Files.deleteIfExists(jsonFile.toPath());
            log.info("JSON зашифрован в {}", encFile.getAbsolutePath());

            // Очищаем таблицу через JPA
            userRepository.deleteAll();
            log.warn("Все записи пользователей удалены");

            clearDatabase();          
            log.warn("Таблица users сброшена");

        } catch (Exception ex) {
            log.error("Ошибка при экспортe/шифровании/очистке при закрытии контекста", ex);
        }
    }

    // === Очистка базы данных === //
    private void clearDatabase() {
        try {
            // Очистка таблицы пользователей
            userRepository.deleteAll();
            log.warn("Все пользователи в базе данных удалены (выполнена очистка PostgreSQL)");

            jdbcTemplate.execute("DROP TABLE IF EXISTS users CASCADE");
            // jdbcTemplate.execute("TRUNCATE TABLE users RESTART IDENTITY CASCADE");
            log.warn("Таблица 'users' удалена и очищена из базы данных.");
        } catch (Exception e) {
            log.error("Ошибка при очистке базы данных", e);
        }
    }

    // === Вспомогательный метод для создания админа === //
    private void createDefaultAdmin(File jsonFile) {
        try {
            String salt = PasswordHashUtil.generateSalt();
            String hash = PasswordHashUtil.sha1Hex("admin", salt);

            User admin = User.builder()
                    .email("admin@example.com")
                    .password_hash(hash)
                    .salt(salt)
                    .role(Role.ADMIN)
                    .accountLocked(false)
                    .failedAttempts(0)
                    .passwordRestrictions(false)
                    .passwordExpirationMonths(0)
                    .minPasswordLength(0)
                    .emailConfirmed(false)
                    .firstLogin(false)
                    .passwordLastChanged(LocalDateTime.now())
                    .build();

            objectMapper.writerWithDefaultPrettyPrinter().writeValue(jsonFile, List.of(admin));
            log.info("Создан JSON с администратором по умолчанию");
        } catch (Exception e) {
            log.error("Ошибка при создании администратора по умолчанию", e);
        }
    }
}
