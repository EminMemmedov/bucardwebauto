package az.bucard.driver;

import az.bucard.config.Config;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Comparator;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;

public final class DriverFactory {
    static {
        System.setProperty("webdriver.chrome.silentOutput", "true");
        Logger.getLogger("org.openqa.selenium").setLevel(Level.SEVERE);
        Logger.getLogger("org.openqa.selenium.devtools").setLevel(Level.OFF);
    }

    private DriverFactory() {}

    public static WebDriver createDriver() {
        String browser = Config.browser().toLowerCase();
        if ("chrome".equals(browser)) {
            return createChrome();
        }
        // Fallback default to Chrome
        return createChrome();
    }

    private static ChromeOptions buildChromeOptions() {
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
                "--disable-background-networking",
                "--log-level=3",
                "--silent"
        );
        return options;
    }

    public static WebDriver createChrome() {
        WebDriver driver = new ChromeDriver(buildChromeOptions());
        driver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(Config.pageLoadTimeoutSeconds()));
        return driver;
    }

    /**
     * Starts a Chrome instance backed by a persistent profile directory so cookies,
     * localStorage, and browser state survive across full browser restarts.
     */
    public static WebDriver createChromeWithProfile(Path userDataDir) {
        ChromeOptions options = buildChromeOptions();
        if (userDataDir != null) {
            options.addArguments("--user-data-dir=" + userDataDir.toAbsolutePath());
        }
        WebDriver driver = new ChromeDriver(options);
        driver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(Config.pageLoadTimeoutSeconds()));
        return driver;
    }

    public static void deleteProfileDirQuietly(Path userDataDir) {
        if (userDataDir == null || !Files.exists(userDataDir)) {
            return;
        }
        try (Stream<Path> walk = Files.walk(userDataDir)) {
            walk.sorted(Comparator.reverseOrder())
                    .forEach(path -> {
                        try {
                            Files.deleteIfExists(path);
                        } catch (IOException ignored) {
                        }
                    });
        } catch (IOException ignored) {
        }
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
