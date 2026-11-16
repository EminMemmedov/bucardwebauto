package az.bucard.driver;

import az.bucard.config.Config;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

import java.time.Duration;

public final class DriverFactory {
    private DriverFactory() {}

    public static WebDriver createDriver() {
        String browser = Config.browser().toLowerCase();
        if ("chrome".equals(browser)) {
            return createChrome();
        }
        // Fallback default to Chrome
        return createChrome();
    }

    public static WebDriver createChrome() {
        ChromeOptions options = new ChromeOptions();
        if (Config.headless()) {
            options.addArguments("--headless=new");
        }
        options.addArguments(
                "--window-size=1440,1000",
                "--disable-dev-shm-usage",
                "--no-sandbox",
                "--disable-gpu",
                "--remote-allow-origins=*",
                "--disable-search-engine-choice-screen",
                "--disable-extensions",
                "--disable-background-networking"
        );
        WebDriver driver = new ChromeDriver(options);
        driver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(Config.pageLoadTimeoutSeconds()));
        return driver;
    }

    public static void quitDriver(WebDriver driver) {
        if (driver != null) {
            try {
                driver.quit();
            } catch (Exception ignored) {
                // Ignore cleanup errors during teardown
            }
        }
    }
}
