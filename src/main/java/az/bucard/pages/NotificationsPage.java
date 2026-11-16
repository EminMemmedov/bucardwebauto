package az.bucard.pages;

import io.qameta.allure.Step;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

public final class NotificationsPage extends BasePage {
    private static final By NOTIFICATION_HEADING = By.xpath(
            "//*[self::h1 or self::h2 or self::h3][contains(normalize-space(), 'Notifications') or contains(normalize-space(), 'Bildirişlər')]");
    private static final By NOTIFICATION_ITEMS = By.cssSelector(
            ".notification-item, div[class*='notificationItem'], div[class*='noticeCard']");
    private static final By UNREAD_BADGE = By.cssSelector(
            ".unread-badge, span[class*='unread'], div[class*='badge']");
    private static final By MARK_ALL_READ_BTN = By.xpath(
            "//button[contains(normalize-space(), 'Mark all') or contains(normalize-space(), 'Hamısını oxunmuş') or contains(normalize-space(), 'Read all')]");

    public NotificationsPage(WebDriver driver) {
        super(driver);
    }

    public boolean isLoaded() {
        return isElementVisible(NOTIFICATION_HEADING) || isElementVisible(NOTIFICATION_ITEMS) || hasMarkAllAsReadButton();
    }

    public int getNotificationsCount() {
        return driver.findElements(NOTIFICATION_ITEMS).size();
    }

    public boolean hasUnreadBadge() {
        return isElementPresent(UNREAD_BADGE);
    }

    public boolean hasMarkAllAsReadButton() {
        return isElementPresent(MARK_ALL_READ_BTN);
    }

    @Step("Bütün bildirişləri oxunmuş kimi qeyd et")
    public boolean markAllAsRead() {
        if (isElementPresent(MARK_ALL_READ_BTN)) {
            click(MARK_ALL_READ_BTN);
            try {
                Thread.sleep(800);
            } catch (InterruptedException ignored) {}
            return true;
        }
        return false;
    }
}
