package az.bucard.reporting;

import az.bucard.config.Config;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;

public final class TelegramReporter {
    private final HttpClient httpClient;

    public TelegramReporter() {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
    }

    public void send(String htmlMessage) {
        String token = Config.telegramBotToken();
        String chatId = Config.telegramChatId();
        if (token == null || token.isBlank() || chatId == null || chatId.isBlank()) {
            // Telegram secrets not provided - skip reporting silently
            return;
        }

        String body = "chat_id=" + encode(chatId) + "&parse_mode=HTML&text=" + encode(htmlMessage);
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

    public void sendPhoto(Path photoPath, String caption) {
        String token = Config.telegramBotToken();
        String chatId = Config.telegramChatId();
        if (token == null || token.isBlank() || chatId == null || chatId.isBlank()) {
            return;
        }
        if (photoPath == null || !Files.exists(photoPath)) {
            return;
        }

        try {
            String boundary = "===TelegramBoundary" + System.currentTimeMillis() + "===";
            byte[] fileBytes = Files.readAllBytes(photoPath);
            String filename = photoPath.getFileName().toString();

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            // field: chat_id
            baos.write(("--" + boundary + "\r\n").getBytes(StandardCharsets.UTF_8));
            baos.write(("Content-Disposition: form-data; name=\"chat_id\"\r\n\r\n").getBytes(StandardCharsets.UTF_8));
            baos.write((chatId + "\r\n").getBytes(StandardCharsets.UTF_8));

            // field: parse_mode
            baos.write(("--" + boundary + "\r\n").getBytes(StandardCharsets.UTF_8));
            baos.write(("Content-Disposition: form-data; name=\"parse_mode\"\r\n\r\n").getBytes(StandardCharsets.UTF_8));
            baos.write(("HTML\r\n").getBytes(StandardCharsets.UTF_8));

            // field: caption
            if (caption != null && !caption.isBlank()) {
                baos.write(("--" + boundary + "\r\n").getBytes(StandardCharsets.UTF_8));
                baos.write(("Content-Disposition: form-data; name=\"caption\"\r\n\r\n").getBytes(StandardCharsets.UTF_8));
                baos.write((caption + "\r\n").getBytes(StandardCharsets.UTF_8));
            }

            // field: photo
            baos.write(("--" + boundary + "\r\n").getBytes(StandardCharsets.UTF_8));
            baos.write(("Content-Disposition: form-data; name=\"photo\"; filename=\"" + filename + "\"\r\n").getBytes(StandardCharsets.UTF_8));
            baos.write(("Content-Type: image/png\r\n\r\n").getBytes(StandardCharsets.UTF_8));
            baos.write(fileBytes);
            baos.write(("\r\n--" + boundary + "--\r\n").getBytes(StandardCharsets.UTF_8));

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://api.telegram.org/bot" + token + "/sendPhoto"))
                    .timeout(Duration.ofSeconds(30))
                    .header("Content-Type", "multipart/form-data; boundary=" + boundary)
                    .POST(HttpRequest.BodyPublishers.ofByteArray(baos.toByteArray()))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() >= 300) {
                System.err.println("Warning: Telegram sendPhoto returned HTTP " + response.statusCode());
            }
        } catch (Exception e) {
            System.err.println("Warning: Could not send Telegram photo: " + e.getMessage());
        }
    }

    private static String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }
}
