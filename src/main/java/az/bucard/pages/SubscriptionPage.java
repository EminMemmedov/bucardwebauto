package az.bucard.pages;

import io.qameta.allure.Step;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

public final class SubscriptionPage extends BasePage {
    private static final By PLAN_HEADING = By.xpath(
            "//*[self::h1 or self::h2 or self::h3][contains(normalize-space(), 'Plan') or contains(normalize-space(), 'Subscription') or contains(normalize-space(), 'Tarif')]");
    private static final By CURRENT_PLAN_BADGE = By.xpath(
            "//*[contains(@class, 'currentPlan') or contains(@class, 'planBadge') or contains(normalize-space(), 'Current Plan') or contains(normalize-space(), 'Cari Plan') or contains(normalize-space(), 'Free') or contains(normalize-space(), 'Pro')]");
    private static final By UPGRADE_BUTTON = By.xpath(
            "//button[contains(normalize-space(), 'Upgrade') or contains(normalize-space(), 'Paketi artır') or contains(normalize-space(), 'Pro-ya keç')]");
    private static final By PLAN_CARDS = By.cssSelector(
            ".plan-card, div[class*='pricingCard'], div[class*='planBox']");

    public SubscriptionPage(WebDriver driver) {
        super(driver);
    }

    public boolean isLoaded() {
        return isElementVisible(PLAN_HEADING) || isElementVisible(CURRENT_PLAN_BADGE) || isElementVisible(PLAN_CARDS);
    }

    @Step("Cari abunəlik planının adını oxu")
    public String getCurrentPlanName() {
        return getTextSafe(CURRENT_PLAN_BADGE);
    }

    public boolean isProUser() {
        return getCurrentPlanName().toLowerCase().contains("pro");
    }

    public boolean isFreeUser() {
        return getCurrentPlanName().toLowerCase().contains("free") || !isProUser();
    }

    public boolean hasUpgradeButton() {
        return isElementPresent(UPGRADE_BUTTON);
    }

    public int getAvailablePlansCount() {
        return driver.findElements(PLAN_CARDS).size();
    }
}
