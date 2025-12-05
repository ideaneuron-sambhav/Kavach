package com.login.Login.service.email;

import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    public void sendPasswordLinkEmail(String to, String resetLink) {
        SimpleMailMessage msg = new SimpleMailMessage();
        msg.setTo(to);
        msg.setSubject("Your Account Password Reset Request For Login Kavach - Do Not Reply");
        String emailContent = """
                Hello,

                We received a request to reset your password.

                Please click the link below to reset your password:
                %s

                If you did not request this, you can safely ignore this email.

                ----------------------------------------------------------
                This is a system-generated email. Please do NOT reply to it.
                ----------------------------------------------------------

                Regards,
                Support Team,
                Kavach
                """.formatted(resetLink);
        msg.setText(emailContent);
        mailSender.send(msg);
    }
    public void sendRegistrationEmail(String email, String activationLink) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);

            helper.setTo(email);
            helper.setSubject("Account Activation - Do Not Reply");

            String emailContent = """
                Hello,

                Your account has been created by the system administrator.

                Please click the link below to set your password and activate your account:
                %s

                ----------------------------------------------------------
                This is a system-generated email. Please do NOT reply to it.
                ----------------------------------------------------------

                Regards,
                Support Team,
                Kavach
                """.formatted(activationLink);

            helper.setText(emailContent);

            mailSender.send(message);

        } catch (Exception e) {
            throw new RuntimeException("Error in sending mail!");
        }
    }
}

