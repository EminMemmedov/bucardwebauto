package az.bucard.pages;

import io.qameta.allure.Step;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

public final class LoginPage extends BasePage {
    private static final By LOGIN_HEADING = By.xpath(
            "//*[self::h1 or self::h2 or self::h3][contains(normalize-space(), 'Login') or contains(normalize-space(), 'Daxil ol') or contains(normalize-space(), 'Sign in')]");
    private static final By EMAIL_FIELD = By.cssSelector(
            "input#email, input[name='email'], input[type='email'], input.formInput");
    private static final By PASSWORD_FIELD = By.cssSelector(
            "input#password, input[name='password']");
    private static final By SUBMIT_BUTTON = By.cssSelector(
            "button.formBtn, button[type='submit'], button[class*='login']");
    private static final By PASSWORD_TOGGLE = By.cssSelector(
            "div.eye");
    private static final By EMAIL_TAB = By.xpath(
            "//div[contains(@class, 'tabList')]//p[1]");
    private static final By PHONE_TAB = By.xpath(
            "//div[contains(@class, 'tabList')]//p[2]");
    private static final By PHONE_FIELD = By.cssSelector(
            "input[type='tel'], input.react-international-phone-input");
    private static final By LINKEDIN_BUTTON = By.cssSelector(
            "button.linkedin, button.btnWithLogo.linkedin");
    private static final By GOOGLE_IFRAME = By.cssSelector(
            "iframe[src*='accounts.google.com/gsi']");
    private static final By FORGOT_PASSWORD_LINK = By.cssSelector(
            "div.forgot a, a[href*='forgot-password'], a[href*='recover']");
    private static final By REGISTER_LINK = By.cssSelector(
            "a[href*='/register']");
    private static final By ERROR_MESSAGE = By.cssSelector(
            ".alert-danger, .error-message, div[role='alert'], span.error, span.errorAlert, div.errorAlert, .Toastify__toast, div[class*='toast'], div[class*='alert'], p.errorAlert");

    public LoginPage(WebDriver driver) {
        super(driver);
    }

    @Step("Open login page at {baseUrl}/login")
    public void open(String baseUrl) {
        driver.get(baseUrl.replaceAll("/+$", "") + "/login");
        visible(EMAIL_FIELD);
    }

    public boolean isLoaded() {
        return isElementVisible(EMAIL_FIELD) || isElementVisible(SUBMIT_BUTTON);
    }

    public boolean hasEmailField() {
        return isElementVisible(EMAIL_FIELD);
    }

    public boolean hasPasswordField() {
        return isElementVisible(PASSWORD_FIELD);
    }

    public boolean hasSubmitButton() {
        return isElementVisible(SUBMIT_BUTTON);
    }

    public boolean hasRegisterLink() {
        return isElementVisible(REGISTER_LINK);
    }

    public boolean hasForgotPasswordLink() {
        return isElementVisible(FORGOT_PASSWORD_LINK);
    }

    public boolean hasLinkedInButton() {
        return isElementVisible(LINKEDIN_BUTTON);
    }

    public boolean hasGoogleButton() {
        return isElementPresent(GOOGLE_IFRAME);
    }

    @Step("Enter email: {email}")
    public void enterCredentials(String email, String password) {
        if (email != null) {
            clearAndType(EMAIL_FIELD, email);
        }
        if (password != null) {
            clearAndType(PASSWORD_FIELD, password);
        }
    }

    @Step("Log in with phone: {rawPhone}")
    public void loginWithPhone(String rawPhone, String password) {
        switchToPhoneTab();
        WebElement phoneInput = visible(PHONE_FIELD);
        String digits = sanitizePhoneDigits(rawPhone);
        phoneInput.sendKeys(digits);
        if (password != null) {
            clearAndType(PASSWORD_FIELD, password);
        }
        submit();
    }

    @Step("Log in with user/phone: {usernameOrPhone}")
    public void login(String usernameOrPhone, String password) {
        if (usernameOrPhone == null) {
            throw new IllegalArgumentException("Username/phone cannot be null");
        }
        if (usernameOrPhone.contains("@")) {
            switchToEmailTab();
            enterCredentials(usernameOrPhone, password);
            submit();
        } else {
            loginWithPhone(usernameOrPhone, password);
        }
    }

    @Step("Submit login form")
    public void submit() {
        click(SUBMIT_BUTTON);
    }

    @Step("Toggle password visibility eye button")
    public void togglePasswordVisibility() {
        click(PASSWORD_TOGGLE);
    }

    public String getPasswordInputType() {
        WebElement passwordElement = visible(PASSWORD_FIELD);
        return passwordElement.getAttribute("type");
    }

    @Step("Switch to Phone tab")
    public void switchToPhoneTab() {
        click(PHONE_TAB);
        visible(PHONE_FIELD);
    }

    @Step("Switch to Email tab")
    public void switchToEmailTab() {
        click(EMAIL_TAB);
        visible(EMAIL_FIELD);
    }

    public boolean isPhoneFieldVisible() {
        return isElementVisible(PHONE_FIELD);
    }

    public boolean hasErrorMessage() {
        return isElementPresent(ERROR_MESSAGE);
    }

    public boolean isSubmitButtonDisabled() {
        if (!isElementPresent(SUBMIT_BUTTON)) {
            return false;
        }
        WebElement btn = driver.findElement(SUBMIT_BUTTON);
        String disabled = btn.getAttribute("disabled");
        String clazz = btn.getAttribute("class");
        return disabled != null || (clazz != null && clazz.contains("disabled"));
    }

    public String getEmailFieldValue() {
        if (!isElementPresent(EMAIL_FIELD)) {
            return "";
        }
        return driver.findElement(EMAIL_FIELD).getAttribute("value");
    }

    public String getErrorMessage() {
        return getTextSafe(ERROR_MESSAGE);
    }

    private static String sanitizePhoneDigits(String phone) {
        if (phone == null) {
            return "";
        }
        String clean = phone.replaceAll("[^0-9]", "");
        if (clean.startsWith("994")) {
            clean = clean.substring(3);
        }
        if (clean.startsWith("0")) {
            clean = clean.substring(1);
        }
        return clean;
    }
}
