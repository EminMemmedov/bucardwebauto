package az.bucard.reporting;

import az.bucard.config.Config;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

public final class TelegramReporter {
    private final HttpClient httpClient;

    public TelegramReporter() {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
    }

    public void send(String message) {
        String token = Config.telegramBotToken();
        String chatId = Config.telegramChatId();
        if (token == null || token.isBlank() || chatId == null || chatId.isBlank()) {
            // Telegram secrets not provided - skip reporting silently as expected
            return;
        }

        String body = "chat_id=" + encode(chatId) + "&text=" + encode(message);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://api.telegram.org/bot" + token + "/sendMessage"))
                .timeout(Duration.ofSeconds(15))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();

        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() >= 300) {
                System.err.println("Warning: Telegram notification returned HTTP " + response.statusCode());
            }
        } catch (IOException e) {
            System.err.println("Warning: Could not send Telegram test summary: " + e.getMessage());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.err.println("Warning: Telegram report was interrupted");
        }
    }

    private static String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }
}
