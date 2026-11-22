package az.bucard.pages;

import io.qameta.allure.Step;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;

import java.time.Duration;
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
            "//*[self::h1 or self::h2 or self::h3 or self::div][normalize-space()='All Boxes' or normalize-space()='My Box' or contains(normalize-space(), 'Boxes')]");
    private static final By HEADER_SEARCH_INPUT = By.cssSelector(
            "input[placeholder*='Search profiles'], input[placeholder*='Search']");
    private static final By BONUS_BADGE = By.xpath(
            "//*[contains(normalize-space(), 'Bonus balance') or contains(@class, 'bonus')]");
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
    private static final By NOTIFICATION_ICON = By.cssSelector(
            "div[class*='notification'], a[href*='/notification'], svg.bellIcon, .notificationBell");
    private static final By LOGOUT_BUTTON = By.xpath(
            "//*[contains(@class, 'logout') or contains(@href, '/logout') or normalize-space()='Log out' or normalize-space()='Çıxış']");
    private static final By USER_DROPDOWN = By.cssSelector(
            "div[class*='userProfile'], div[class*='profileDropdown'], .avatarSmall");
    private static final By SHARE_HEADER_ICON = By.cssSelector(
            ".dashboardHeader .shareIcon");
    private static final By SHARED_PROFILE_MODAL = By.cssSelector(
            ".sharedProfileModal.active");
    private static final By SHARED_MODAL_LINK = By.cssSelector(
            ".sharedProfileModal .sharedLink");
    private static final By SHARED_MODAL_QR_IMG = By.cssSelector(
            ".sharedProfileModal .qrCode img");
    private static final By SHARED_MODAL_OVERLAY = By.cssSelector(
            ".sharedProfileModal .modalOverlay");

    public record ShareProfileInfo(String shareLink, String qrImageUrl, String username) {}

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

    public BoxManagementPage openBoxManagement() {
        waitForPreloaderToDisappear();
        dismissModalIfPresent();
        return new BoxManagementPage(driver);
    }

    public SubscriptionPage openSubscription() {
        waitForPreloaderToDisappear();
        dismissModalIfPresent();
        openSettings();
        return new SubscriptionPage(driver);
    }

    public BonusPage openBonusPage() {
        waitForPreloaderToDisappear();
        dismissModalIfPresent();
        if (isElementPresent(BONUS_BADGE)) {
            click(BONUS_BADGE);
        }
        return new BonusPage(driver);
    }

    public NotificationsPage openNotifications() {
        waitForPreloaderToDisappear();
        dismissModalIfPresent();
        if (isElementVisible(NOTIFICATION_ICON)) {
            try {
                click(NOTIFICATION_ICON);
            } catch (Exception e) {
                try {
                    ((JavascriptExecutor) driver).executeScript("arguments[0].click();", driver.findElement(NOTIFICATION_ICON));
                } catch (Exception ignored) {}
            }
        }
        return new NotificationsPage(driver);
    }

    @Step("İstifadəçi hesabından çıxış et (Logout)")
    public LoginPage logout() {
        waitForPreloaderToDisappear();
        dismissModalIfPresent();

        if (isElementPresent(USER_DROPDOWN)) {
            try {
                click(USER_DROPDOWN);
                Thread.sleep(500);
            } catch (Exception ignored) {}
        }

        if (isElementPresent(LOGOUT_BUTTON)) {
            click(LOGOUT_BUTTON);
        } else {
            // Birbaşa settings və ya /login ünvanına yönləndirmə
            driver.get(driver.getCurrentUrl().replaceAll("/dashboard.*", "/login"));
        }

        try {
            wait.until(ExpectedConditions.urlContains("/login"));
        } catch (Exception ignored) {}

        return new LoginPage(driver);
    }

    @Step("Paylaşım modalını aç və link/QR məlumatlarını əldə et")
    public ShareProfileInfo openShareModalAndExtractInfo() {
        waitForPreloaderToDisappear();
        dismissModalIfPresent();

        if (isElementVisible(SHARE_HEADER_ICON, Duration.ofSeconds(5))) {
            click(SHARE_HEADER_ICON);
        } else {
            click(SHARE_ELEMENT);
        }

        visible(SHARED_PROFILE_MODAL);

        String shareLink = "";
        try {
            WebElement linkEl = visible(SHARED_MODAL_LINK);
            shareLink = linkEl.getAttribute("href");
            if (shareLink == null || shareLink.isBlank()) {
                shareLink = linkEl.getText().trim();
            }
        } catch (Exception ignored) {}

        String qrImageUrl = "";
        try {
            if (isElementVisible(SHARED_MODAL_QR_IMG, Duration.ofSeconds(5))) {
                qrImageUrl = visible(SHARED_MODAL_QR_IMG).getAttribute("src");
            }
        } catch (Exception ignored) {}

        String username = extractUsernameFromUrl(shareLink);

        try {
            if (isElementVisible(SHARED_MODAL_OVERLAY, Duration.ofSeconds(2))) {
                click(SHARED_MODAL_OVERLAY);
            }
        } catch (Exception ignored) {}

        return new ShareProfileInfo(
                shareLink == null ? "" : shareLink.trim(),
                qrImageUrl == null ? "" : qrImageUrl.trim(),
                username
        );
    }

    private String extractUsernameFromUrl(String url) {
        if (url == null || url.isBlank()) {
            return "";
        }
        String clean = url.split("[?#]")[0].replaceAll("/+$", "");
        int idx = clean.lastIndexOf('/');
        return idx >= 0 && idx < clean.length() - 1 ? clean.substring(idx + 1).trim() : "";
    }
}
