package az.bucard.pages;

import io.qameta.allure.Step;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;

public final class LandingPage extends BasePage {
    private static final By LOGO = By.cssSelector(
            "div.logo, a[href='/'] img, img[alt='bucard-logo']");
    private static final By NAV_BAR = By.cssSelector(
            "nav.navBar, ul.navList");
    private static final By NAV_ITEMS = By.cssSelector(
            "li.navItem");
    private static final By LANGUAGE_SWITCHER = By.cssSelector(
            ".selectLang");
    private static final By CURRENT_LANGUAGE_LABEL = By.cssSelector(
            ".selectLang .text p");
    private static final By LANG_BOX = By.cssSelector(
            ".selectLang ul.langBox");
    private static final By AZ_LANG_ITEM = By.cssSelector(
            ".selectLang ul.langBox li#az-AZ, .selectLang li[id*='az']");
    private static final By EN_LANG_ITEM = By.cssSelector(
            ".selectLang ul.langBox li#en-US, .selectLang li[id*='en']");
    private static final By RU_LANG_ITEM = By.cssSelector(
            ".selectLang ul.langBox li#ru-RU, .selectLang li[id*='ru']");
    private static final By SIGN_IN_LINK = By.cssSelector(
            "a.signIn, a[href*='/login']");
    private static final By REGISTER_BUTTON = By.cssSelector(
            "a.btn[href*='/register'], a[href*='/register']");
    private static final By MONTHLY_TAB = By.xpath(
            "//button[contains(@class, 'tabBtn') and contains(normalize-space(), 'Monthly')]");
    private static final By YEARLY_TAB = By.xpath(
            "//button[contains(@class, 'tabBtn') and contains(normalize-space(), 'Yearly')]");
    private static final By APP_STORE_LINK = By.cssSelector(
            "a[href*='apps.apple.com']");
    private static final By GOOGLE_PLAY_LINK = By.cssSelector(
            "a[href*='play.google.com']");

    public LandingPage(WebDriver driver) {
        super(driver);
    }

    @Step("Open landing page at {baseUrl}")
    public void open(String baseUrl) {
        driver.get(baseUrl);
        visible(LOGO);
    }

    public boolean isLoaded() {
        return isElementVisible(LOGO) && isElementVisible(SIGN_IN_LINK);
    }

    public boolean hasNavMenu() {
        return isElementPresent(NAV_BAR) || isElementPresent(NAV_ITEMS);
    }

    public int getNavItemCount() {
        return driver.findElements(NAV_ITEMS).size();
    }

    public boolean hasLanguageSwitcher() {
        return isElementPresent(LANGUAGE_SWITCHER);
    }

    @Step("Cari seçilmiş dil kodunu oxu (məs. 'AZ', 'EN', 'РУ')")
    public String getCurrentLanguageCode() {
        if (!isElementPresent(CURRENT_LANGUAGE_LABEL)) {
            return "";
        }
        return driver.findElement(CURRENT_LANGUAGE_LABEL).getText().trim();
    }

    @Step("Seçilmiş dil göstəricisinin '{expectedCode}' olmasını gözlə")
    public boolean waitForLanguageCode(String expectedCode, Duration timeout) {
        try {
            return new WebDriverWait(driver, timeout)
                    .until(ExpectedConditions.textToBePresentInElementLocated(CURRENT_LANGUAGE_LABEL, expectedCode));
        } catch (TimeoutException e) {
            return false;
        }
    }

    @Step("Dil menyusunu aç")
    public void openLanguageMenu() {
        click(LANGUAGE_SWITCHER);
        visible(LANG_BOX);
    }

    @Step("Dili dəyiş: {langCode} ('az', 'en', 'ru')")
    public void switchLanguage(String langCode) {
        String expectedText;
        By targetLocator;
        if ("en".equalsIgnoreCase(langCode)) {
            expectedText = "EN";
            targetLocator = EN_LANG_ITEM;
        } else if ("ru".equalsIgnoreCase(langCode)) {
            expectedText = "РУ";
            targetLocator = RU_LANG_ITEM;
        } else {
            expectedText = "AZ";
            targetLocator = AZ_LANG_ITEM;
        }

        openLanguageMenu();
        WebElement target = visible(targetLocator);
        try {
            target.click();
        } catch (Exception e) {
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", target);
        }
        waitForLanguageCode(expectedText, Duration.ofSeconds(5));
    }

    @Step("Giriş linkinin cari mətnini al")
    public String getSignInLinkText() {
        if (!isElementVisible(SIGN_IN_LINK)) {
            return "";
        }
        return driver.findElement(SIGN_IN_LINK).getText().trim();
    }

    public boolean hasSignInLink() {
        return isElementVisible(SIGN_IN_LINK);
    }

    public boolean hasRegisterButton() {
        return isElementVisible(REGISTER_BUTTON);
    }

    public boolean hasMonthlyTab() {
        return isElementPresent(MONTHLY_TAB);
    }

    public boolean hasYearlyTab() {
        return isElementPresent(YEARLY_TAB);
    }

    @Step("Switch package billing tab to Yearly")
    public void switchToYearlyTab() {
        click(YEARLY_TAB);
    }

    @Step("Switch package billing tab to Monthly")
    public void switchToMonthlyTab() {
        click(MONTHLY_TAB);
    }

    public boolean isYearlyTabActive() {
        WebElement tab = visible(YEARLY_TAB);
        String cls = tab.getAttribute("class");
        return cls != null && cls.contains("active");
    }

    public boolean isMonthlyTabActive() {
        WebElement tab = visible(MONTHLY_TAB);
        String cls = tab.getAttribute("class");
        return cls != null && cls.contains("active");
    }

    public boolean hasAppStoreBadge() {
        return isElementPresent(APP_STORE_LINK);
    }

    public boolean hasGooglePlayBadge() {
        return isElementPresent(GOOGLE_PLAY_LINK);
    }

    @Step("Navigate to Login page from header sign-in link")
    public LoginPage openLoginPage() {
        click(SIGN_IN_LINK);
        return new LoginPage(driver);
    }
}
