package az.bucard.smoke;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.testng.ITestContext;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

public class SiteAvailabilityTest {
    private WebDriver driver;

    @BeforeMethod
    public void openBrowser() {
        ChromeOptions options = new ChromeOptions();
        if (Boolean.parseBoolean(System.getProperty("browser.headless", "true"))) {
            options.addArguments("--headless=new");
        }
        options.addArguments("--window-size=1440,1000", "--disable-dev-shm-usage", "--no-sandbox");
        driver = new ChromeDriver(options);
    }

    @AfterMethod(alwaysRun = true)
    public void closeBrowser() {
        if (driver != null) {
            driver.quit();
        }
    }

    @Test(description = "Bucard responds with a page carrying the product title")
    public void siteIsReachable() {
        String baseUrl = System.getProperty("bucard.baseUrl", "https://bucard.az");
        driver.get(baseUrl);
        String title = driver.getTitle();
        Assert.assertTrue(title != null && title.toLowerCase().contains("bucard"),
                "Expected the Bucard page title after loading the site");
    }

    @AfterSuite(alwaysRun = true)
    public void sendTelegramSummary(ITestContext context) {
        String token = System.getenv("TELEGRAM_BOT_TOKEN");
        String chatId = System.getenv("TELEGRAM_CHAT_ID");
        if (token == null || token.isBlank() || chatId == null || chatId.isBlank()) {
            return;
        }
        long seconds = Duration.ofMillis(context.getEndDate().getTime() - context.getStartDate().getTime()).toSeconds();
        String message = "Bucard UI smoke\n"
                + "Result: " + context.getPassedTests().size() + " passed, "
                + context.getFailedTests().size() + " failed, "
                + context.getSkippedTests().size() + " skipped\n"
                + "Duration: " + seconds + "s";
        String body = "chat_id=" + encode(chatId) + "&text=" + encode(message);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://api.telegram.org/bot" + token + "/sendMessage"))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();
        try {
            HttpResponse<Void> response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.discarding());
            if (response.statusCode() >= 300) {
                throw new IllegalStateException("Telegram returned HTTP " + response.statusCode());
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Telegram report was interrupted", e);
        } catch (Exception e) {
            throw new IllegalStateException("Could not send Telegram test summary", e);
        }
    }

    private static String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }
}
