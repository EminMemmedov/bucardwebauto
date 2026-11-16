package az.bucard.pages;

import io.qameta.allure.Step;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

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
}
