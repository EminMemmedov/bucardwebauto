package az.bucard.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

public final class SettingsPage extends BasePage {
    private static final By SETTINGS_HEADING = By.xpath(
            "//*[self::h1 or self::h2 or self::h3][contains(normalize-space(), 'Settings') or contains(normalize-space(), 'Tənzimləmələr')]");
    private static final By ACCOUNT_CENTER_LINK = By.xpath(
            "//*[contains(normalize-space(), 'Account Center') or contains(normalize-space(), 'Hesab mərkəzi')]");
    private static final By PLAN_SECTION_LINK = By.xpath(
            "//*[contains(normalize-space(), 'Plan') or contains(normalize-space(), 'Tarif')]");
    private static final By PAYMENTS_SECTION_LINK = By.xpath(
            "//*[contains(normalize-space(), 'Payment') or contains(normalize-space(), 'Ödəniş')]");
    private static final By TERMS_PRIVACY_LINK = By.xpath(
            "//*[contains(normalize-space(), 'Terms') or contains(normalize-space(), 'Privacy') or contains(normalize-space(), 'Qaydalar')]");
    private static final By CARD_CENTER_LINK = By.xpath(
            "//*[contains(normalize-space(), 'Card Center') or contains(normalize-space(), 'Kart')]");
    private static final By HELP_SECTION_LINK = By.xpath(
            "//*[contains(normalize-space(), 'Help') or contains(normalize-space(), 'Kömək') or contains(normalize-space(), 'FAQ')]");
    private static final By BACK_TO_DASHBOARD_LINK = By.cssSelector(
            "a[href*='/dashboard'], .go-back");

    public SettingsPage(WebDriver driver) {
        super(driver);
    }

    public boolean isLoaded() {
        return isElementVisible(SETTINGS_HEADING) || isElementVisible(ACCOUNT_CENTER_LINK) || isElementVisible(PLAN_SECTION_LINK);
    }

    public boolean hasAccountCenter() {
        return isElementPresent(ACCOUNT_CENTER_LINK);
    }

    public boolean hasPlanSection() {
        return isElementPresent(PLAN_SECTION_LINK);
    }

    public boolean hasPaymentsSection() {
        return isElementPresent(PAYMENTS_SECTION_LINK);
    }

    public boolean hasTermsAndPrivacy() {
        return isElementPresent(TERMS_PRIVACY_LINK);
    }

    public boolean hasCardCenter() {
        return isElementPresent(CARD_CENTER_LINK);
    }

    public boolean hasHelpSection() {
        return isElementPresent(HELP_SECTION_LINK);
    }

    public DashboardPage backToDashboard() {
        if (isElementPresent(BACK_TO_DASHBOARD_LINK)) {
            click(BACK_TO_DASHBOARD_LINK);
        } else {
            driver.navigate().back();
        }
        return new DashboardPage(driver);
    }
}
