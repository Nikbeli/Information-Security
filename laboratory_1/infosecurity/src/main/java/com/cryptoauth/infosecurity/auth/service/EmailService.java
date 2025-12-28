package com.cryptoauth.infosecurity.auth.service;

import java.time.LocalDateTime;

import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

@Service
@RequiredArgsConstructor
public class EmailService {
     private final JavaMailSender mailSender;

    public void sendOtpEmail(String toEmail, String otpCode) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            // От кого письмо (имя + email)
            helper.setFrom("Информационная безопасность! <infosecurity@gmail.com>");
            // Почта поддержки
            helper.setReplyTo("infosecurity@gmail.com");
            // Кому
            helper.setTo(toEmail);

            // Тема письма — информативная и ненавязчивая
            helper.setSubject("Ваш уникальный код подтверждения входа");

            // Текст письма — с приветствием, объяснением, деталями и подписью
            StringBuilder emailText = new StringBuilder();

            emailText.append("Здравствуйте! 👋 \n\n");

            emailText.append(
                    "Мы получили запрос на отправку кода подтверждения для доступа к вашему аккаунту. 🔐\n");
            emailText.append("Пожалуйста, используйте следующий код для подтверждения вашей личности:\n\n");

            emailText.append(" ✅ >>>  ").append(otpCode).append("  <<< ✅ \n\n");

            emailText.append("⏳ Этот код действителен в течение 5 минут. Пожалуйста, не сообщайте его никому.\n\n");

            emailText.append(
                    "Если вы не запрашивали этот код, пожалуйста, проигнорируйте это письмо — это может быть угрозой вашей безопасности.\n\n");

            emailText.append(
                    "🔒 Для вашей безопасности не пересылайте этот код и не вводите его на подозрительных сайтах.\n\n");

            emailText.append(
                    "Если у вас возникли вопросы или проблемы, пожалуйста, свяжитесь с нашей службой поддержки:\n");
            emailText.append("📧 support@infosecurity@gmail.com\n\n");

            emailText.append("С уважением,\n");
            emailText.append("Команда infosecurity@gmail.com 👩‍⚕️\n\n");

            emailText.append("🕒 Дата и время отправки: ").append(LocalDateTime.now()).append("\n");

            helper.setText(emailText.toString(), false);

            mailSender.send(message);
        } catch (MessagingException e) {
            throw new RuntimeException("Не удалось отправить email: " + e.getMessage(), e);
        }
    }
}
