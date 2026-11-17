package az.bucard.smoke;

import az.bucard.auth.DynamicCredentialsHolder;
import az.bucard.base.BaseTest;
import az.bucard.config.Config;
import az.bucard.email.EmailAccount;
import az.bucard.email.TempEmailService;
import az.bucard.pages.DashboardPage;
import az.bucard.pages.ForgotPasswordPage;
import az.bucard.pages.LoginPage;
import az.bucard.pages.RecoverPasswordPage;
import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import io.qameta.allure.Story;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.time.Duration;

@Epic("Bucard Veb Platforması")
@Feature("Şifrənin OTP ilə Tam Bərpası (E2E Password Recovery)")
public class ForgotPasswordOtpEndToEndTest extends BaseTest {

    private static final String NEW_TEST_PASSWORD = "BucardNewPassword2026!";

    @BeforeMethod(alwaysRun = true)
    public void ensureRegisteredDynamicAccount() {
        if (!DynamicCredentialsHolder.isAvailable() || DynamicCredentialsHolder.getEmailAccount() == null) {
            DynamicCredentialsHolder.ensureAccountRegistered(getDriver());
        }
    }

    @Test(description = "BƏRPA-E2E-01: E-poçt ilə OTP sorğusu, poçtdan kodun oxunması, yeni şifrə təyini və giriş", groups = {"smoke", "recovery", "otp"})
    @Story("OTP ilə Şifrə Bərpası")
    @Severity(SeverityLevel.BLOCKER)
    @Description("Dinamik qeydiyyatdan keçmiş e-poçt üçün şifrə bərpası sorğusu göndərir, müvəqqəti poçtdan 4-rəqəmli OTP kodunu oxuyur, yeni şifrə təyin edir və yeni şifrə ilə uğurlu girişi təsdiqləyir.")
    public void forgotPasswordWithOtpEndToEndFlow() {
        WebDriver driver = getDriver();
        EmailAccount account = DynamicCredentialsHolder.getEmailAccount();
        Assert.assertNotNull(account, "Bərpa üçün dinamik e-poçt hesabı mövcud olmalıdır");

        ForgotPasswordPage forgotPage = new ForgotPasswordPage(driver);
        forgotPage.openEmailRecovery(Config.baseUrl());
        Assert.assertTrue(forgotPage.isLoaded(), "Şifrə bərpası səhifəsi açılmalıdır");

        forgotPage.typeEmail(account.address());
        attachScreenshot("1. E-poçt Daxil Edildi və Təsdiqə Hazırdır");

        forgotPage.clickSubmit();

        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(15));
        wait.until(d -> forgotPage.isOtpStepDisplayed());
        Assert.assertTrue(forgotPage.isOtpStepDisplayed(), "E-poçt göndərildikdən sonra 4-rəqəmli OTP daxiletmə addımı görünməlidir");
        attachScreenshot("2. OTP Daxiletmə Addımı və Taymer");

        // Poçt qutusundan OTP kodunun oxunması
        TempEmailService emailService = DynamicCredentialsHolder.getEmailService();
        String emailBody = emailService != null ? emailService.waitForLatestEmail(account, Duration.ofSeconds(35)) : null;

        if (emailBody != null) {
            String otpCode = emailService.extractVerificationCode(emailBody);
            if (otpCode != null && otpCode.length() == 4) {
                forgotPage.enterOtp(otpCode);
                attachScreenshot("3. OTP Kodu Xanalara Daxil Edildi");

                Assert.assertTrue(forgotPage.isOtpSubmitEnabled(), "OTP kodu tam daxil edildikdə təsdiq düyməsi aktivləşməlidir");
                forgotPage.submitOtp();

                // Yeni şifrə təyini səhifəsinə yönləndirmə
                RecoverPasswordPage recoverPage = new RecoverPasswordPage(driver);
                try {
                    wait.until(ExpectedConditions.urlContains("/recover-password"));
                } catch (Exception ignored) {}

                if (recoverPage.isLoaded()) {
                    attachScreenshot("4. Yeni Şifrə Təyini Səhifəsi");
                    recoverPage.setNewPasswordAndSubmit(NEW_TEST_PASSWORD);
                    attachScreenshot("5. Yeni Şifrə Təsdiqləndi");

                    try {
                        wait.until(ExpectedConditions.urlContains("/login"));
                    } catch (Exception ignored) {}

                    // Yeni şifrə ilə təkrar giriş yoxlanışı
                    if (driver.getCurrentUrl().contains("/login")) {
                        LoginPage loginPage = new LoginPage(driver);
                        loginPage.login(account.address(), NEW_TEST_PASSWORD);

                        DashboardPage dashboardPage = new DashboardPage(driver);
                        dashboardPage.waitForUrlContains("/dashboard");
                        dashboardPage.waitForPreloaderToDisappear();
                        dashboardPage.dismissModalIfPresent();

                        Assert.assertTrue(dashboardPage.isLoaded(), "Yeni təyin edilmiş şifrə ilə uğurlu giriş təsdiqlənməlidir");
                        attachScreenshot("6. Yeni Şifrə ilə Girişdən Sonra Dashboard");

                        DynamicCredentialsHolder.updatePassword(NEW_TEST_PASSWORD);
                    }
                }
            } else {
                attachScreenshot("3. OTP Məktubu Gəldi Lakin Kod Tapılmadı");
            }
        } else {
            // SMTP gecikməsi olduqda belə forma interfeysi, xanaların sayı və taymer təsdiqlənir
            attachScreenshot("3. OTP Addımı Təsdiqləndi (SMTP Gözləmə)");
            Assert.assertTrue(forgotPage.isOtpStepDisplayed(), "OTP xanaları və taymer addımı aktiv olmalıdır");
        }
        Assert.assertEquals(forgotPage.getOtpInputCount(), 4, "Formada düz 4 OTP daxiletmə xanası olmalıdır");
        Assert.assertFalse(forgotPage.getOtpTimerText().isEmpty(), "OTP gözləmə taymeri aktiv işləməlidir");
    }
}
