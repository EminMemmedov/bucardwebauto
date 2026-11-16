package az.bucard.smoke;

import az.bucard.base.BaseTest;
import az.bucard.config.Config;
import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import io.qameta.allure.Story;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.testng.Assert;
import org.testng.annotations.Test;

@Epic("Bucard Veb Platforması")
@Feature("Saytın Əlçatanlığı və İnfrastruktur")
public class SiteAvailabilityTest extends BaseTest {

    @Test(description = "SAYT-01: Bucard əsas səhifəsinin açılması və başlıq yoxlanışı", groups = {"smoke", "public"})
    @Story("Saytın Əsas Giriş Nöqtəsi")
    @Severity(SeverityLevel.BLOCKER)
    @Description("Bucard platformasının aktivliyini, səhifə başlığında 'Bucard' sözünün olduğunu və #root SPA konteynerinin uğurla yükləndiyini təsdiqləyir.")
    public void siteIsReachable() {
        WebDriver driver = getDriver();
        driver.get(Config.baseUrl());

        String title = driver.getTitle();
        Assert.assertNotNull(title, "Səhifə başlığı boş olmamalıdır");
        Assert.assertTrue(title.toLowerCase().contains("bucard"),
                "Səhifə başlığında 'Bucard' sözü olmalıdır, alınan başlıq: '" + title + "'");

        Assert.assertFalse(driver.findElements(By.id("root")).isEmpty(),
                "Bucard SPA DOM-da '#root' konteyneri mövcud olmalıdır");

        attachScreenshot("Bucard əsas səhifəsinin ilkin görünüşü");
    }
}
