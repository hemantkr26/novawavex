
package com.novawavex.novawavex.service;

import jakarta.mail.MessagingException;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Properties;

@Service
public class EmailService {

    @Value("${spring.mail.host}")
    private String mailHost;

    @Value("${spring.mail.port}")
    private int mailPort;

    @Value("${spring.mail.username}")
    private String mailUsername;

    @Value("${spring.mail.password}")
    private String mailPassword;

    /*
     * =========================================
     * SEND PASSWORD RESET EMAIL
     * =========================================
     */

    public void sendPasswordResetEmail(
            String recipientEmail,
            String resetLink) {

        System.out.println(
                ">>> EmailService: Sending password reset email to "
                        + recipientEmail
        );

        try {

            Properties properties =
                    new Properties();

            properties.put(
                    "mail.smtp.host",
                    mailHost
            );

            properties.put(
                    "mail.smtp.port",
                    String.valueOf(mailPort)
            );

            properties.put(
                    "mail.smtp.auth",
                    "true"
            );

            properties.put(
                    "mail.smtp.starttls.enable",
                    "true"
            );

            Session session =
                    Session.getInstance(
                            properties,
                            new jakarta.mail.Authenticator() {

                                @Override
                                protected jakarta.mail.PasswordAuthentication
                                getPasswordAuthentication() {

                                    return new jakarta.mail.PasswordAuthentication(
                                            mailUsername,
                                            mailPassword
                                    );
                                }
                            }
                    );

            MimeMessage message =
                    new MimeMessage(session);

            message.setFrom(
                    new InternetAddress(
                            mailUsername
                    )
            );

            message.setRecipients(
                    jakarta.mail.Message.RecipientType.TO,
                    InternetAddress.parse(
                            recipientEmail
                    )
            );

            message.setSubject(
                    "NovaWavex Password Reset"
            );

            message.setText(
                    "Hello,\n\n"
                    + "We received a request to reset "
                    + "your NovaWavex account password.\n\n"
                    + "Use the link below to create a "
                    + "new password:\n\n"
                    + resetLink
                    + "\n\n"
                    + "This password reset link will "
                    + "expire in 15 minutes and can "
                    + "only be used once.\n\n"
                    + "If you did not request a password "
                    + "reset, you can safely ignore "
                    + "this email.\n\n"
                    + "Regards,\n"
                    + "NovaWavex Security Team"
            );

            Transport.send(message);

        } catch (MessagingException exception) {

            throw new IllegalStateException(
                    "Unable to send password reset email",
                    exception
            );
        }
    }
}
