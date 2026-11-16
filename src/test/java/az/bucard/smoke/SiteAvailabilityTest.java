package az.bucard.smoke;

import az.bucard.base.BaseTest;
import az.bucard.config.Config;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.testng.Assert;
import org.testng.annotations.Test;

public class SiteAvailabilityTest extends BaseTest {

    @Test(description = "Bucard public entry point responds with a page carrying the product title", groups = {"smoke", "public"})
    public void siteIsReachable() {
        WebDriver driver = getDriver();
        driver.get(Config.baseUrl());

        String title = driver.getTitle();
        Assert.assertNotNull(title, "Page title should not be null after loading baseUrl");
        Assert.assertTrue(title.toLowerCase().contains("bucard"),
                "Expected page title to contain 'Bucard', but got: '" + title + "'");

        // Verify root single-page container is present
        Assert.assertFalse(driver.findElements(By.id("root")).isEmpty(),
                "Expected '#root' container to be present in the Bucard SPA DOM");
    }
}
