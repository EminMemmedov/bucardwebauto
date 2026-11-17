package az.bucard.pages;

import io.qameta.allure.Step;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.util.List;

public final class ForgotPasswordPage extends BasePage {
    private static final By EMAIL_FIELD = By.cssSelector(
            "input#email, input[name='email'], input[type='email'], input.formInput");
    private static final By PHONE_FIELD = By.cssSelector(
            "input[type='tel'], input.react-international-phone-input");
    private static final By SUBMIT_BUTTON = By.cssSelector(
            "button.formBtn, button[type='submit']");
    private static final By BACK_NAVIGATE = By.cssSelector(
            ".formHeader .navigate, div.navigate, img[alt='go-back']");
    private static final By ERROR_ALERT = By.cssSelector(
            "span.errorAlert, div.errorAlert, p.errorAlert, .alert, .Toastify__toast");
    private static final By PAGE_HEADING = By.cssSelector(
            "h1, h2, h3, .formTitle, .authTitle, div.title");

    // Step 2 (OTP) locators
    private static final By OTP_INPUTS = By.cssSelector(
            "form.registerForm input[name='otp'], form.registerForm input[type='tel'], .otpForm .inputArea input, .inputBox input[type='tel'], input[inputmode='numeric']");
    private static final By OTP_SUBMIT_BUTTON = By.cssSelector(
            "#submit-button, form.registerForm button[type='submit'], form.registerForm button.formBtn, button.formBtn");
    private static final By OTP_TIMER = By.cssSelector(
            "span.timer, p.resendTimerInfo, .time");
    private static final By RESEND_LINK = By.cssSelector(
            "p.reSend, p.details.try");

    public ForgotPasswordPage(WebDriver driver) {
        super(driver);
    }

    @Step("Şifrə bərpası səhifəsini aç ({recoveryType}): {baseUrl}/forgot-password/{recoveryType}")
    public void open(String baseUrl, String recoveryType) {
        String cleanBase = baseUrl.replaceAll("/+$", "");
        driver.get(cleanBase + "/forgot-password/" + recoveryType);
        visible(SUBMIT_BUTTON);
    }

    public void openEmailRecovery(String baseUrl) {
        open(baseUrl, "email");
    }

    public void openPhoneRecovery(String baseUrl) {
        open(baseUrl, "phone");
    }

    @Step("Şifrə bərpası səhifəsinin yükləndiyini yoxla")
    public boolean isLoaded() {
        return isElementVisible(SUBMIT_BUTTON) && (isElementVisible(EMAIL_FIELD) || isElementVisible(PHONE_FIELD));
    }

    @Step("E-poçt daxiletmə sahəsinin mövcudluğunu yoxla")
    public boolean hasEmailField() {
        return !driver.findElements(EMAIL_FIELD).isEmpty();
    }

    @Step("Telefon daxiletmə sahəsinin mövcudluğunu yoxla")
    public boolean hasPhoneField() {
        return !driver.findElements(PHONE_FIELD).isEmpty();
    }

    @Step("Təsdiq / Göndər düyməsinin mövcudluğunu yoxla")
    public boolean hasSubmitButton() {
        return isElementVisible(SUBMIT_BUTTON);
    }

    @Step("Təsdiq düyməsinin aktivlik vəziyyətini yoxla (enabled/disabled)")
    public boolean isSubmitButtonEnabled() {
        if (!isElementPresent(SUBMIT_BUTTON)) {
            return false;
        }
        WebElement btn = driver.findElement(SUBMIT_BUTTON);
        String disabledAttr = btn.getAttribute("disabled");
        String classAttr = btn.getAttribute("class");
        return disabledAttr == null && (classAttr == null || !classAttr.contains("disabled"));
    }

    @Step("Geri qayıt düyməsinin (navigate) mövcudluğunu yoxla")
    public boolean hasNavigateBack() {
        return isElementVisible(BACK_NAVIGATE);
    }

    @Step("Geri qayıt düyməsinə kliklə")
    public void clickNavigateBack() {
        click(BACK_NAVIGATE);
    }

    @Step("E-poçt sahəsinə mətn daxil et: {email}")
    public void typeEmail(String email) {
        type(EMAIL_FIELD, email);
    }

    @Step("E-poçt sahəsini təmizlə")
    public void clearEmail() {
        WebElement el = driver.findElement(EMAIL_FIELD);
        el.clear();
    }

    @Step("Təsdiq / Göndər düyməsinə kliklə")
    public void clickSubmit() {
        click(SUBMIT_BUTTON);
    }

    @Step("Xəta bildirişinin (errorAlert) mövcudluğunu yoxla")
    public boolean hasErrorAlert() {
        return isElementVisible(ERROR_ALERT);
    }

    @Step("Xəta bildirişinin mətnini oxu")
    public String getErrorAlertText() {
        if (!isElementVisible(ERROR_ALERT)) {
            return "";
        }
        return driver.findElement(ERROR_ALERT).getText().trim();
    }

    // --- OTP Step 2 Methods ---

    @Step("OTP daxiletmə addımının (Step 2) aktiv olduğunu yoxla")
    public boolean isOtpStepDisplayed() {
        return !driver.findElements(OTP_INPUTS).isEmpty();
    }

    @Step("OTP xanalarının sayını al")
    public int getOtpInputCount() {
        return driver.findElements(OTP_INPUTS).size();
    }

    @Step("4-rəqəmli OTP kodunu daxil et: '{otpCode}'")
    public void enterOtp(String otpCode) {
        if (otpCode == null || otpCode.isBlank()) {
            return;
        }
        List<WebElement> inputs = driver.findElements(OTP_INPUTS);
        int len = Math.min(otpCode.length(), inputs.size());
        for (int i = 0; i < len; i++) {
            WebElement input = inputs.get(i);
            input.clear();
            input.sendKeys(String.valueOf(otpCode.charAt(i)));
        }
    }

    @Step("OTP xanalarını təmizlə")
    public void clearOtp() {
        List<WebElement> inputs = driver.findElements(OTP_INPUTS);
        for (WebElement input : inputs) {
            input.clear();
        }
    }

    @Step("OTP təsdiq düyməsinin aktivlik vəziyyətini yoxla")
    public boolean isOtpSubmitEnabled() {
        if (!isElementPresent(OTP_SUBMIT_BUTTON)) {
            return false;
        }
        WebElement btn = driver.findElement(OTP_SUBMIT_BUTTON);
        String disabledAttr = btn.getAttribute("disabled");
        String classAttr = btn.getAttribute("class");
        return disabledAttr == null && (classAttr == null || !classAttr.contains("disabled"));
    }

    @Step("OTP təsdiq düyməsinə kliklə")
    public void submitOtp() {
        click(OTP_SUBMIT_BUTTON);
    }

    @Step("OTP taymerinin mətnini oxu")
    public String getOtpTimerText() {
        if (!isElementVisible(OTP_TIMER)) {
            return "";
        }
        return driver.findElement(OTP_TIMER).getText().trim();
    }

    @Step("Yenidən göndər (Resend) linkinin mövcudluğunu yoxla")
    public boolean hasResendLink() {
        return isElementVisible(RESEND_LINK);
    }
}
