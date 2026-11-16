package az.bucard.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;

import java.util.List;
import java.util.stream.Collectors;

public final class DashboardPage extends BasePage {
    private static final By PRELOADER = By.cssSelector(
            ".preloader, div.splash, div[class*='preloader']");
    private static final By OVERLAY = By.cssSelector(
            "div.overlay, div.customModal.active");
    private static final By MODAL_CLOSE_BUTTON = By.cssSelector(
            ".bonusCloseIcon, div.closeModal, img[alt='close-icon'], div.customModalBox .closeModal");
    private static final By ALL_BOXES_HEADING = By.xpath(
            "//*[self::h1 or self::h2 or self::h3 or self::div][normalize-space()='All Boxes' or normalize-space()='My Box']");
    private static final By HEADER_SEARCH_INPUT = By.cssSelector(
            "input[placeholder*='Search profiles'], input[placeholder*='Search']");
    private static final By BONUS_BADGE = By.xpath(
            "//*[contains(normalize-space(), 'Bonus balance')]");
    private static final By SHARE_ELEMENT = By.xpath(
            "//*[contains(@class, 'share') or normalize-space()='Share' or contains(normalize-space(), 'Share')]");
    private static final By DOWNLOAD_APP_BUTTON = By.xpath(
            "//*[contains(normalize-space(), 'Download App') or contains(normalize-space(), 'Tətbiqi yüklə')]");
    private static final By EMAIL_CONFIRM_NOTICE = By.xpath(
            "//*[contains(normalize-space(), 'confirm') and contains(normalize-space(), 'email')]");
    private static final By SEARCH_NAV_ITEM = By.xpath(
            "//li[normalize-space()='Search'] | //a[contains(@href, '/search')] | //*[self::li or self::div][normalize-space()='Search']");
    private static final By PROFILE_NAV_LINK = By.cssSelector(
            "a[href*='/profile/']");
    private static final By SETTINGS_NAV_LINK = By.cssSelector(
            "a[href*='/settings']");
    private static final By BOX_LINKS = By.cssSelector(
            "a[href*='/box-details/']");

    public DashboardPage(WebDriver driver) {
        super(driver);
    }

    public boolean isLoaded() {
        waitForPreloaderToDisappear();
        dismissModalIfPresent();
        return isElementVisible(ALL_BOXES_HEADING) || isElementVisible(BOX_LINKS);
    }

    public void waitForPreloaderToDisappear() {
        try {
            wait.until(ExpectedConditions.invisibilityOfElementLocated(PRELOADER));
        } catch (Exception ignored) {
        }
    }

    public void dismissModalIfPresent() {
        try {
            if (isElementPresent(MODAL_CLOSE_BUTTON)) {
                driver.findElement(MODAL_CLOSE_BUTTON).click();
            } else if (isElementPresent(OVERLAY)) {
                driver.findElement(OVERLAY).click();
            }
            wait.until(ExpectedConditions.invisibilityOfElementLocated(By.cssSelector("div.overlay, .bonusCloseIcon")));
        } catch (Exception ignored) {
        }
    }

    public boolean hasHeaderSearchInput() {
        waitForPreloaderToDisappear();
        return isElementPresent(HEADER_SEARCH_INPUT);
    }

    public boolean hasBonusBadge() {
        waitForPreloaderToDisappear();
        return isElementPresent(BONUS_BADGE);
    }

    public String getBonusBalanceText() {
        return getTextSafe(BONUS_BADGE);
    }

    public boolean hasShareButton() {
        waitForPreloaderToDisappear();
        return isElementPresent(SHARE_ELEMENT);
    }

    public boolean hasDownloadAppButton() {
        waitForPreloaderToDisappear();
        return isElementPresent(DOWNLOAD_APP_BUTTON);
    }

    public boolean hasEmailConfirmationNotice() {
        waitForPreloaderToDisappear();
        return isElementPresent(EMAIL_CONFIRM_NOTICE);
    }

    public int visibleBoxCount() {
        waitForPreloaderToDisappear();
        dismissModalIfPresent();
        try {
            wait.until(ExpectedConditions.visibilityOfElementLocated(BOX_LINKS));
        } catch (Exception ignored) {
        }
        return (int) driver.findElements(BOX_LINKS).stream()
                .filter(element -> {
                    try {
                        return element.isDisplayed();
                    } catch (Exception e) {
                        return false;
                    }
                })
                .count();
    }

    public List<String> getBoxNames() {
        waitForPreloaderToDisappear();
        dismissModalIfPresent();
        return driver.findElements(BOX_LINKS).stream()
                .map(WebElement::getText)
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());
    }

    public BoxDetailsPage openFirstAvailableBox() {
        waitForPreloaderToDisappear();
        dismissModalIfPresent();
        wait.until(ExpectedConditions.visibilityOfElementLocated(BOX_LINKS));
        click(BOX_LINKS);
        return new BoxDetailsPage(driver);
    }

    public SearchPage openSearch() {
        waitForPreloaderToDisappear();
        dismissModalIfPresent();
        click(SEARCH_NAV_ITEM);
        return new SearchPage(driver);
    }

    public ProfilePage openProfile() {
        waitForPreloaderToDisappear();
        dismissModalIfPresent();
        click(PROFILE_NAV_LINK);
        return new ProfilePage(driver);
    }

    public SettingsPage openSettings() {
        waitForPreloaderToDisappear();
        dismissModalIfPresent();
        click(SETTINGS_NAV_LINK);
        return new SettingsPage(driver);
    }
}
