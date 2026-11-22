package az.bucard.unit;

import az.bucard.config.Config;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import org.testng.Assert;
import org.testng.annotations.Test;

@Epic("Unit Tests")
@Feature("Framework Configuration")
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

    @Test(description = "Verify proShareUrl derives shared-user URL from pro.username when shareUrl is unset", groups = "unit")
    public void testProShareUrlDerivedFromUsername() {
        String prevUsername = System.getProperty("bucard.pro.username");
        String prevShareUrl = System.getProperty("bucard.pro.shareUrl");
        String prevBaseUrl = System.getProperty("bucard.baseUrl");
        try {
            System.setProperty("bucard.baseUrl", "https://bucard.az");
            System.clearProperty("bucard.pro.shareUrl");
            System.setProperty("bucard.pro.username", "pro_demo_user");

            Assert.assertEquals(Config.proShareUrl(), "https://bucard.az/shared-user/pro_demo_user");
        } finally {
            restoreProp("bucard.pro.username", prevUsername);
            restoreProp("bucard.pro.shareUrl", prevShareUrl);
            restoreProp("bucard.baseUrl", prevBaseUrl);
        }
    }

    private static void restoreProp(String key, String previousValue) {
        if (previousValue == null) {
            System.clearProperty(key);
        } else {
            System.setProperty(key, previousValue);
        }
    }
}
