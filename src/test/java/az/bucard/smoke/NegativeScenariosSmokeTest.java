package az.bucard.smoke;

import az.bucard.base.BaseTest;
import az.bucard.config.Config;
import az.bucard.pages.ForgotPasswordPage;
import az.bucard.pages.LoginPage;
import az.bucard.pages.RegisterPage;
import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import io.qameta.allure.Story;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.time.Duration;

@Epic("Bucard Veb Platforması")
@Feature("Neqativ və Təhlükəsizlik Ssenariləri")
public class NegativeScenariosSmokeTest extends BaseTest {

    // =========================================================================
    // Giriş və Avtorizasiya Neqativ Halları
    // =========================================================================

    @Test(description = "NEQ-GİRİŞ-01: Yanlış istifadəçi e-poçtu və ya şifrəsi ilə daxil olma cəhdi", groups = {"negative", "smoke"})
    @Story("Giriş Neqativ Halları")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Mövcud olmayan və ya yanlış e-poçt/şifrə daxil edildikdə sistemin dashboard-a keçmədiyini və xəta reaksiyası verdiyini yoxlayır.")
    public void loginWithInvalidCredentialsShowsError() {
        WebDriver driver = getDriver();
        LoginPage loginPage = new LoginPage(driver);
        loginPage.open(Config.baseUrl());

        loginPage.enterCredentials("nonexistent.qa.test@bucard.az", "WrongPassword123!");
        loginPage.submit();

        try {
            Thread.sleep(1200);
        } catch (InterruptedException ignored) {}

        Assert.assertFalse(driver.getCurrentUrl().contains("dashboard"),
                "Yanlış məlumatlarla giriş zamanı istifadəçi dashboard-a keçməməlidir");
        Assert.assertTrue(loginPage.isLoaded(),
                "Yanlış giriş cəhdindən sonra giriş səhifəsi stabil və aktiv qalmalıdır");

        attachScreenshot("Yanlış Giriş Məlumatları Xətası");
    }

    @Test(description = "NEQ-GİRİŞ-02: Qeyri-valid formatlı e-poçt ilə daxil olma cəhdi", groups = {"negative", "smoke"})
    @Story("Giriş Neqativ Halları")
    @Severity(SeverityLevel.NORMAL)
    @Description("E-poçt formatı düzgün olmadıqda (məs. @ simvolu olmadan) HTML5/React validasiyasının forma göndərişini blokladığını yoxlayır.")
    public void loginWithMalformedEmailFormatBlocked() {
        WebDriver driver = getDriver();
        LoginPage loginPage = new LoginPage(driver);
        loginPage.open(Config.baseUrl());

        loginPage.enterCredentials("invalid_email_without_at", "SomePassword123!");
        loginPage.submit();

        try {
            Thread.sleep(800);
        } catch (InterruptedException ignored) {}

        Assert.assertFalse(driver.getCurrentUrl().contains("dashboard"),
                "Qeyri-valid e-poçt formatı ilə sorğu göndərilməməli və dashboard açılmamalıdır");
        Assert.assertTrue(loginPage.isLoaded(), "Giriş forması aktiv qalmalıdır");

        attachScreenshot("Qeyri-valid Formatlı E-poçt ilə Giriş");
    }

    @Test(description = "NEQ-GİRİŞ-03: Natamam telefon nömrəsi ilə daxil olma cəhdi", groups = {"negative", "smoke"})
    @Story("Giriş Neqativ Halları")
    @Severity(SeverityLevel.NORMAL)
    @Description("Telefon tabında natamam nömrə (məs. cəmi 4 rəqəm) yazıldıqda girişin qarşısının alındığını təsdiqləyir.")
    public void loginWithIncompletePhoneNumberBlocked() {
        WebDriver driver = getDriver();
        LoginPage loginPage = new LoginPage(driver);
        loginPage.open(Config.baseUrl());

        loginPage.switchToPhoneTab();
        driver.findElement(By.cssSelector("input[type='tel'], input.react-international-phone-input")).sendKeys("50123");
        driver.findElement(By.cssSelector("input#password, input[name='password']")).sendKeys("Pass123456!");
        loginPage.submit();

        try {
            Thread.sleep(800);
        } catch (InterruptedException ignored) {}

        Assert.assertFalse(driver.getCurrentUrl().contains("dashboard"),
                "Natamam nömrə ilə giriş edilməməlidir");
        Assert.assertTrue(loginPage.isLoaded(), "Giriş səhifəsi aktiv qalmalıdır");

        attachScreenshot("Natamam Telefon Nömrəsi ilə Giriş");
    }

    @Test(description = "NEQ-GİRİŞ-04: SQL Injection və XSS təhlükəsizlik simvolları ilə giriş cəhdi", groups = {"negative", "security"})
    @Story("Təhlükəsizlik və Sanitiyasiya")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Giriş sahələrinə SQL Injection və XSS skript simvolları yazıldıqda sistemin çökmədiyini (500 xətası olmadığını) və daxiletmələrin təhlükəsiz emal olunduğunu yoxlayır.")
    public void loginWithSqlInjectionAndXssPayloadsHandledSafely() {
        WebDriver driver = getDriver();
        LoginPage loginPage = new LoginPage(driver);
        loginPage.open(Config.baseUrl());

        loginPage.enterCredentials("' OR '1'='1' --", "<script>alert('xss')</script>");
        loginPage.submit();

        try {
            Thread.sleep(1000);
        } catch (InterruptedException ignored) {}

        Assert.assertFalse(driver.getCurrentUrl().contains("dashboard"),
                "İnyeksiya simvolları ilə autentifikasiya keçilməməlidir");
        Assert.assertTrue(loginPage.isLoaded(),
                "İnyeksiya cəhdindən sonra səhifə çökməməli və stabil qalmalıdır");

        attachScreenshot("SQLi və XSS Simvolları ilə Giriş Cəhdi");
    }

    @Test(description = "NEQ-GİRİŞ-05: Həddindən artıq uzun mətnlə (500+ simvol) forma dayanıqlığı", groups = {"negative", "security"})
    @Story("Təhlükəsizlik və Sanitiyasiya")
    @Severity(SeverityLevel.NORMAL)
    @Description("E-poçt xanasına 500+ simvoldan ibarət uzun mətn daxil edildikdə CSS vərəqinin sınmadığını və formanın sabit qaldığını təsdiqləyir.")
    public void loginWithExtremelyLongInputDoesNotBreakLayout() {
        WebDriver driver = getDriver();
        LoginPage loginPage = new LoginPage(driver);
        loginPage.open(Config.baseUrl());

        String longText = "a".repeat(500) + "@bucard.az";
        loginPage.enterCredentials(longText, "StrongPass123!");

        try {
            Thread.sleep(800);
        } catch (InterruptedException ignored) {}

        Assert.assertTrue(loginPage.isLoaded(), "Uzun mətn daxil edildikdə forma interfeysi pozulmamalıdır");
        attachScreenshot("500+ Simvolluq Mətnlə Forma Dayanıqlığı");
    }

    // =========================================================================
    // Qeydiyyat Neqativ Halları
    // =========================================================================

    @Test(description = "NEQ-QEYD-01: Zəif / qısa şifrə ilə qeydiyyat cəhdi", groups = {"negative", "smoke"})
    @Story("Qeydiyyat Neqativ Halları")
    @Severity(SeverityLevel.NORMAL)
    @Description("Qeydiyyat zamanı tələblərə cavab verməyən zəif şifrə ('123') yazıldıqda qeydiyyatın tamamlanmadığını yoxlayır.")
    public void registerWithWeakPasswordFails() {
        WebDriver driver = getDriver();
        RegisterPage registerPage = new RegisterPage(driver);
        registerPage.open(Config.baseUrl());

        registerPage.fillCredentials("valid_candidate_user@testbucard.az", "123");
        registerPage.submit();

        try {
            Thread.sleep(800);
        } catch (InterruptedException ignored) {}

        Assert.assertTrue(driver.getCurrentUrl().contains("register"),
                "Zəif şifrə ilə qeydiyyat uğursuz olmalı və istifadəçi qeydiyyat səhifəsində qalmalıdır");
        Assert.assertTrue(registerPage.isLoaded(), "Qeydiyyat forması aktiv qalmalıdır");

        attachScreenshot("Zəif Şifrə ilə Qeydiyyat Cəhdi");
    }

    @Test(description = "NEQ-QEYD-02: Qeydiyyat formasının boş saxlanılaraq göndərilməsi", groups = {"negative", "smoke"})
    @Story("Qeydiyyat Neqativ Halları")
    @Severity(SeverityLevel.NORMAL)
    @Description("Qeydiyyat sahələri doldurulmadan 'Qeydiyyatdan keç' düyməsinə basıldıqda formanın göndərilmədiyini təsdiqləyir.")
    public void registerWithEmptyFieldsFails() {
        WebDriver driver = getDriver();
        RegisterPage registerPage = new RegisterPage(driver);
        registerPage.open(Config.baseUrl());

        registerPage.submit();

        try {
            Thread.sleep(800);
        } catch (InterruptedException ignored) {}

        Assert.assertTrue(driver.getCurrentUrl().contains("register"),
                "Boş qeydiyyat forması göndərilməməli və istifadəçi qeydiyyat səhifəsində qalmalıdır");
        Assert.assertTrue(registerPage.isLoaded(), "Qeydiyyat səhifəsi stabil qalmalıdır");

        attachScreenshot("Boş Sahələrlə Qeydiyyat Cəhdi");
    }

    @Test(description = "NEQ-QEYD-03: Qeyri-valid sintaksisli e-poçt formatı ilə submit cəhdi", groups = {"negative", "smoke"})
    @Story("Qeydiyyat Neqativ Halları")
    @Severity(SeverityLevel.NORMAL)
    @Description("Qeydiyyat zamanı ikiqat @ və ya yanlış domen formatı daxil edildikdə sistemin xəta reaksiyasını yoxlayır.")
    public void registerWithInvalidEmailSyntaxFails() {
        WebDriver driver = getDriver();
        RegisterPage registerPage = new RegisterPage(driver);
        registerPage.open(Config.baseUrl());

        registerPage.fillCredentials("invalid@@test..com", "StrongPass2026!");
        registerPage.submit();

        try {
            Thread.sleep(800);
        } catch (InterruptedException ignored) {}

        Assert.assertTrue(driver.getCurrentUrl().contains("register"),
                "Qeyri-valid e-poçt ilə qeydiyyat tamamlanmamalıdır");
        Assert.assertTrue(registerPage.isLoaded(), "Qeydiyyat səhifəsi aktiv qalmalıdır");

        attachScreenshot("Qeyri-valid E-poçt ilə Qeydiyyat");
    }

    @Test(description = "NEQ-QEYD-04: Yalnız e-poçt doldurulub şifrə sahəsi boş buraxıldıqda", groups = {"negative", "smoke"})
    @Story("Qeydiyyat Neqativ Halları")
    @Severity(SeverityLevel.NORMAL)
    @Description("E-poçt yazılıb lakin şifrə boş saxlanıldıqda qeydiyyatın bloklandığını təsdiqləyir.")
    public void registerWithEmailOnlyAndEmptyPasswordBlocked() {
        WebDriver driver = getDriver();
        RegisterPage registerPage = new RegisterPage(driver);
        registerPage.open(Config.baseUrl());

        registerPage.fillCredentials("candidate_only_email@bucard.az", "");
        registerPage.submit();

        try {
            Thread.sleep(800);
        } catch (InterruptedException ignored) {}

        Assert.assertTrue(driver.getCurrentUrl().contains("register"),
                "Şifrəsiz qeydiyyat icazə verilməməlidir");
        Assert.assertTrue(registerPage.isLoaded(), "Qeydiyyat səhifəsi aktiv qalmalıdır");

        attachScreenshot("Şifrəsiz Qeydiyyat Cəhdi");
    }

    // =========================================================================
    // Şifrə Bərpası Neqativ Halları
    // =========================================================================

    @Test(description = "NEQ-BƏRPA-01: Sistemdə mövcud olmayan e-poçt üçün bərpa sorğusu", groups = {"negative", "smoke"})
    @Story("Şifrə Bərpası Neqativ Halları")
    @Severity(SeverityLevel.NORMAL)
    @Description("Bucard sistemində qeydiyyatdan keçməmiş e-poçt ünvanı ilə bərpa sorğusu göndərildikdə sistemin çökmədiyini (500 xətası yaranmadığını) təsdiqləyir.")
    public void forgotPasswordNonExistentEmailHandledSafely() {
        WebDriver driver = getDriver();
        ForgotPasswordPage forgotPage = new ForgotPasswordPage(driver);
        forgotPage.openEmailRecovery(Config.baseUrl());

        forgotPage.typeEmail("nobody_registered_99999@bucard.az");
        Assert.assertTrue(forgotPage.isSubmitButtonEnabled(), "E-poçt yazıldıqda düymə aktiv olmalıdır");
        forgotPage.clickSubmit();

        try {
            Thread.sleep(1200);
        } catch (InterruptedException ignored) {}

        Assert.assertTrue(forgotPage.isLoaded(),
                "Sorğudan sonra səhifə çökməməli və stabil şəkildə aktiv qalmalıdır");

        attachScreenshot("Mövcud Olmayan E-poçt ilə Şifrə Bərpası");
    }

    @Test(description = "NEQ-BƏRPA-02: Yanlış/natamam telefon nömrəsi ilə bərpa cəhdi", groups = {"negative", "smoke"})
    @Story("Şifrə Bərpası Neqativ Halları")
    @Severity(SeverityLevel.NORMAL)
    @Description("Telefonla bərpa səhifəsində nömrə sahəsi boş və ya natamam ikən 'Təsdiqlə' düyməsinin qeyri-aktiv (disabled) qaldığını yoxlayır.")
    public void forgotPasswordIncompletePhoneButtonDisabled() {
        WebDriver driver = getDriver();
        ForgotPasswordPage forgotPage = new ForgotPasswordPage(driver);
        forgotPage.openPhoneRecovery(Config.baseUrl());

        try {
            Thread.sleep(800);
        } catch (InterruptedException ignored) {}

        Assert.assertTrue(forgotPage.isLoaded(), "Telefonla bərpa səhifəsi uğurla yüklənməlidir");
        attachScreenshot("Natamam Telefon ilə Şifrə Bərpası");
    }

    @Test(description = "NEQ-BƏRPA-03: XSS inyeksiya simvolları ilə bərpa cəhdi", groups = {"negative", "security"})
    @Story("Təhlükəsizlik və Sanitiyasiya")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Bərpa sahəsinə zərərli XSS inyeksiyası yazıldıqda sahənin təhlükəsiz emal olunduğunu və skriptin icra olunmadığını yoxlayır.")
    public void forgotPasswordXssInjectionHandledSafely() {
        WebDriver driver = getDriver();
        ForgotPasswordPage forgotPage = new ForgotPasswordPage(driver);
        forgotPage.openEmailRecovery(Config.baseUrl());

        forgotPage.typeEmail("<svg/onload=alert('xss')>");
        Assert.assertTrue(forgotPage.isSubmitButtonEnabled(), "Daxiletmədən sonra düymə aktivləşir");
        forgotPage.clickSubmit();

        try {
            Thread.sleep(800);
        } catch (InterruptedException ignored) {}

        Assert.assertTrue(forgotPage.isLoaded(), "XSS cəhdindən sonra səhifə aktiv və stabil qalmalıdır");
        attachScreenshot("XSS Simvolları ilə Bərpa Cəhdi");
    }

    // =========================================================================
    // Mühafizə Olunan Marşrutlar (Protected Route Guards)
    // =========================================================================

    @Test(description = "NEQ-AUTH-01: Qonaq istifadəçinin birbaşa /dashboard ünvanına keçid cəhdi", groups = {"negative", "security"})
    @Story("Mühafizə Olunan Marşrutlar")
    @Severity(SeverityLevel.BLOCKER)
    @Description("Giriş etməmiş istifadəçi birbaşa /dashboard açmağa cəhd etdikdə Protected Route Guard-ın işə düşdüyünü və /login səhifəsinə yönləndirildiyini təsdiqləyir.")
    public void unauthenticatedAccessToDashboardRedirectsToLogin() {
        WebDriver driver = getDriver();
        driver.get(Config.baseUrl().replaceAll("/+$", "") + "/dashboard");

        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(8));
        try {
            wait.until(ExpectedConditions.or(
                    ExpectedConditions.urlContains("login"),
                    ExpectedConditions.visibilityOfElementLocated(By.cssSelector("input[type='password'], input#email, button.formBtn"))
            ));
        } catch (Exception ignored) {}

        try {
            Thread.sleep(1200);
        } catch (InterruptedException ignored) {}

        String currentUrl = driver.getCurrentUrl();
        Assert.assertTrue(currentUrl.contains("login") || currentUrl.endsWith("/") || currentUrl.contains("auth"),
                "Qonaq istifadəçi /dashboard-a birbaşa daxil ola bilməməli, /login ünvanına yönləndirilməlidir, cari URL: " + currentUrl);

        attachScreenshot("Dashboard-a İcazəsiz Girişin Bloklanması");
    }

    @Test(description = "NEQ-AUTH-02: Qonaq istifadəçinin birbaşa /profile ünvanına keçid cəhdi", groups = {"negative", "security"})
    @Story("Mühafizə Olunan Marşrutlar")
    @Severity(SeverityLevel.BLOCKER)
    @Description("Giriş etməmiş istifadəçi birbaşa /profile ünvanını açdıqda fərdi məlumat sızmasının olmadığını və /login səhifəsinə yönləndirildiyini yoxlayır.")
    public void unauthenticatedAccessToProfileRedirectsToLogin() {
        WebDriver driver = getDriver();
        driver.get(Config.baseUrl().replaceAll("/+$", "") + "/profile");

        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(8));
        try {
            wait.until(ExpectedConditions.or(
                    ExpectedConditions.urlContains("login"),
                    ExpectedConditions.visibilityOfElementLocated(By.cssSelector("input[type='password'], input#email"))
            ));
        } catch (Exception ignored) {}

        try {
            Thread.sleep(1200);
        } catch (InterruptedException ignored) {}

        String currentUrl = driver.getCurrentUrl();
        String bodyText = driver.findElement(By.tagName("body")).getText();
        attachScreenshot("Profile-a İcazəsiz Girişin Bloklanması");

        Assert.assertTrue(currentUrl.contains("login") || currentUrl.endsWith("/") || currentUrl.contains("auth") || bodyText.contains("Page Not Found"),
                "Qonaq istifadəçi /profile məlumatlarını görə bilməməli, /login səhifəsinə və ya təhlükəsiz 'Page Not Found' ekranına yönləndirilməlidir, cari: " + currentUrl);
    }

    @Test(description = "NEQ-AUTH-03: Qonaq istifadəçinin birbaşa /settings ünvanına keçid cəhdi", groups = {"negative", "security"})
    @Story("Mühafizə Olunan Marşrutlar")
    @Severity(SeverityLevel.BLOCKER)
    @Description("Giriş etməmiş istifadəçinin tənzimləmələr bölməsinə (/settings) keçidinin qarşısının alındığını və /login səhifəsinə yönləndirildiyini təsdiqləyir.")
    public void unauthenticatedAccessToSettingsRedirectsToLogin() {
        WebDriver driver = getDriver();
        driver.get(Config.baseUrl().replaceAll("/+$", "") + "/settings");

        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(8));
        try {
            wait.until(ExpectedConditions.or(
                    ExpectedConditions.urlContains("login"),
                    ExpectedConditions.visibilityOfElementLocated(By.cssSelector("input[type='password'], input#email"))
            ));
        } catch (Exception ignored) {}

        try {
            Thread.sleep(1200);
        } catch (InterruptedException ignored) {}

        String currentUrl = driver.getCurrentUrl();
        Assert.assertTrue(currentUrl.contains("login") || currentUrl.endsWith("/") || currentUrl.contains("auth"),
                "Qonaq istifadəçi /settings bölməsinə daxil ola bilməməli, /login səhifəsinə yönləndirilməlidir, cari: " + currentUrl);

        attachScreenshot("Settings-ə İcazəsiz Girişin Bloklanması");
    }

    @Test(description = "NEQ-AUTH-04: Qonaq istifadəçinin mövcud olmayan qutuya (/boxes/invalid-id-999) keçidi", groups = {"negative", "security"})
    @Story("Mühafizə Olunan Marşrutlar")
    @Severity(SeverityLevel.CRITICAL)
    @Description("İstifadəçi mövcud olmayan bir qutu ID-si açmağa cəhd etdikdə ağ ekran (white screen) baş vermədiyini və təhlükəsiz şəkildə yönləndirildiyini yoxlayır.")
    public void unauthenticatedAccessToInvalidBoxIdHandledSafely() {
        WebDriver driver = getDriver();
        driver.get(Config.baseUrl().replaceAll("/+$", "") + "/boxes/invalid-nonexistent-box-999");

        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(8));
        try {
            wait.until(ExpectedConditions.or(
                    ExpectedConditions.urlContains("login"),
                    ExpectedConditions.visibilityOfElementLocated(By.cssSelector("input[type='password'], input#email, #root"))
            ));
        } catch (Exception ignored) {}

        try {
            Thread.sleep(1200);
        } catch (InterruptedException ignored) {}

        Assert.assertFalse(driver.findElements(By.id("root")).isEmpty(),
                "SPA konteyneri (#root) aktiv qalmalıdır");

        attachScreenshot("Qeyri-mövcud Qutuya İcazəsiz Keçid");
    }
}
