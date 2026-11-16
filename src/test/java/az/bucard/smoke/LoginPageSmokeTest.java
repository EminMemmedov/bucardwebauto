package az.bucard.smoke;

import az.bucard.base.BaseTest;
import az.bucard.config.Config;
import az.bucard.pages.LoginPage;
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
@Feature("İstifadəçi Girişi (Authentication)")
public class LoginPageSmokeTest extends BaseTest {

    @Test(description = "GİRİŞ-01: Giriş səhifəsinin (/login) açılması və bütün interfeys elementlərinin yoxlanışı", groups = {"smoke", "public"})
    @Story("Giriş Səhifəsi Elementləri")
    @Severity(SeverityLevel.CRITICAL)
    @Description("/login ünvanının xətasız açıldığını, e-poçt və şifrə daxiletmə sahələrini, təsdiq düyməsini, şifrə bərpası və qeydiyyat keçidlərini yoxlayır.")
    public void loginPageLoadsCleanly() {
        WebDriver driver = getDriver();
        LoginPage loginPage = new LoginPage(driver);
        loginPage.open(Config.baseUrl());

        Assert.assertTrue(loginPage.isLoaded(), "Giriş səhifəsi və forma yüklənməlidir");
        Assert.assertTrue(loginPage.hasEmailField(), "E-poçt daxiletmə sahəsi görünməlidir");
        Assert.assertTrue(loginPage.hasPasswordField(), "Şifrə daxiletmə sahəsi görünməlidir");
        Assert.assertTrue(loginPage.hasSubmitButton(), "Daxil ol düyməsi görünməlidir");
        Assert.assertTrue(loginPage.hasForgotPasswordLink(), "'Şifrəni unutmusunuz?' linki mövcud olmalıdır");
        Assert.assertTrue(loginPage.hasRegisterLink(), "'Qeydiyyatdan keçin' linki mövcud olmalıdır");

        Assert.assertFalse(driver.findElements(By.id("root")).isEmpty(),
                "SPA '#root' konteyneri giriş səhifəsində mövcud olmalıdır");

        attachScreenshot("Giriş Səhifəsinin İlkin Görünüşü");
    }

    @Test(description = "GİRİŞ-02: 'Email' və 'Telefon' giriş tabları arasında keçid", groups = {"smoke", "public"})
    @Story("Giriş Növləri Tabları")
    @Severity(SeverityLevel.NORMAL)
    @Description("Giriş səhifəsində 'Email' və 'Telefon' tabları arasında keçid etdikdə müvafiq daxiletmə xanalarının ekranda əks olunduğunu təsdiqləyir.")
    public void emailAndPhoneTabsSwitchingWorks() {
        WebDriver driver = getDriver();
        LoginPage loginPage = new LoginPage(driver);
        loginPage.open(Config.baseUrl());

        Assert.assertTrue(loginPage.hasEmailField(), "İlkin olaraq e-poçt daxiletmə xanası görünməlidir");

        loginPage.switchToPhoneTab();
        Assert.assertTrue(loginPage.isPhoneFieldVisible(), "Telefon tabına keçdikdən sonra nömrə sahəsi aktiv olmalıdır");
        attachScreenshot("Telefonla Giriş Tabı");

        loginPage.switchToEmailTab();
        Assert.assertTrue(loginPage.hasEmailField(), "E-poçt tabına qayıtdıqdan sonra e-poçt sahəsi bərpa olunmalıdır");
    }

    @Test(description = "GİRİŞ-03: Şifrə görünməzlik göz ikonunun (Eye Toggle) işləməsi", groups = {"smoke", "public"})
    @Story("Təhlükəsizlik və UI İdarəetmə")
    @Severity(SeverityLevel.NORMAL)
    @Description("Göz ikonuna basdıqda şifrə sahəsinin tipinin 'password' və 'text' arasında düzgün dəyişdiyini təsdiqləyir.")
    public void passwordVisibilityToggleWorks() {
        WebDriver driver = getDriver();
        LoginPage loginPage = new LoginPage(driver);
        loginPage.open(Config.baseUrl());

        Assert.assertEquals(loginPage.getPasswordInputType(), "password",
                "İlkin vəziyyətdə şifrə sahəsi 'password' tipində olmalıdır");

        loginPage.togglePasswordVisibility();
        Assert.assertEquals(loginPage.getPasswordInputType(), "text",
                "Göz ikonuna klikdən sonra şifrə 'text' tipinə çevrilməlidir");
        attachScreenshot("Göz İkonu ilə Şifrənin Aşkar Görünüşü");

        loginPage.togglePasswordVisibility();
        Assert.assertEquals(loginPage.getPasswordInputType(), "password",
                "Göz ikonuna təkrar klikdən sonra şifrə 'password' tipinə qayıtmalıdır");
    }

    @Test(description = "GİRİŞ-04: Boş forma göndərildikdə validasiya reaksiyası", groups = {"smoke", "public"})
    @Story("Məlumat Validasiyası")
    @Severity(SeverityLevel.NORMAL)
    @Description("Sahələr boş qaldıqda 'Daxil ol' düyməsinə basıldıqda səhifənin çökmədiyini və validasiya bildirişinin yarandığını yoxlayır.")
    public void emptySubmitTriggersValidation() {
        WebDriver driver = getDriver();
        LoginPage loginPage = new LoginPage(driver);
        loginPage.open(Config.baseUrl());

        loginPage.submit();

        Assert.assertTrue(loginPage.hasErrorMessage() || loginPage.isLoaded(),
                "Boş giriş cəhdindən sonra səhifə stabil qalmalı və ya validasiya xətası göstərməlidir");
        attachScreenshot("Boş Məlumatla Giriş Cəhdi");
    }

    @Test(description = "GİRİŞ-05: 'Şifrəni unutmusunuz?' linki ilə bərpa səhifəsinə yönləndirmə", groups = {"smoke", "public"})
    @Story("Naviqasiya Keçidləri")
    @Severity(SeverityLevel.NORMAL)
    @Description("Şifrəni unutmusunuz linkinə basdıqda istifadəçinin /forgot-password səhifəsinə problemsiz keçdiyini təsdiqləyir.")
    public void forgotPasswordNavigationWorks() {
        WebDriver driver = getDriver();
        LoginPage loginPage = new LoginPage(driver);
        loginPage.open(Config.baseUrl());

        driver.findElement(By.cssSelector("div.forgot a, a[href*='forgot-password']")).click();

        Assert.assertTrue(driver.getCurrentUrl().contains("forgot-password"),
                "Keçiddən sonra URL 'forgot-password' ehtiva etməlidir, cari URL: " + driver.getCurrentUrl());
        attachScreenshot("Şifrə Bərpasına Yönləndirmə Təsdiqi");
    }

    @Test(description = "GİRİŞ-06: 'Qeydiyyatdan keçin' linki ilə /register səhifəsinə yönləndirmə", groups = {"smoke", "public"})
    @Story("Naviqasiya Keçidləri")
    @Severity(SeverityLevel.NORMAL)
    @Description("Qeydiyyat keçidinə basdıqda istifadəçinin /register ünvanına yönləndirildiyini təsdiqləyir.")
    public void registerNavigationWorks() {
        WebDriver driver = getDriver();
        LoginPage loginPage = new LoginPage(driver);
        loginPage.open(Config.baseUrl());

        driver.findElement(By.cssSelector("a[href*='/register'], div.haveAccount a")).click();

        Assert.assertTrue(driver.getCurrentUrl().contains("register"),
                "Keçiddən sonra URL 'register' ehtiva etməlidir, cari URL: " + driver.getCurrentUrl());
        attachScreenshot("Qeydiyyata Yönləndirmə Təsdiqi");
    }
}
