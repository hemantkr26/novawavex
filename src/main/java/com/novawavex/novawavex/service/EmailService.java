
package com.novawavex.novawavex.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

@Service
public class EmailService {

    @Value("${RESEND_API_KEY}")
    private String resendApiKey;

    /*
     * =========================================
     * RESEND EMAIL API
     * =========================================
     */

    private static final String RESEND_API_URL =
            "https://api.resend.com/emails";

    private static final String FROM_EMAIL =
            "NovaWavex <onboarding@resend.dev>";

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

            String htmlContent =
                    "<html>"
                    + "<body>"
                    + "<h2>NovaWavex Password Reset</h2>"
                    + "<p>Hello,</p>"
                    + "<p>We received a request to reset "
                    + "your NovaWavex account password.</p>"
                    + "<p>Click the button below to create "
                    + "a new password:</p>"
                    + "<p>"
                    + "<a href=\"" + resetLink + "\" "
                    + "style=\"display:inline-block;"
                    + "padding:12px 20px;"
                    + "background:#000;"
                    + "color:#fff;"
                    + "text-decoration:none;"
                    + "border-radius:6px;\">"
                    + "Reset Password"
                    + "</a>"
                    + "</p>"
                    + "<p>Or use this link:</p>"
                    + "<p>" + resetLink + "</p>"
                    + "<p>This password reset link will "
                    + "expire in 15 minutes and can "
                    + "only be used once.</p>"
                    + "<p>If you did not request a password "
                    + "reset, you can safely ignore this email.</p>"
                    + "<p>Regards,<br>"
                    + "NovaWavex Security Team</p>"
                    + "</body>"
                    + "</html>";

            String jsonBody =
                    "{"
                    + "\"from\":\"" + escapeJson(FROM_EMAIL) + "\","
                    + "\"to\":[\"" + escapeJson(recipientEmail) + "\"],"
                    + "\"subject\":\"NovaWavex Password Reset\","
                    + "\"html\":\"" + escapeJson(htmlContent) + "\""
                    + "}";

            HttpClient client =
                    HttpClient.newHttpClient();

            HttpRequest request =
                    HttpRequest.newBuilder()
                            .uri(URI.create(RESEND_API_URL))
                            .header(
                                    "Authorization",
                                    "Bearer " + resendApiKey
                            )
                            .header(
                                    "Content-Type",
                                    "application/json"
                            )
                            .POST(
                                    HttpRequest.BodyPublishers
                                            .ofString(jsonBody)
                            )
                            .build();

            System.out.println(
                    ">>> EmailService: Connecting to Resend API"
            );

            HttpResponse<String> response =
                    client.send(
                            request,
                            HttpResponse.BodyHandlers.ofString()
                    );

            System.out.println(
                    ">>> EmailService: Resend response status: "
                            + response.statusCode()
            );

            if (response.statusCode() < 200
                    || response.statusCode() >= 300) {

                System.err.println(
                        ">>> EmailService ERROR: Resend API returned:"
                );

                System.err.println(
                        response.body()
                );

                throw new IllegalStateException(
                        "Unable to send password reset email"
                );
            }

            System.out.println(
                    ">>> EmailService: Password reset email "
                            + "sent successfully"
            );

            System.out.println(
                    ">>> EmailService: Resend response: "
                            + response.body()
            );

        } catch (Exception exception) {

            System.err.println(
                    ">>> EmailService ERROR: "
                            + exception.getClass().getName()
            );

            System.err.println(
                    ">>> EmailService ERROR MESSAGE: "
                            + exception.getMessage()
            );

            exception.printStackTrace();

            throw new IllegalStateException(
                    "Unable to send password reset email",
                    exception
            );
        }
    }

    /*
     * =========================================
     * ESCAPE JSON
     * =========================================
     */

    private String escapeJson(String value) {

        return value
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\r", "\\r")
                .replace("\n", "\\n");
    }
}
