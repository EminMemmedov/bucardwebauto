package az.bucard.unit;

import az.bucard.config.Config;
import org.testng.Assert;
import org.testng.annotations.Test;

public class ConfigTest {

    @Test(description = "Verify default base URL is https://bucard.az", groups = "unit")
    public void testDefaultBaseUrl() {
        Assert.assertNotNull(Config.baseUrl());
        Assert.assertTrue(Config.baseUrl().startsWith("http"));
    }

    @Test(description = "Verify default timeouts have positive values", groups = "unit")
    public void testDefaultTimeouts() {
        Assert.assertTrue(Config.explicitTimeoutSeconds() > 0, "Explicit timeout should be > 0");
        Assert.assertTrue(Config.pageLoadTimeoutSeconds() > 0, "Page load timeout should be > 0");
    }

    @Test(description = "Verify isAuthAvailable returns false when credentials are not configured", groups = "unit")
    public void testIsAuthAvailableWithoutCredentials() {
        if (System.getenv("BUCARD_TEST_USER") == null && System.getProperty("bucard.test.user") == null) {
            Assert.assertFalse(Config.isAuthAvailable(),
                    "isAuthAvailable should return false when credentials are not supplied");
        }
    }
}
