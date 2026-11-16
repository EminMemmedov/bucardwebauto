package az.bucard.utils;

import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public final class ScreenshotUtil {
    private static final Path SCREENSHOT_DIR = Paths.get("target", "screenshots");
    private static final DateTimeFormatter TIMESTAMP_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss_SSS");

    private ScreenshotUtil() {}

    public static Path captureFailure(WebDriver driver, String testName) {
        if (driver == null) {
            return null;
        }
        try {
            if (!(driver instanceof TakesScreenshot takesScreenshot)) {
                return null;
            }
            byte[] bytes = takesScreenshot.getScreenshotAs(OutputType.BYTES);
            if (bytes == null || bytes.length == 0) {
                return null;
            }

            Files.createDirectories(SCREENSHOT_DIR);
            String sanitizedName = sanitizeFilename(testName);
            String timestamp = LocalDateTime.now().format(TIMESTAMP_FORMAT);
            Path targetFile = SCREENSHOT_DIR.resolve(sanitizedName + "_" + timestamp + ".png");
            Files.write(targetFile, bytes);
            return targetFile;
        } catch (IOException | RuntimeException e) {
            System.err.println("Failed to capture screenshot for test: " + testName + " (" + e.getMessage() + ")");
            return null;
        }
    }

    private static String sanitizeFilename(String name) {
        if (name == null || name.isBlank()) {
            return "test_failure";
        }
        return name.replaceAll("[^a-zA-Z0-9._-]", "_");
    }
}
