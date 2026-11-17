package az.bucard.pages;

import io.qameta.allure.Step;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

public final class RecoverPasswordPage extends BasePage {
    private static final By RECOVER_FORM = By.cssSelector(
            "form.recoverForm, form[class*='recover']");
    private static final By NEW_PASSWORD_FIELD = By.cssSelector(
            "form.recoverForm input[type='password'], form.recoverForm input[name='password'], input[type='password']");
    private static final By SUBMIT_BUTTON = By.cssSelector(
            "form.recoverForm button[type='submit'], form.recoverForm button.formBtn, button[type='submit']");
    private static final By PASSWORD_EYE = By.cssSelector(
            "form.recoverForm .eye, form.recoverForm svg, .eye");
    private static final By ERROR_ALERT = By.cssSelector(
            "span.errorAlert, div.errorAlert, p.errorAlert, .alert, .Toastify__toast");
    private static final By SUCCESS_ALERT = By.cssSelector(
            ".Toastify__toast--success, div[class*='success'], .alert-success");

    public RecoverPasswordPage(WebDriver driver) {
        super(driver);
    }

    @Step("Yeni şifrə təyini səhifəsinin (/recover-password) yükləndiyini yoxla")
    public boolean isLoaded() {
        return isElementVisible(NEW_PASSWORD_FIELD) || driver.getCurrentUrl().contains("/recover-password");
    }

    @Step("Yeni şifrə sahəsinin mövcudluğunu yoxla")
    public boolean hasNewPasswordField() {
        return isElementVisible(NEW_PASSWORD_FIELD);
    }

    @Step("Yeni şifrə daxil et: '{newPassword}'")
    public void enterNewPassword(String newPassword) {
        clearAndType(NEW_PASSWORD_FIELD, newPassword);
    }

    @Step("Şifrə görünməzlik göz ikonuna kliklə (eye toggle)")
    public void togglePasswordVisibility() {
        if (isElementPresent(PASSWORD_EYE)) {
            click(PASSWORD_EYE);
        }
    }

    @Step("Yeni şifrəni təsdiqlə və göndər")
    public void submit() {
        click(SUBMIT_BUTTON);
    }

    @Step("Yeni şifrəni təyin et və göndər: '{newPassword}'")
    public void setNewPasswordAndSubmit(String newPassword) {
        enterNewPassword(newPassword);
        submit();
    }

    @Step("Xəta bildirişinin (errorAlert) mövcudluğunu yoxla")
    public boolean hasError() {
        return isElementVisible(ERROR_ALERT);
    }

    @Step("Xəta bildirişinin mətnini oxu")
    public String getErrorMessage() {
        if (!isElementVisible(ERROR_ALERT)) {
            return "";
        }
        return driver.findElement(ERROR_ALERT).getText().trim();
    }

    @Step("Uğurlu bildirişin (successToast) mövcudluğunu yoxla")
    public boolean hasSuccessMessage() {
        return isElementVisible(SUCCESS_ALERT);
    }
}
