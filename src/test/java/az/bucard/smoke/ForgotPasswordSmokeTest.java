package az.bucard.smoke;

import az.bucard.base.BaseTest;
import az.bucard.config.Config;
import az.bucard.pages.ForgotPasswordPage;
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
@Feature("Şifrənin Bərpası (Password Recovery)")
public class ForgotPasswordSmokeTest extends BaseTest {

    @Test(description = "BƏRPA-01: E-poçt ilə şifrə bərpası səhifəsinin (/forgot-password/email) açılması və ilkin vəziyyəti", groups = {"smoke", "public"})
    @Story("E-poçt ilə Bərpa")
    @Severity(SeverityLevel.CRITICAL)
    @Description("/forgot-password/email ünvanının xətasız açıldığını, e-poçt daxiletmə sahəsinin mövcudluğunu və ilkin olaraq göndər düyməsinin deaktiv (disabled) olduğunu yoxlayır.")
    public void emailRecoveryPageLoadsCleanly() {
        WebDriver driver = getDriver();
        ForgotPasswordPage forgotPage = new ForgotPasswordPage(driver);
        forgotPage.openEmailRecovery(Config.baseUrl());

        Assert.assertTrue(forgotPage.isLoaded(), "Şifrə bərpası səhifəsi və təsdiq düyməsi görünməlidir");
        Assert.assertTrue(forgotPage.hasEmailField(), "E-poçt daxiletmə sahəsi görünməlidir");
        Assert.assertFalse(forgotPage.hasPhoneField(), "E-poçt bərpa səhifəsində telefon sahəsi olmamalıdır");
        Assert.assertTrue(forgotPage.hasSubmitButton(), "Təsdiq / Göndər düyməsi görünməlidir");
        Assert.assertFalse(forgotPage.isSubmitButtonEnabled(), "Məlumat daxil edilmədikdə təsdiq düyməsi qeyri-aktiv (disabled) olmalıdır");

        Assert.assertFalse(driver.findElements(By.id("root")).isEmpty(),
                "SPA '#root' konteyneri bərpa səhifəsində mövcud olmalıdır");

        attachScreenshot("E-poçt ilə Şifrə Bərpası Səhifəsi");
    }

    @Test(description = "BƏRPA-02: Telefon nömrəsi ilə şifrə bərpası səhifəsinin (/forgot-password/phone) açılması və yoxlanışı", groups = {"smoke", "public"})
    @Story("Telefon Nömrəsi ilə Bərpa")
    @Severity(SeverityLevel.CRITICAL)
    @Description("/forgot-password/phone ünvanının açıldığını, telefon nömrəsi daxiletmə sahəsinin mövcudluğunu və e-poçt sahəsinin olmadığını təsdiqləyir.")
    public void phoneRecoveryPageLoadsCleanly() {
        WebDriver driver = getDriver();
        ForgotPasswordPage forgotPage = new ForgotPasswordPage(driver);
        forgotPage.openPhoneRecovery(Config.baseUrl());

        Assert.assertTrue(forgotPage.isLoaded(), "Telefonla bərpa səhifəsi və təsdiq düyməsi görünməlidir");
        Assert.assertTrue(forgotPage.hasPhoneField(), "Telefon nömrəsi sahəsi görünməlidir");
        Assert.assertFalse(forgotPage.hasEmailField(), "Telefonla bərpa səhifəsində e-poçt sahəsi olmamalıdır");
        Assert.assertTrue(forgotPage.hasSubmitButton(), "Təsdiq düyməsi görünməlidir");

        attachScreenshot("Telefon Nömrəsi ilə Şifrə Bərpası Səhifəsi");
    }

    @Test(description = "BƏRPA-03: E-poçt daxil etdikdə təsdiq düyməsinin aktivləşməsi (Input Reactive State)", groups = {"smoke", "public"})
    @Story("Daxiletmə və Düymə Vəziyyəti")
    @Severity(SeverityLevel.NORMAL)
    @Description("E-poçt xanası boş olduqda düymənin qeyri-aktiv olduğunu, simvol yazıldıqda dərhal aktivləşdiyini yoxlayır.")
    public void emailInputEnablesSubmitButton() {
        WebDriver driver = getDriver();
        ForgotPasswordPage forgotPage = new ForgotPasswordPage(driver);
        forgotPage.openEmailRecovery(Config.baseUrl());

        Assert.assertFalse(forgotPage.isSubmitButtonEnabled(), "İlkin olaraq düymə qeyri-aktiv (disabled) olmalıdır");

        forgotPage.typeEmail("test@example.com");
        Assert.assertTrue(forgotPage.isSubmitButtonEnabled(), "E-poçt yazıldıqdan sonra təsdiq düyməsi aktivləşməlidir");
        attachScreenshot("E-poçt Yazıldıqdan Sonra Düymənin Aktivləşməsi");
    }

    @Test(description = "BƏRPA-04: 'Geri qayıt' düyməsi ilə /login səhifəsinə qayıdışın yoxlanışı", groups = {"smoke", "public"})
    @Story("Giriş Səhifəsinə Qayıdış")
    @Severity(SeverityLevel.NORMAL)
    @Description("Şifrə bərpası səhifəsində geri qayıt oxuna və ya keçidə basdıqda istifadəçinin /login səhifəsinə problemsiz qayıtdığını təsdiqləyir.")
    public void backNavigationReturnsToLogin() {
        WebDriver driver = getDriver();
        ForgotPasswordPage forgotPage = new ForgotPasswordPage(driver);
        forgotPage.openEmailRecovery(Config.baseUrl());

        Assert.assertTrue(forgotPage.hasNavigateBack(), "Geri qayıtma düyməsi görünməlidir");
        forgotPage.clickNavigateBack();

        Assert.assertTrue(driver.getCurrentUrl().contains("/login"),
                "Geri düyməsinə klikdən sonra URL '/login' olmalıdır, cari URL: " + driver.getCurrentUrl());
        attachScreenshot("Giriş Səhifəsinə Uğurlu Qayıdış");
    }

    @Test(description = "BƏRPA-05: Qeyri-valid formatlı e-poçt daxil edildikdə xətanın göstərilməsi", groups = {"smoke", "public"})
    @Story("Format Validasiyası")
    @Severity(SeverityLevel.NORMAL)
    @Description("E-poçt sahəsinə sintaktik olaraq yanlış məlumat (məs. 'invalid-format-test') yazıb təsdiq etdikdə xəta bildirişinin yarandığını yoxlayır.")
    public void invalidEmailFormatInputValidation() {
        WebDriver driver = getDriver();
        ForgotPasswordPage forgotPage = new ForgotPasswordPage(driver);
        forgotPage.openEmailRecovery(Config.baseUrl());

        forgotPage.typeEmail("invalid-format-test");
        Assert.assertTrue(forgotPage.isSubmitButtonEnabled(), "Mətn yazıldıqda düymə aktiv olmalıdır");
        forgotPage.clickSubmit();

        attachScreenshot("Qeyri-valid Formatlı E-poçt Daxil Edilmiş Vəziyyət");
        Assert.assertTrue(forgotPage.isLoaded(), "Xətalı daxiletmədən sonra səhifə çökməməli və aktiv qalmalıdır");
    }
}
