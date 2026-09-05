package com.novawavex.novawavex.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Service
public class EmailService {

    @Value("${GOOGLE_CLIENT_ID}")
    private String clientId;

    @Value("${GOOGLE_CLIENT_SECRET}")
    private String clientSecret;

    @Value("${GOOGLE_REFRESH_TOKEN}")
    private String refreshToken;

    @Value("${GOOGLE_SENDER_EMAIL}")
    private String senderEmail;

    private static final String GOOGLE_TOKEN_URL =
            "https://oauth2.googleapis.com/token";

    private static final String GMAIL_SEND_URL =
            "https://gmail.googleapis.com/gmail/v1/users/me/messages/send";

    private static final String GMAIL_SCOPE =
            "https://www.googleapis.com/auth/gmail.send";

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

            String accessToken = getAccessToken();

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
                    + "<a href=\"" + escapeHtml(resetLink) + "\" "
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
                    + "<p>" + escapeHtml(resetLink) + "</p>"
                    + "<p>This password reset link will "
                    + "expire in 15 minutes and can "
                    + "only be used once.</p>"
                    + "<p>If you did not request a password "
                    + "reset, you can safely ignore this email.</p>"
                    + "<p>Regards,<br>"
                    + "NovaWavex Security Team</p>"
                    + "</body>"
                    + "</html>";

            String rawEmail =
                    "From: NovaWavex <" + senderEmail + ">\r\n"
                    + "To: " + recipientEmail + "\r\n"
                    + "Subject: NovaWavex Password Reset\r\n"
                    + "MIME-Version: 1.0\r\n"
                    + "Content-Type: text/html; charset=UTF-8\r\n"
                    + "\r\n"
                    + htmlContent;

            String encodedMessage =
                    Base64.getUrlEncoder()
                            .withoutPadding()
                            .encodeToString(
                                    rawEmail.getBytes(
                                            StandardCharsets.UTF_8
                                    )
                            );

            String jsonBody =
                    "{"
                    + "\"raw\":\""
                    + encodedMessage
                    + "\""
                    + "}";

            HttpClient client =
                    HttpClient.newHttpClient();

            HttpRequest request =
                    HttpRequest.newBuilder()
                            .uri(URI.create(GMAIL_SEND_URL))
                            .header(
                                    "Authorization",
                                    "Bearer " + accessToken
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
                    ">>> EmailService: Sending email through Gmail API"
            );

            HttpResponse<String> response =
                    client.send(
                            request,
                            HttpResponse.BodyHandlers.ofString()
                    );

            System.out.println(
                    ">>> EmailService: Gmail API response status: "
                            + response.statusCode()
            );

            if (response.statusCode() < 200
                    || response.statusCode() >= 300) {

                System.err.println(
                        ">>> EmailService ERROR: Gmail API returned:"
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
     * GET GOOGLE ACCESS TOKEN
     * =========================================
     */

    private String getAccessToken()
            throws Exception {

        String requestBody =
                "client_id="
                + urlEncode(clientId)
                + "&client_secret="
                + urlEncode(clientSecret)
                + "&refresh_token="
                + urlEncode(refreshToken)
                + "&grant_type=refresh_token";

        HttpClient client =
                HttpClient.newHttpClient();

        HttpRequest request =
                HttpRequest.newBuilder()
                        .uri(URI.create(GOOGLE_TOKEN_URL))
                        .header(
                                "Content-Type",
                                "application/x-www-form-urlencoded"
                        )
                        .POST(
                                HttpRequest.BodyPublishers
                                        .ofString(requestBody)
                        )
                        .build();

        System.out.println(
                ">>> EmailService: Refreshing Google access token"
        );

        HttpResponse<String> response =
                client.send(
                        request,
                        HttpResponse.BodyHandlers.ofString()
                );

        if (response.statusCode() < 200
                || response.statusCode() >= 300) {

            System.err.println(
                    ">>> EmailService ERROR: Google token API returned:"
            );

            System.err.println(
                    response.body()
            );

            throw new IllegalStateException(
                    "Unable to obtain Google access token"
            );
        }

        String accessToken =
                extractJsonValue(
                        response.body(),
                        "access_token"
                );

        if (accessToken == null
                || accessToken.isBlank()) {

            throw new IllegalStateException(
                    "Google access token was not returned"
            );
        }

        return accessToken;
    }

    /*
     * =========================================
     * SIMPLE JSON VALUE EXTRACTION
     * =========================================
     */

    private String extractJsonValue(
            String json,
            String key) {

        String search =
                "\"" + key + "\":\"";

        int start =
                json.indexOf(search);

        if (start == -1) {
            return null;
        }

        start += search.length();

        int end =
                json.indexOf(
                        "\"",
                        start
                );

        if (end == -1) {
            return null;
        }

        return json.substring(
                start,
                end
        );
    }

    /*
     * =========================================
     * URL ENCODE
     * =========================================
     */

    private String urlEncode(
            String value) {

        return java.net.URLEncoder
                .encode(
                        value,
                        StandardCharsets.UTF_8
                );
    }

    /*
     * =========================================
     * HTML ESCAPE
     * =========================================
     */

    private String escapeHtml(
            String value) {

        return value
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }
}