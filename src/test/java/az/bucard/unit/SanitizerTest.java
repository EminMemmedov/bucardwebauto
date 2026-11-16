package az.bucard.unit;

import az.bucard.utils.Sanitizer;
import org.testng.Assert;
import org.testng.annotations.Test;

public class SanitizerTest {

    @Test(description = "Verify null or empty messages are handled gracefully", groups = "unit")
    public void testNullOrEmpty() {
        Assert.assertEquals(Sanitizer.sanitizeErrorMessage(null), "");
        Assert.assertEquals(Sanitizer.sanitizeErrorMessage(""), "");
        Assert.assertEquals(Sanitizer.sanitizeErrorMessage("   "), "");
    }

    @Test(description = "Verify password and token parameters are redacted", groups = "unit")
    public void testRedactsSensitiveParameters() {
        String raw = "Login failed for user test with password=SuperSecretPassword123 and token=abc999xyz";
        String sanitized = Sanitizer.sanitizeErrorMessage(raw);
        Assert.assertFalse(sanitized.contains("SuperSecretPassword123"));
        Assert.assertFalse(sanitized.contains("abc999xyz"));
        Assert.assertTrue(sanitized.contains("password=[REDACTED]"));
        Assert.assertTrue(sanitized.contains("token=[REDACTED]"));
    }

    @Test(description = "Verify sensitive URLs with query parameters are redacted", groups = "unit")
    public void testRedactsQueryUrls() {
        String raw = "Failed to load https://bucard.az/api/v1/auth?session=secretSessionId123";
        String sanitized = Sanitizer.sanitizeErrorMessage(raw);
        Assert.assertFalse(sanitized.contains("secretSessionId123"));
        Assert.assertTrue(sanitized.contains("[REDACTED_URL]"));
    }

    @Test(description = "Verify long messages are truncated without exceeding limits", groups = "unit")
    public void testTruncatesLongMessages() {
        String longMessage = "A".repeat(200);
        String sanitized = Sanitizer.sanitizeErrorMessage(longMessage);
        Assert.assertTrue(sanitized.length() <= 100);
        Assert.assertTrue(sanitized.endsWith("..."));
    }
}
