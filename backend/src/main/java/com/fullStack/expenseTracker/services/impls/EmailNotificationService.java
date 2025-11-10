package com.fullStack.expenseTracker.services.impls;
import java.io.UnsupportedEncodingException;
import java.util.Objects;

import com.fullStack.expenseTracker.services.NotificationService;
import com.fullStack.expenseTracker.models.User;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

@Component
@ConditionalOnProperty(name = "email.provider", havingValue = "smtp")
@Slf4j
public class EmailNotificationService implements NotificationService {

    @Autowired
    private JavaMailSender javaMailSender;

    @Value("${spring.mail.from.email:${spring.mail.username}}")
    private String fromMail;

    @Value("${spring.mail.from.name:Company}")
    private String senderName;

    @Override
    public void sendUserRegistrationVerificationEmail(User user) throws MessagingException, UnsupportedEncodingException {
        String toAddress = Objects.requireNonNull(user.getEmail(), "User email cannot be null");
        String fromAddress = Objects.requireNonNull(fromMail, "From mail cannot be null");
        String fromName = Objects.requireNonNull(senderName, "Sender name cannot be null");
        String subject = Objects.requireNonNull("Please verify your registration");
        String content = "Dear " + user.getUsername() + ",<br><br>"
                + "<p>Thank you for joining us! We are glad to have you on board.</p><br>"
                + "<p>To complete the sign up process, enter the verification code in your device.</p><br>"
                + "<p>verification code: <strong>" + user.getVerificationCode() + "</strong></p><br>"
                + "<p><strong>Please note that the above verification code will be expired within 15 minutes.</strong></p>"
                + "<br>Thank you,<br>"
                + "Your company name.";

        log.info("Attempting to send verification email to: {}", toAddress);
        log.debug("From address: {}, From name: {}, Subject: {}", fromAddress, fromName, subject);

        try {
            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message);

            helper.setFrom(fromAddress, fromName);
            helper.setTo(toAddress);
            helper.setSubject(subject);
            helper.setText(content, true);

            javaMailSender.send(message);
            log.info("Verification email sent successfully to: {}", toAddress);
        } catch (MessagingException e) {
            log.error("Failed to send verification email to: {}. Error: {}", toAddress, e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error sending verification email to: {}. Error: {}", toAddress, e.getMessage(), e);
            throw new MessagingException("Failed to send email: " + e.getMessage(), e);
        }
    }


    @Override
    public void sendForgotPasswordVerificationEmail(User user) throws MessagingException, UnsupportedEncodingException {
        String toAddress = Objects.requireNonNull(user.getEmail(), "User email cannot be null");
        String fromAddress = Objects.requireNonNull(fromMail, "From mail cannot be null");
        String fromName = Objects.requireNonNull(senderName, "Sender name cannot be null");
        String subject = Objects.requireNonNull("Forgot password - Please verify your Account");
        String content = "Dear " + user.getUsername() + ",<br><br>"
                + "<p>To change your password, enter the verification code in your device.</p><br>"
                + "<p>verification code: <strong>" + user.getVerificationCode() + "</strong></p><br>"
                + "<p><strong>Please note that the above verification code will be expired within 15 minutes.</strong></p>"
                + "<br>Thank you,<br>"
                + "Your company name.";

        log.info("Attempting to send forgot password email to: {}", toAddress);
        log.debug("From address: {}, From name: {}, Subject: {}", fromAddress, fromName, subject);

        try {
            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message);

            helper.setFrom(fromAddress, fromName);
            helper.setTo(toAddress);
            helper.setSubject(subject);
            helper.setText(content, true);

            javaMailSender.send(message);
            log.info("Forgot password email sent successfully to: {}", toAddress);
        } catch (MessagingException e) {
            log.error("Failed to send forgot password email to: {}. Error: {}", toAddress, e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error sending forgot password email to: {}. Error: {}", toAddress, e.getMessage(), e);
            throw new MessagingException("Failed to send email: " + e.getMessage(), e);
        }
    }
}
