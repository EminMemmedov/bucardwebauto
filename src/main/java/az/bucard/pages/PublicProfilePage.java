package az.bucard.pages;

import io.qameta.allure.Step;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;

import java.time.Duration;
import java.util.List;
import java.util.Locale;

public final class PublicProfilePage extends BasePage {
    private static final By PUBLIC_NAME = By.cssSelector(
            "h1, h2, .profile-name, div[class*='name']");
    private static final By PUBLIC_AVATAR = By.cssSelector(
            "img[class*='avatar'], img[class*='profile'], div[class*='avatar']");
    private static final By PUBLIC_BIO = By.cssSelector(
            ".bio, .about, p[class*='bio'], div[class*='description']");
    private static final By CONTACT_INFO = By.cssSelector(
            ".contact-info, div[class*='contact'], a[href^='mailto:'], a[href^='tel:']");
    private static final By RESTRICTED_NOTICE = By.xpath(
            "//*[contains(normalize-space(), 'Private') or contains(normalize-space(), 'Gizli profil') or contains(normalize-space(), 'Page Not Found') or contains(normalize-space(), 'İcazəsiz')]");
    private static final By PRELOADER = By.cssSelector(
            ".preloader, div.splash, div[class*='preloader']");
    private static final By ADD_TO_BOX_BTN = By.cssSelector("button.addBtn");
    private static final By ADDED_TO_BOX_BTN = By.cssSelector("button.addBtn.added");
    private static final By ADD_TO_BOX_MODAL = By.cssSelector(".modalBox");
    private static final By MODAL_BOX_CARDS = By.cssSelector(".modalBox ul.boxList li.boxCard");
    private static final By MODAL_BOX_NAME = By.cssSelector(".boxName");
    private static final By MODAL_CONFIRM_ADD_BTN = By.cssSelector(".modalBox button.addBtn");

    public PublicProfilePage(WebDriver driver) {
        super(driver);
    }

    @Step("İctimai profil səhifəsini aç: '{url}'")
    public void open(String url) {
        driver.get(url);
    }

    public boolean isLoaded() {
        return isElementVisible(PUBLIC_NAME) || isElementVisible(PUBLIC_AVATAR) || isPrivateRestricted();
    }

    public boolean isAccessible() {
        return isElementVisible(PUBLIC_NAME) && !isPrivateRestricted();
    }

    public boolean isPrivateRestricted() {
        return isElementPresent(RESTRICTED_NOTICE);
    }

    public String getDisplayedName() {
        return getTextSafe(PUBLIC_NAME);
    }

    public boolean hasAvatar() {
        return isElementPresent(PUBLIC_AVATAR);
    }

    public boolean hasBio() {
        return isElementPresent(PUBLIC_BIO);
    }

    public void waitForProfileReady() {
        try {
            wait.until(ExpectedConditions.invisibilityOfElementLocated(PRELOADER));
        } catch (Exception ignored) {
        }
        try {
            wait.until(d -> isElementPresent(PUBLIC_NAME)
                    || isElementPresent(ADD_TO_BOX_BTN)
                    || isElementPresent(RESTRICTED_NOTICE));
        } catch (Exception ignored) {
        }
    }

    public String getCurrentUsernameFromUrl() {
        String url = driver.getCurrentUrl();
        if (url == null || url.isBlank()) {
            return "";
        }
        String cleanUrl = url.split("[?#]")[0].replaceAll("/+$", "");
        String[] markers = {"/profile/", "/shared-user/"};
        for (String marker : markers) {
            int idx = cleanUrl.indexOf(marker);
            if (idx >= 0) {
                String tail = cleanUrl.substring(idx + marker.length());
                int slash = tail.indexOf('/');
                return (slash >= 0 ? tail.substring(0, slash) : tail).trim();
            }
        }
        return "";
    }

    @Step("Açılan profilin gözlənilən istifadəçiyə aid olduğunu yoxla: '{expectedUsername}' / '{expectedFullName}'")
    public boolean isProfileOf(String expectedUsername, String expectedFullName) {
        waitForProfileReady();
        boolean usernameMatched = false;
        if (expectedUsername != null && !expectedUsername.isBlank()) {
            String urlUsername = getCurrentUsernameFromUrl().toLowerCase(Locale.ROOT);
            usernameMatched = urlUsername.equals(expectedUsername.trim().toLowerCase(Locale.ROOT))
                    || urlUsername.contains(expectedUsername.trim().toLowerCase(Locale.ROOT));
        }

        boolean fullNameMatched = true;
        if (expectedFullName != null && !expectedFullName.isBlank()) {
            String expectedLower = expectedFullName.trim().toLowerCase(Locale.ROOT);
            String displayed = getDisplayedName().toLowerCase(Locale.ROOT);
            if (displayed.contains(expectedLower)) {
                fullNameMatched = true;
            } else {
                String bodyText = "";
                try {
                    bodyText = driver.findElement(By.tagName("body")).getText().toLowerCase(Locale.ROOT);
                } catch (Exception ignored) {}
                fullNameMatched = bodyText.contains(expectedLower);
            }
        }

        return (expectedUsername == null || expectedUsername.isBlank() || usernameMatched) && fullNameMatched;
    }

    public boolean hasAddToBoxButton() {
        waitForProfileReady();
        return isElementVisible(ADD_TO_BOX_BTN, Duration.ofSeconds(5));
    }

    public boolean isAlreadyAddedToBox() {
        return isElementPresent(ADDED_TO_BOX_BTN);
    }

    @Step("'+ Add to Box' düyməsinə kliklə")
    public void clickAddToBox() {
        waitForProfileReady();
        click(ADD_TO_BOX_BTN);
    }

    public boolean isAddToBoxModalOpen() {
        return isElementVisible(MODAL_BOX_CARDS, Duration.ofSeconds(5))
                || isElementVisible(ADD_TO_BOX_MODAL, Duration.ofSeconds(5));
    }

    @Step("Mövcud qutunu seç və profili əlavə et")
    public String selectFirstAvailableBoxAndConfirm() {
        wait.until(ExpectedConditions.visibilityOfElementLocated(MODAL_BOX_CARDS));
        List<WebElement> boxCards = driver.findElements(MODAL_BOX_CARDS);
        if (boxCards.isEmpty()) {
            throw new IllegalStateException("Qutu seçimi modalında heç bir qutu (li.boxCard) tapılmadı");
        }
        WebElement firstCard = boxCards.get(0);
        String selectedBoxName = "";
        try {
            selectedBoxName = firstCard.findElement(MODAL_BOX_NAME).getText().trim();
        } catch (Exception e) {
            selectedBoxName = firstCard.getText().trim();
        }
        firstCard.click();

        click(MODAL_CONFIRM_ADD_BTN);
        waitForUrlContains("/box-details/");
        return selectedBoxName;
    }
}
