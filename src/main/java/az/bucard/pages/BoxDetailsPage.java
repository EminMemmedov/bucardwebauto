package az.bucard.pages;

import io.qameta.allure.Step;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.util.List;
import java.util.Locale;

public final class BoxDetailsPage extends BasePage {
    private static final By BOX_HEADING = By.xpath(
            "//*[self::h1 or self::h2 or self::h3][contains(@class, 'title') or contains(@class, 'box') or contains(@class, 'heading') or contains(normalize-space(), 'Personals') or contains(normalize-space(), 'Companies') or contains(normalize-space(), 'Test')]");
    private static final By SEARCH_CONTACT_INPUT = By.cssSelector(
            "input[placeholder*='Search'], input[type='search'], input[name='search']");
    private static final By ADD_CONTACT_BUTTON = By.xpath(
            "//button[contains(normalize-space(), 'Add') or contains(., 'Əlavə et') or contains(@class, 'add')]");
    private static final By EMPTY_STATE_MESSAGE = By.xpath(
            "//*[contains(normalize-space(), 'User not found') or contains(normalize-space(), 'No contacts') or contains(normalize-space(), 'Tapılmadı') or contains(normalize-space(), 'No users')]");
    private static final By CONTACT_ITEMS = By.cssSelector(
            ".contact-card, .contact-item, div[class*='contact'], div[class*='user-item']");
    private static final By BACK_TO_DASHBOARD_LINK = By.cssSelector(
            "a[href*='/dashboard'], .go-back");

    public BoxDetailsPage(WebDriver driver) {
        super(driver);
    }

    public boolean isLoaded() {
        return isElementVisible(SEARCH_CONTACT_INPUT) || isElementVisible(BOX_HEADING) || hasEmptyState() || hasAddContactButton();
    }

    public boolean hasContactSearch() {
        return isElementPresent(SEARCH_CONTACT_INPUT);
    }

    public boolean hasAddContactButton() {
        return isElementPresent(ADD_CONTACT_BUTTON);
    }

    public boolean hasEmptyState() {
        return isElementPresent(EMPTY_STATE_MESSAGE);
    }

    public String getEmptyStateText() {
        return getTextSafe(EMPTY_STATE_MESSAGE);
    }

    public int getContactCount() {
        return driver.findElements(CONTACT_ITEMS).size();
    }

    @Step("Qutunun başlığını əldə et")
    public String getBoxTitleText() {
        return getTextSafe(BOX_HEADING);
    }

    @Step("Qutuda profilin mövcudluğunu yoxla: '{expectedFullName}' / '{expectedUsername}'")
    public boolean containsProfile(String expectedFullName, String expectedUsername) {
        String nameLower = expectedFullName == null ? "" : expectedFullName.trim().toLowerCase(Locale.ROOT);
        String userLower = expectedUsername == null ? "" : expectedUsername.trim().toLowerCase(Locale.ROOT);

        if (nameLower.isEmpty() && userLower.isEmpty()) {
            return false;
        }

        List<WebElement> contacts = driver.findElements(CONTACT_ITEMS);
        for (WebElement item : contacts) {
            try {
                String text = item.getText().toLowerCase(Locale.ROOT);
                String hrefAttr = item.getAttribute("innerHTML");
                String combined = text + " " + (hrefAttr == null ? "" : hrefAttr.toLowerCase(Locale.ROOT));
                if ((!nameLower.isEmpty() && combined.contains(nameLower))
                        || (!userLower.isEmpty() && combined.contains(userLower))) {
                    return true;
                }
            } catch (Exception ignored) {}
        }

        try {
            String bodyText = driver.findElement(By.tagName("body")).getText().toLowerCase(Locale.ROOT);
            if ((!nameLower.isEmpty() && bodyText.contains(nameLower))
                    || (!userLower.isEmpty() && bodyText.contains(userLower))) {
                return true;
            }
        } catch (Exception ignored) {}

        return false;
    }

    public boolean containsOnlyUser(String unexpectedEmailOrName) {
        if (unexpectedEmailOrName == null || unexpectedEmailOrName.isBlank()) {
            return false;
        }
        String unexpectedLower = unexpectedEmailOrName.trim().toLowerCase(Locale.ROOT);
        List<WebElement> contacts = driver.findElements(CONTACT_ITEMS);
        if (contacts.isEmpty()) {
            return false;
        }
        for (WebElement item : contacts) {
            try {
                String text = item.getText().toLowerCase(Locale.ROOT);
                if (!text.isBlank() && !text.contains(unexpectedLower)) {
                    return false;
                }
            } catch (Exception ignored) {}
        }
        return true;
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
