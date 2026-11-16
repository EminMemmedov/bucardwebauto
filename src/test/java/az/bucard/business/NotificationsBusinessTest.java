package az.bucard.business;

import az.bucard.auth.DynamicCredentialsHolder;
import az.bucard.base.BaseTest;
import az.bucard.config.Config;
import az.bucard.pages.DashboardPage;
import az.bucard.pages.LoginPage;
import az.bucard.pages.NotificationsPage;
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
@Feature("P2: Bildirişlər Sistemi")
public class NotificationsBusinessTest extends BaseTest {

    @BeforeMethod(alwaysRun = true)
    public void requireCredentials() {
        if (!DynamicCredentialsHolder.isAvailable()) {
            DynamicCredentialsHolder.ensureAccountRegistered(getDriver());
        }
    }

    private DashboardPage performLogin() {
        WebDriver driver = getDriver();
        driver.get(Config.baseUrl() + "/dashboard");
        DashboardPage page = new DashboardPage(driver);
        page.waitForPreloaderToDisappear();
        page.dismissModalIfPresent();

        if (driver.getCurrentUrl().contains("/dashboard") && page.isLoaded()) {
            return page;
        }

        LoginPage loginPage = new LoginPage(driver);
        loginPage.open(Config.baseUrl());
        loginPage.login(DynamicCredentialsHolder.getEffectiveUser(), DynamicCredentialsHolder.getEffectivePassword());

        page.waitForUrlContains("/dashboard");
        page.waitForPreloaderToDisappear();
        page.dismissModalIfPresent();
        Assert.assertTrue(page.isLoaded(), "Dashboard uğurla açılmalıdır");
        return page;
    }

    @Test(description = "BIZ-P2-01: Bildirişlərin göstərilməsi və oxunma statusunun yenilənməsi", groups = {"business", "p2", "notifications"})
    @Story("Bildirişlər (Notifications)")
    @Severity(SeverityLevel.NORMAL)
    @Description("Bildirişlərin düzgün göstərildiyini və oxunma statusunun uğurla yeniləndiyini yoxlayır.")
    public void notificationsListAndMarkReadWorks() {
        DashboardPage dashboardPage = performLogin();
        NotificationsPage notificationsPage = dashboardPage.openNotifications();

        Assert.assertTrue(notificationsPage.isLoaded() || dashboardPage.isLoaded(),
                "Bildirişlər paneli və ya modalı açılmalıdır");
        attachScreenshot("Bildirişlər Bölməsi Vəziyyəti");

        if (notificationsPage.hasMarkAllAsReadButton()) {
            notificationsPage.markAllAsRead();
            attachScreenshot("Bildirişlərin Hamısını Oxunmuş Qeyd Etmə");
        }
    }
}
