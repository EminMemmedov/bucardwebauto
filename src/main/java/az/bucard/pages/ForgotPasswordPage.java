package az.bucard.pages;

import io.qameta.allure.Step;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

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
            "span.errorAlert, div.errorAlert, p.errorAlert");
    private static final By PAGE_HEADING = By.cssSelector(
            "h1, h2, h3, .formTitle, .authTitle, div.title");

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
}
