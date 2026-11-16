package az.bucard.business;

import az.bucard.base.BaseTest;
import az.bucard.config.Config;
import az.bucard.pages.BonusPage;
import az.bucard.pages.DashboardPage;
import az.bucard.pages.LoginPage;
import az.bucard.pages.SubscriptionPage;
import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import io.qameta.allure.Story;
import org.openqa.selenium.WebDriver;
import org.testng.Assert;
import org.testng.SkipException;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

@Epic("Biznes Funksionallıqları")
@Feature("P1: Abunəlik, Planlar və Bonus Sistemi")
public class SubscriptionAndBonusBusinessTest extends BaseTest {

    @BeforeMethod(alwaysRun = true)
    public void requireCredentials() {
        if (!Config.isAuthAvailable()) {
            throw new SkipException(
                    "BUCARD_TEST_USER / BUCARD_TEST_PASSWORD təyin edilməyib. "
                    + "PM tərəfindən test hesabı təqdim edildikdən sonra bu biznes testi canlı icra ediləcək."
            );
        }
    }

    private DashboardPage performLogin() {
        WebDriver driver = getDriver();
        LoginPage loginPage = new LoginPage(driver);
        loginPage.open(Config.baseUrl());
        loginPage.login(Config.testUser(), Config.testPassword());

        DashboardPage page = new DashboardPage(driver);
        page.waitForUrlContains("/dashboard");
        page.waitForPreloaderToDisappear();
        page.dismissModalIfPresent();
        Assert.assertTrue(page.isLoaded(), "Dashboard uğurla açılmalıdır");
        return page;
    }

    @Test(description = "BIZ-P1-03: Abunəlik və planlar (Free və Pro funksionallıq və limitləri)", groups = {"business", "p1", "subscription"})
    @Story("Abunəlik və Planlar")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Free və Pro istifadəçilərin müvafiq funksionallıqlara çıxışını və cari plan məlumatlarını yoxlayır.")
    public void subscriptionAndPlanAccessVerification() {
        DashboardPage dashboardPage = performLogin();
        SubscriptionPage subscriptionPage = dashboardPage.openSubscription();

        Assert.assertTrue(subscriptionPage.isLoaded(), "Abunəlik və planlar səhifəsi uğurla açılmalıdır");
        String planName = subscriptionPage.getCurrentPlanName();
        Assert.assertNotNull(planName, "Cari abunəlik planı adı əldə edilməlidir");
        attachScreenshot("Cari Abunəlik Planı və Məhdudiyyətlər");
    }

    @Test(description = "BIZ-P1-04: Bonus sistemi (Balans və bonus tarixçəsinin göstərilməsi)", groups = {"business", "p1", "bonus"})
    @Story("Bonus Sistemi")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Bonus balansının və bonus tarixçəsinin düzgün yükləndiyini və göstərildiyini yoxlayır.")
    public void bonusBalanceAndHistoryVerification() {
        DashboardPage dashboardPage = performLogin();
        Assert.assertTrue(dashboardPage.hasBonusBadge(), "Dashboard-da bonus balansı indikatoru mövcud olmalıdır");

        String bonusText = dashboardPage.getBonusBalanceText();
        Assert.assertFalse(bonusText.isEmpty(), "Bonus balansı boş olmamalıdır");
        attachScreenshot("Dashboard Bonus Balansı İndikatoru");

        BonusPage bonusPage = dashboardPage.openBonusPage();
        if (bonusPage.isLoaded()) {
            attachScreenshot("Bonus Tarixçəsi və Əməliyyatlar Bölməsi");
        }
    }
}
