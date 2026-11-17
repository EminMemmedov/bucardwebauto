package az.bucard.email;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class MailTmClient implements TempEmailService {
    private static final String BASE_URL = "https://api.mail.tm";
    private static final HttpClient HTTP_CLIENT = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();
    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Override
    public EmailAccount createAccount() {
        try {
            String domain = fetchAvailableDomain();
            String username = "bucard_auto_" + System.currentTimeMillis();
            String emailAddress = username + "@" + domain;
            String defaultPassword = "BucardTest2026!";

            String accountPayload = MAPPER.writeValueAsString(new AccountRequest(emailAddress, defaultPassword));
            HttpRequest createReq = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/accounts"))
                    .header("Content-Type", "application/json")
                    .header("User-Agent", "BucardAutomatedTest/1.0")
                    .POST(HttpRequest.BodyPublishers.ofString(accountPayload))
                    .build();

            HttpResponse<String> createResp = HTTP_CLIENT.send(createReq, HttpResponse.BodyHandlers.ofString());
            if (createResp.statusCode() != 200 && createResp.statusCode() != 201) {
                throw new IllegalStateException("Mail.tm account creation failed: HTTP " + createResp.statusCode() + " body: " + createResp.body());
            }

            JsonNode accountJson = MAPPER.readTree(createResp.body());
            String accountId = accountJson.path("id").asText();

            // Token əldə edilməsi
            String tokenPayload = MAPPER.writeValueAsString(new TokenRequest(emailAddress, defaultPassword));
            HttpRequest tokenReq = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/token"))
                    .header("Content-Type", "application/json")
                    .header("User-Agent", "BucardAutomatedTest/1.0")
                    .POST(HttpRequest.BodyPublishers.ofString(tokenPayload))
                    .build();

            HttpResponse<String> tokenResp = HTTP_CLIENT.send(tokenReq, HttpResponse.BodyHandlers.ofString());
            JsonNode tokenJson = MAPPER.readTree(tokenResp.body());
            String token = tokenJson.path("token").asText();

            return new EmailAccount(emailAddress, defaultPassword, token, accountId);
        } catch (Exception e) {
            throw new RuntimeException("Mail.tm temp email generasiyası zamanı xəta: " + e.getMessage(), e);
        }
    }

    private String fetchAvailableDomain() throws Exception {
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/domains"))
                .header("User-Agent", "BucardAutomatedTest/1.0")
                .GET()
                .build();

        HttpResponse<String> resp = HTTP_CLIENT.send(req, HttpResponse.BodyHandlers.ofString());
        JsonNode root = MAPPER.readTree(resp.body());
        JsonNode members = root.path("hydra:member");
        if (members.isArray() && !members.isEmpty()) {
            return members.get(0).path("domain").asText();
        }
        return "fldubai.com";
    }

    @Override
    public String waitForLatestEmail(EmailAccount account, Duration timeout) {
        long endMs = System.currentTimeMillis() + timeout.toMillis();
        while (System.currentTimeMillis() < endMs) {
            try {
                HttpRequest listReq = HttpRequest.newBuilder()
                        .uri(URI.create(BASE_URL + "/messages"))
                        .header("Authorization", "Bearer " + account.token())
                        .header("User-Agent", "BucardAutomatedTest/1.0")
                        .GET()
                        .build();

                HttpResponse<String> listResp = HTTP_CLIENT.send(listReq, HttpResponse.BodyHandlers.ofString());
                JsonNode root = MAPPER.readTree(listResp.body());
                JsonNode messages = root.path("hydra:member");

                if (messages.isArray() && !messages.isEmpty()) {
                    String msgId = messages.get(0).path("id").asText();

                    // Məktubun tam mətni
                    HttpRequest detailReq = HttpRequest.newBuilder()
                            .uri(URI.create(BASE_URL + "/messages/" + msgId))
                            .header("Authorization", "Bearer " + account.token())
                            .header("User-Agent", "BucardAutomatedTest/1.0")
                            .GET()
                            .build();

                    HttpResponse<String> detailResp = HTTP_CLIENT.send(detailReq, HttpResponse.BodyHandlers.ofString());
                    JsonNode msgJson = MAPPER.readTree(detailResp.body());
                    String text = msgJson.path("text").asText("");
                    String html = msgJson.path("html").asText("");
                    return text + "\n" + html;
                }
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            } catch (Exception ignored) {
            }
        }
        return null;
    }

    @Override
    public String extractConfirmationLink(String emailContent) {
        if (emailContent == null) return null;
        Pattern pattern = Pattern.compile("https?://[\\w./?=&%#+-]+(?:confirm|verify|activate|token|email)[\\w./?=&%#+-]*", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(emailContent);
        if (matcher.find()) {
            return matcher.group();
        }
        return null;
    }

    @Override
    public String extractVerificationCode(String emailContent) {
        if (emailContent == null) return null;
        Pattern contextPattern = Pattern.compile("(?:kod|code|otp|şifrə|parol|təsdiq)[^\\d]*?(\\d{4})\\b", Pattern.CASE_INSENSITIVE);
        Matcher contextMatcher = contextPattern.matcher(emailContent);
        if (contextMatcher.find()) {
            return contextMatcher.group(1);
        }
        Pattern pattern = Pattern.compile("\\b(\\d{4})\\b");
        Matcher matcher = pattern.matcher(emailContent);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }

    private record AccountRequest(String address, String password) {}
    private record TokenRequest(String address, String password) {}
}
