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

public final class GuerrillaMailClient implements TempEmailService {
    private static final String API_URL = "https://api.guerrillamail.com/ajax.php";
    private static final HttpClient HTTP_CLIENT = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();
    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Override
    public EmailAccount createAccount() {
        try {
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(API_URL + "?f=get_email_address"))
                    .header("User-Agent", "BucardAutomatedTest/1.0")
                    .GET()
                    .build();

            HttpResponse<String> resp = HTTP_CLIENT.send(req, HttpResponse.BodyHandlers.ofString());
            JsonNode root = MAPPER.readTree(resp.body());
            String address = root.path("email_addr").asText();
            String sidToken = root.path("sid_token").asText();

            if (address != null && !address.isBlank()) {
                return new EmailAccount(address, "BucardTest2026!", sidToken, sidToken);
            }
        } catch (Exception ignored) {
        }

        // Fallback: sharklasers.com unikal ünvan generasiyası
        String fallback = "bucard_auto_" + System.currentTimeMillis() + "@sharklasers.com";
        return new EmailAccount(fallback, "BucardTest2026!", null, null);
    }

    @Override
    public String waitForLatestEmail(EmailAccount account, Duration timeout) {
        if (account.token() == null) {
            return null;
        }
        long endMs = System.currentTimeMillis() + timeout.toMillis();
        while (System.currentTimeMillis() < endMs) {
            try {
                HttpRequest req = HttpRequest.newBuilder()
                        .uri(URI.create(API_URL + "?f=check_email&seq=0&sid_token=" + account.token()))
                        .header("User-Agent", "BucardAutomatedTest/1.0")
                        .GET()
                        .build();

                HttpResponse<String> resp = HTTP_CLIENT.send(req, HttpResponse.BodyHandlers.ofString());
                JsonNode root = MAPPER.readTree(resp.body());
                JsonNode list = root.path("list");
                if (list.isArray() && !list.isEmpty()) {
                    String mailId = list.get(0).path("mail_id").asText();

                    HttpRequest fetchReq = HttpRequest.newBuilder()
                            .uri(URI.create(API_URL + "?f=fetch_email&email_id=" + mailId + "&sid_token=" + account.token()))
                            .header("User-Agent", "BucardAutomatedTest/1.0")
                            .GET()
                            .build();

                    HttpResponse<String> fetchResp = HTTP_CLIENT.send(fetchReq, HttpResponse.BodyHandlers.ofString());
                    JsonNode mailJson = MAPPER.readTree(fetchResp.body());
                    return mailJson.path("mail_body").asText("");
                }
                Thread.sleep(2500);
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
        Pattern pattern = Pattern.compile("\\b(\\d{4,6})\\b");
        Matcher matcher = pattern.matcher(emailContent);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }
}
