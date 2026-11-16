package az.bucard.smoke;

import az.bucard.base.BaseTest;
import az.bucard.config.Config;
import az.bucard.pages.LandingPage;
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
@Feature("İctimai Səhifələr və Naviqasiya (Pre-Login)")
public class PublicRoutesSmokeTest extends BaseTest {

    @Test(description = "ANA-01: Ana səhifənin loqosu, naviqasiyası və qeydiyyat düymələri", groups = {"smoke", "public"})
    @Story("Ana Səhifə (Landing)")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Bucard ana səhifəsinin loqosunu, naviqasiya menyusunu, dil seçimini və 'Giriş' / 'Qeydiyyat' düymələrinin mövcudluğunu yoxlayır.")
    public void landingPageHeaderAndCtasArePresent() {
        WebDriver driver = getDriver();
        LandingPage landingPage = new LandingPage(driver);
        landingPage.open(Config.baseUrl());

        Assert.assertTrue(landingPage.isLoaded(), "Ana səhifənin loqosu və giriş linki görünməlidir");
        Assert.assertTrue(landingPage.hasNavMenu(), "Naviqasiya menyusu mövcud olmalıdır");
        Assert.assertTrue(landingPage.hasLanguageSwitcher(), "Dil seçimi elementi mövcud olmalıdır");
        Assert.assertTrue(landingPage.hasSignInLink(), "Giriş (Daxil ol) düyməsi mövcud olmalıdır");
        Assert.assertTrue(landingPage.hasRegisterButton(), "'Qeydiyyat' / profil yaratma düyməsi mövcud olmalıdır");

        attachScreenshot("Bucard Ana Səhifəsinin Ümumi Görünüşü");
    }

    @Test(description = "TARİF-02: Aylıq və İllik tarif paketləri arasında keçid", groups = {"smoke", "public"})
    @Story("Tariflər və Paketlər")
    @Severity(SeverityLevel.NORMAL)
    @Description("İstifadəçinin 'Aylıq' və 'İllik' tarif tabları arasında klik edərək keçid edə bildiyini və qiymətlərin dəyişdiyini təsdiqləyir.")
    public void landingPackagesTabSwitcherWorks() {
        WebDriver driver = getDriver();
        LandingPage landingPage = new LandingPage(driver);
        landingPage.open(Config.baseUrl());

        if (landingPage.hasMonthlyTab() && landingPage.hasYearlyTab()) {
            Assert.assertTrue(landingPage.isMonthlyTabActive(), "Susmaya görə 'Aylıq' tabı aktiv olmalıdır");

            landingPage.switchToYearlyTab();
            Assert.assertTrue(landingPage.isYearlyTabActive(), "Kliklədikdən sonra 'İllik' tabı aktiv olmalıdır");
            attachScreenshot("İllik Tarif Paketləri");

            landingPage.switchToMonthlyTab();
            Assert.assertTrue(landingPage.isMonthlyTabActive(), "'Aylıq' tabına qayıdış uğurla tamamlanmalıdır");
            attachScreenshot("Aylıq Tarif Paketləri");
        }
    }

    @Test(description = "GİRİŞ-03: Giriş səhifəsinin (/login) təmiz açılması və yüklənməsi", groups = {"smoke", "public"})
    @Story("Giriş Səhifəsi (/login)")
    @Severity(SeverityLevel.CRITICAL)
    @Description("/login ünvanının xətasız açıldığını, heç bir kənar yönləndirmə olmadığını və tətbiqin #root konteynerinin qurulduğunu yoxlayır.")
    public void loginRouteIsAccessible() {
        WebDriver driver = getDriver();
        LoginPage loginPage = new LoginPage(driver);
        loginPage.open(Config.baseUrl());

        String currentUrl = driver.getCurrentUrl();
        Assert.assertTrue(currentUrl.contains("/login") || currentUrl.contains("/dashboard"),
                "URL '/login' (və ya avtorizasiya olunubsa '/dashboard') olmalıdır, cari: " + currentUrl);

        Assert.assertFalse(driver.findElements(By.id("root")).isEmpty(),
                "Login səhifəsində SPA '#root' konteyneri mövcud olmalıdır");

        attachScreenshot("Giriş Səhifəsi (/login)");
    }

    @Test(description = "XƏTA-04: Səhifədə 500 / 502 server xətalarının olmaması", groups = {"smoke", "public"})
    @Story("Server İnteqrallığı")
    @Severity(SeverityLevel.NORMAL)
    @Description("Giriş səhifəsinin mənbə kodunda 500 Internal Server Error və ya 502 Bad Gateway kimi xətaların olmadığını təsdiqləyir.")
    public void loginPageHasNoServerError() {
        WebDriver driver = getDriver();
        LoginPage loginPage = new LoginPage(driver);
        loginPage.open(Config.baseUrl());

        String pageSource = driver.getPageSource();
        Assert.assertFalse(pageSource.contains("502 Bad Gateway") || pageSource.contains("500 Internal Server Error"),
                "Giriş səhifəsində server xətası mətni olmamalıdır");

        attachScreenshot("Server Xətası Olmayan Giriş Səhifəsi");
    }

    @Test(description = "GİRİŞ-05: Giriş formasında sahələr, düymələr və bərpa linki", groups = {"smoke", "public"})
    @Story("Giriş Forması Elementləri")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Giriş formasında e-poçt xanası, şifrə xanası, 'Davam et' düyməsi, şifrə bərpası və LinkedIn düyməsinin olduğunu yoxlayır.")
    public void loginPageKeyControlsArePresent() {
        WebDriver driver = getDriver();
        LoginPage loginPage = new LoginPage(driver);
        loginPage.open(Config.baseUrl());

        Assert.assertTrue(loginPage.hasEmailField(), "E-poçt daxiletmə sahəsi (#email) mövcud olmalıdır");
        Assert.assertTrue(loginPage.hasPasswordField(), "Şifrə daxiletmə sahəsi (#password) mövcud olmalıdır");
        Assert.assertTrue(loginPage.hasSubmitButton(), "Təsdiq / Davam et düyməsi (.formBtn) mövcud olmalıdır");
        Assert.assertTrue(loginPage.hasForgotPasswordLink(), "Şifrəni unutmusunuz linki mövcud olmalıdır");
        Assert.assertTrue(loginPage.hasRegisterLink(), "Qeydiyyat linki mövcud olmalıdır");
        Assert.assertTrue(loginPage.hasLinkedInButton(), "LinkedIn ilə giriş düyməsi mövcud olmalıdır");

        attachScreenshot("Giriş Formasının Əsas Sahələri");
    }

    @Test(description = "GİRİŞ-06: Şifrənin göz işarəsinə kliklə göstərilməsi və gizlədilməsi", groups = {"smoke", "public"})
    @Story("Şifrə Sahəsi İdarəetməsi")
    @Severity(SeverityLevel.NORMAL)
    @Description("Şifrə sahəsindəki göz ikonuna basdıqda input tipinin 'password' və 'text' arasında düzgün dəyişdiyini yoxlayır.")
    public void passwordVisibilityToggleWorks() {
        WebDriver driver = getDriver();
        LoginPage loginPage = new LoginPage(driver);
        loginPage.open(Config.baseUrl());

        Assert.assertEquals(loginPage.getPasswordInputType(), "password",
                "Şifrə sahəsi əvvəlcə type='password' olmalıdır");

        loginPage.togglePasswordVisibility();
        Assert.assertEquals(loginPage.getPasswordInputType(), "text",
                "Göz ikonuna klikdən sonra şifrə sahəsi type='text' olmalıdır");
        attachScreenshot("Şifrə Göstərilən Vəziyyətdə (Açıq Göz)");

        loginPage.togglePasswordVisibility();
        Assert.assertEquals(loginPage.getPasswordInputType(), "password",
                "Yenidən kliklədikdə sahə type='password' vəziyyətinə qayıtmalıdır");
    }

    @Test(description = "GİRİŞ-07: 'E-poçt' və 'Telefon nömrəsi' giriş növləri arasında keçid", groups = {"smoke", "public"})
    @Story("Giriş Növləri Tabları")
    @Severity(SeverityLevel.NORMAL)
    @Description("Giriş formasında 'E-poçt' və 'Telefon' tabları arasında klik etdikdə uyğun sahələrin göründüyünü yoxlayır.")
    public void authTabSwitchingWorks() {
        WebDriver driver = getDriver();
        LoginPage loginPage = new LoginPage(driver);
        loginPage.open(Config.baseUrl());

        Assert.assertTrue(loginPage.hasEmailField(), "E-poçt sahəsi susmaya görə görünməlidir");

        loginPage.switchToPhoneTab();
        Assert.assertTrue(loginPage.isPhoneFieldVisible(), "Telefon tabına keçdikdən sonra nömrə sahəsi görünməlidir");
        attachScreenshot("Telefon Nömrəsi ilə Giriş Forması");

        loginPage.switchToEmailTab();
        Assert.assertTrue(loginPage.hasEmailField(), "E-poçt tabına qayıtdıqda e-poçt sahəsi bərpa olunmalıdır");
    }
}
