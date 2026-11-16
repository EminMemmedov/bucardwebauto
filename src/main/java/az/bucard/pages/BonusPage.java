package az.bucard.pages;

import io.qameta.allure.Step;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

public final class BonusPage extends BasePage {
    private static final By BONUS_HEADING = By.xpath(
            "//*[self::h1 or self::h2 or self::h3][contains(normalize-space(), 'Bonus') or contains(normalize-space(), 'Mükafat')]");
    private static final By BONUS_BALANCE_AMOUNT = By.xpath(
            "//*[contains(@class, 'bonusAmount') or contains(@class, 'balance') or contains(@class, 'bonus-value')]");
    private static final By TRANSACTION_HISTORY_LIST = By.cssSelector(
            ".bonus-history, .transaction-list, div[class*='historyItem'], div[class*='transaction']");
    private static final By INVITE_LINK_INPUT = By.cssSelector(
            "input[class*='referral'], input[class*='invite'], input[readonly]");

    public BonusPage(WebDriver driver) {
        super(driver);
    }

    public boolean isLoaded() {
        return isElementVisible(BONUS_HEADING) || isElementVisible(BONUS_BALANCE_AMOUNT);
    }

    @Step("Bonus balansını oxu")
    public String getBonusBalance() {
        return getTextSafe(BONUS_BALANCE_AMOUNT);
    }

    public int getHistoryTransactionsCount() {
        return driver.findElements(TRANSACTION_HISTORY_LIST).size();
    }

    public boolean hasInviteLink() {
        return isElementPresent(INVITE_LINK_INPUT);
    }
}
