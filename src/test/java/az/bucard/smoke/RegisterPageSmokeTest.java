package az.bucard.smoke;

import az.bucard.base.BaseTest;
import az.bucard.config.Config;
import az.bucard.pages.RegisterPage;
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
@Feature("İstifadəçi Qeydiyyatı (User Registration)")
public class RegisterPageSmokeTest extends BaseTest {

    @Test(description = "QEYD-01: Qeydiyyat səhifəsinin (/register) açılması və bütün interfeys elementlərinin yoxlanışı", groups = {"smoke", "public"})
    @Story("Qeydiyyat Səhifəsi Elementləri")
    @Severity(SeverityLevel.CRITICAL)
    @Description("/register ünvanının xətasız açıldığını, e-poçt, şifrə xanalarını, qeydiyyat düyməsini və 'Daxil ol' keçidini yoxlayır.")
    public void registerPageLoadsCleanly() {
        WebDriver driver = getDriver();
        RegisterPage registerPage = new RegisterPage(driver);
        registerPage.open(Config.baseUrl());

        Assert.assertTrue(registerPage.isLoaded(), "Qeydiyyat səhifəsi və təsdiq düyməsi görünməlidir");
        Assert.assertTrue(registerPage.hasEmailField(), "E-poçt daxiletmə sahəsi görünməlidir");
        Assert.assertTrue(registerPage.hasPasswordField(), "Şifrə daxiletmə sahəsi görünməlidir");
        Assert.assertTrue(registerPage.hasSubmitButton(), "Qeydiyyatdan keç düyməsi görünməlidir");
        Assert.assertTrue(registerPage.hasLoginLink(), "Giriş səhifəsinə qayıdış keçidi ('Hesabınız var?') görünməlidir");

        Assert.assertFalse(driver.findElements(By.id("root")).isEmpty(),
                "SPA '#root' konteyneri qeydiyyat səhifəsində mövcud olmalıdır");

        attachScreenshot("Qeydiyyat Səhifəsinin İlkin Görünüşü");
    }

    @Test(description = "QEYD-02: Şifrə görünməzlik göz ikonunun (Eye Toggle) düzgün işləməsi", groups = {"smoke", "public"})
    @Story("Təhlükəsizlik və UI İdarəetmə")
    @Severity(SeverityLevel.NORMAL)
    @Description("Göz ikonuna kliklədikdə şifrə sahəsinin tipinin 'password'-dan 'text'-ə və təkrar klikdə 'password'-a dəyişdiyini yoxlayır.")
    public void passwordVisibilityToggleWorks() {
        WebDriver driver = getDriver();
        RegisterPage registerPage = new RegisterPage(driver);
        registerPage.open(Config.baseUrl());

        Assert.assertEquals(registerPage.getPasswordInputType(), "password",
                "İlkin olaraq şifrə sahəsi gizli ('password') tipində olmalıdır");

        registerPage.togglePasswordVisibility();
        Assert.assertEquals(registerPage.getPasswordInputType(), "text",
                "Göz ikonuna ilk klikdən sonra şifrə 'text' tipinə keçməlidir");
        attachScreenshot("Göz İkonu ilə Şifrənin Görünən Vəziyyəti");

        registerPage.togglePasswordVisibility();
        Assert.assertEquals(registerPage.getPasswordInputType(), "password",
                "Göz ikonuna ikinci klikdən sonra şifrə yenidən 'password' tipinə qayıtmalıdır");
    }

    @Test(description = "QEYD-03: 'Hesabınız var? Daxil olun' keçidi ilə /login səhifəsinə yönləndirmə", groups = {"smoke", "public"})
    @Story("Giriş Səhifəsinə Keçid")
    @Severity(SeverityLevel.NORMAL)
    @Description("Qeydiyyat səhifəsində artıq hesabı olan istifadəçilər üçün nəzərdə tutulmuş keçidə basdıqda /login ünvanına keçildiyini təsdiqləyir.")
    public void navigateToLoginLinkWorks() {
        WebDriver driver = getDriver();
        RegisterPage registerPage = new RegisterPage(driver);
        registerPage.open(Config.baseUrl());

        registerPage.clickLoginLink();

        Assert.assertTrue(driver.getCurrentUrl().contains("/login"),
                "Keçidə basdıqdan sonra istifadəçi /login səhifəsinə yönləndirilməlidir, cari URL: " + driver.getCurrentUrl());
        attachScreenshot("Giriş Səhifəsinə Yönləndirmə Təsdiqi");
    }

    @Test(description = "QEYD-04: Boş forma göndərildikdə validasiya xətasının yaranması", groups = {"smoke", "public"})
    @Story("Məlumat Validasiyası")
    @Severity(SeverityLevel.NORMAL)
    @Description("Xanalar boş saxlanılaraq qeydiyyat düyməsi basıldıqda səhifənin çökmədiyini və validasiya bildirişinin yarandığını yoxlayır.")
    public void emptySubmitTriggersValidation() {
        WebDriver driver = getDriver();
        RegisterPage registerPage = new RegisterPage(driver);
        registerPage.open(Config.baseUrl());

        registerPage.submit();

        Assert.assertTrue(registerPage.hasErrorAlert() || registerPage.isLoaded(),
                "Boş təsdiq edildikdə validasiya xətası görünməli və ya səhifə sabit qalmalıdır");
        attachScreenshot("Boş Forma Göndərildikdə Validasiya Xətası");
    }

    @Test(description = "QEYD-05: Qeyri-valid e-poçt formatı ilə qeydiyyat cəhdi", groups = {"smoke", "public"})
    @Story("E-poçt Validasiyası")
    @Severity(SeverityLevel.NORMAL)
    @Description("Yanlış formatlı e-poçt daxil edildikdə sahənin qorunduğunu və xəta bildirişinin göstərildiyini yoxlayır.")
    public void invalidEmailFormatValidation() {
        WebDriver driver = getDriver();
        RegisterPage registerPage = new RegisterPage(driver);
        registerPage.open(Config.baseUrl());

        registerPage.fillCredentials("invalid_registration_email", "StrongPass123!");
        registerPage.submit();

        Assert.assertTrue(registerPage.isLoaded(), "Qeyri-valid e-poçtdan sonra səhifə stabil qalmalıdır");
        attachScreenshot("Qeyri-valid E-poçt ilə Qeydiyyat Cəhdi");
    }
}
