package az.bucard.smoke;

import az.bucard.auth.DynamicCredentialsHolder;
import az.bucard.base.BaseTest;
import az.bucard.config.Config;
import az.bucard.email.EmailAccount;
import az.bucard.email.GuerrillaMailClient;
import az.bucard.email.TempEmailService;
import az.bucard.pages.DashboardPage;
import az.bucard.pages.LoginPage;
import az.bucard.pages.RegisterPage;
import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import io.qameta.allure.Story;
import org.openqa.selenium.WebDriver;
import org.testng.Assert;
import org.testng.annotations.Test;

@Epic("Bucard Veb Platforması")
@Feature("Dinamik Temp-Mail Qeydiyyatı və Giriş İnteqrasiyası")
public class DynamicRegistrationSmokeTest extends BaseTest {

    private static EmailAccount dynamicAccount;
    private static final String DEFAULT_PASSWORD = "BucardTest2026!";

    @Test(description = "DYN-QEYD-01: Ödənişsiz Temp-Mail xidməti ilə unikal e-poçt generasiyası", groups = {"dynamic", "registration"})
    @Story("Müvəqqəti E-poçt Generasiyası")
    @Severity(SeverityLevel.BLOCKER)
    @Description("Ödənişsiz Temp-Mail REST API vasitəsilə qeydiyyat tələb etmədən tam unikal müvəqqəti test e-poçtu yaradır.")
    public void generateTempEmailAccount() {
        TempEmailService emailService = new GuerrillaMailClient();
        dynamicAccount = emailService.createAccount();

        Assert.assertNotNull(dynamicAccount, "Dinamik e-poçt hesabı yaradılmalıdır");
        Assert.assertTrue(dynamicAccount.isValid(), "Hesab ünvanı etibarlı olmalıdır, cari: " + dynamicAccount.address());
        Assert.assertTrue(dynamicAccount.address().contains("@"), "E-poçt ünvanında '@' simvolu olmalıdır");
    }

    @Test(description = "DYN-QEYD-02: Dinamik qeydiyyat, Dashboard-a yönləndirmə və Çıxış (Logout)", groups = {"dynamic", "registration"}, dependsOnMethods = "generateTempEmailAccount")
    @Story("Avtomatlaşdırılmış Qeydiyyat və Çıxış")
    @Severity(SeverityLevel.BLOCKER)
    @Description("/register səhifəsində dinamik e-poçt ilə qeydiyyatdan keçir, /dashboard-a keçidi və çıxış (Logout) əməliyyatını təsdiqləyir.")
    public void registerWithDynamicEmailVerifyDashboardAndLogout() {
        Assert.assertNotNull(dynamicAccount, "Qeydiyyat üçün dinamik e-poçt mövcud olmalıdır");

        WebDriver driver = getDriver();
        RegisterPage registerPage = new RegisterPage(driver);
        registerPage.open(Config.baseUrl());
        Assert.assertTrue(registerPage.isLoaded(), "Qeydiyyat səhifəsi açılmalıdır");

        attachScreenshot("Qeydiyyat Formasının İlkin Vəziyyəti");

        DashboardPage dashboardPage = registerPage.registerNewUser(dynamicAccount.address(), DEFAULT_PASSWORD);
        dashboardPage.waitForPreloaderToDisappear();
        dashboardPage.dismissModalIfPresent();

        String currentUrl = driver.getCurrentUrl();
        Assert.assertTrue(currentUrl.contains("/dashboard") || dashboardPage.isLoaded(),
                "Qeydiyyatdan sonra /dashboard səhifəsinə yönləndirmə təsdiqlənməlidir, cari: " + currentUrl);

        attachScreenshot("Dinamik Qeydiyyatdan Sonra Dashboard");

        // Çıxış (Logout) icrası
        LoginPage loginPage = dashboardPage.logout();
        Assert.assertTrue(loginPage.isLoaded() || driver.getCurrentUrl().contains("/login"),
                "Çıxışdan sonra istifadəçi Login səhifəsinə yönləndirilməlidir");

        attachScreenshot("Çıxışdan Sonra Giriş Səhifəsi");

        // Bütün sonrakı biznes testlər üçün dinamik hesabı aktivləşdiririk
        DynamicCredentialsHolder.setCredentials(dynamicAccount.address(), DEFAULT_PASSWORD);
    }

    @Test(description = "DYN-QEYD-03: Yeni qeydiyyatdan keçmiş dinamik hesab ilə təkrar giriş (Login)", groups = {"dynamic", "registration"}, dependsOnMethods = "registerWithDynamicEmailVerifyDashboardAndLogout")
    @Story("Dinamik Hesabla Giriş")
    @Severity(SeverityLevel.BLOCKER)
    @Description("Yeni yaradılmış e-poçt və şifrəni /login səhifəsində istifadə edərək uğurlu təkrar girişi təsdiqləyir.")
    public void loginWithNewlyRegisteredAccount() {
        Assert.assertNotNull(dynamicAccount, "Təkrar giriş üçün dinamik hesab mövcud olmalıdır");

        WebDriver driver = getDriver();
        LoginPage loginPage = new LoginPage(driver);
        loginPage.open(Config.baseUrl());
        loginPage.login(dynamicAccount.address(), DEFAULT_PASSWORD);

        DashboardPage dashboardPage = new DashboardPage(driver);
        dashboardPage.waitForUrlContains("/dashboard");
        dashboardPage.waitForPreloaderToDisappear();
        dashboardPage.dismissModalIfPresent();

        Assert.assertTrue(dashboardPage.isLoaded() || driver.getCurrentUrl().contains("/dashboard"),
                "Yeni yaradılmış hesabla təkrar giriş uğurla tamamlanmalıdır");

        attachScreenshot("Dinamik Hesabla Təkrar Girişdən Sonra Dashboard");
    }
}
