package az.bucard.pages;

import io.qameta.allure.Step;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

public final class RegisterPage extends BasePage {
    private static final By EMAIL_FIELD = By.cssSelector(
            "input[name='email'], input[type='email'], input#email");
    private static final By PASSWORD_FIELD = By.cssSelector(
            "input#password, input[name='password']");
    private static final By SUBMIT_BUTTON = By.cssSelector(
            "button.formBtn, button[type='submit']");
    private static final By PASSWORD_EYE = By.cssSelector(
            "div.eye");
    private static final By LINKEDIN_BUTTON = By.cssSelector(
            "button.linkedin, button.btnWithLogo.linkedin");
    private static final By LOGIN_LINK = By.cssSelector(
            "a[href*='/login'], .haveAccount a");
    private static final By ERROR_ALERT = By.cssSelector(
            "span.errorAlert, div.errorAlert");

    public RegisterPage(WebDriver driver) {
        super(driver);
    }

    @Step("Qeydiyyat səhifəsini aç: {baseUrl}/register")
    public void open(String baseUrl) {
        String cleanBase = baseUrl.replaceAll("/+$", "");
        driver.get(cleanBase + "/register");
        visible(EMAIL_FIELD);
    }

    @Step("Qeydiyyat səhifəsinin yükləndiyini yoxla")
    public boolean isLoaded() {
        return isElementVisible(EMAIL_FIELD) && isElementVisible(SUBMIT_BUTTON);
    }

    @Step("E-poçt daxiletmə sahəsinin mövcudluğunu yoxla")
    public boolean hasEmailField() {
        return isElementVisible(EMAIL_FIELD);
    }

    @Step("Şifrə daxiletmə sahəsinin mövcudluğunu yoxla")
    public boolean hasPasswordField() {
        return isElementVisible(PASSWORD_FIELD);
    }

    @Step("Qeydiyyat düyməsinin mövcudluğunu yoxla")
    public boolean hasSubmitButton() {
        return isElementVisible(SUBMIT_BUTTON);
    }

    @Step("LinkedIn ilə qeydiyyat düyməsinin mövcudluğunu yoxla")
    public boolean hasLinkedInButton() {
        return isElementVisible(LINKEDIN_BUTTON);
    }

    @Step("Giriş səhifəsinə keçid ('Hesabınız var? Daxil olun') linkini yoxla")
    public boolean hasLoginLink() {
        return isElementVisible(LOGIN_LINK);
    }

    @Step("Giriş səhifəsinə keçid linkinə kliklə")
    public void clickLoginLink() {
        click(LOGIN_LINK);
    }

    @Step("E-poçt və şifrə daxil et")
    public void fillCredentials(String email, String password) {
        if (email != null) {
            clearAndType(EMAIL_FIELD, email);
        }
        if (password != null) {
            clearAndType(PASSWORD_FIELD, password);
        }
    }

    @Step("Şifrə görünməzlik göz ikonuna kliklə (eye toggle)")
    public void togglePasswordVisibility() {
        click(PASSWORD_EYE);
    }

    @Step("Şifrə daxiletmə sahəsinin tipini al (text və ya password)")
    public String getPasswordInputType() {
        WebElement pwd = visible(PASSWORD_FIELD);
        return pwd.getAttribute("type");
    }

    @Step("Qeydiyyat formasını göndər")
    public void submit() {
        click(SUBMIT_BUTTON);
    }

    @Step("Xəta bildirişinin (errorAlert) mövcudluğunu yoxla")
    public boolean hasErrorAlert() {
        return isElementVisible(ERROR_ALERT);
    }
}
