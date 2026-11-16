package az.bucard.pages;

import io.qameta.allure.Step;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

public final class ProfilePage extends BasePage {
    private static final By CONTACT_INFO_HEADING = By.xpath(
            "//*[self::h1 or self::h2 or self::h3 or self::h4][contains(normalize-space(), 'Contact info') or contains(normalize-space(), 'Əlaqə')]");
    private static final By PORTFOLIO_SECTION = By.xpath(
            "//*[self::h1 or self::h2 or self::h3 or self::h4][contains(normalize-space(), 'Portfolio')]");
    private static final By EXPERIENCE_SECTION = By.xpath(
            "//*[self::h1 or self::h2 or self::h3 or self::h4][contains(normalize-space(), 'Experience') or contains(normalize-space(), 'Təcrübə')]");
    private static final By SKILLS_SECTION = By.xpath(
            "//*[self::h1 or self::h2 or self::h3 or self::h4][contains(normalize-space(), 'Skills') or contains(normalize-space(), 'Bacarıqlar')]");
    private static final By LANGUAGES_SECTION = By.xpath(
            "//*[self::h1 or self::h2 or self::h3 or self::h4][contains(normalize-space(), 'Languages') or contains(normalize-space(), 'Dillər')]");
    private static final By KEYWORDS_SECTION = By.xpath(
            "//*[self::h1 or self::h2 or self::h3 or self::h4][contains(normalize-space(), 'Keywords') or contains(normalize-space(), 'Açar sözlər')]");
    private static final By STATUS_BADGE = By.xpath(
            "//*[contains(@class, 'status') or contains(@class, 'badge') or contains(translate(normalize-space(), 'PRIVATE', 'private'), 'private') or contains(translate(normalize-space(), 'PUBLIC', 'public'), 'public')]");
    private static final By PROFILE_AVATAR = By.cssSelector(
            "img[class*='avatar'], img[class*='profile'], div[class*='avatar'], img[alt*='avatar']");
    private static final By PROFILE_FULL_NAME = By.cssSelector(
            "h1.name, h2.name, div[class*='profileName'], .user-name, div[class*='userTitle']");
    private static final By EDIT_PROFILE_BTN = By.xpath(
            "//button[contains(normalize-space(), 'Edit') or contains(normalize-space(), 'Düzəliş') or contains(@class, 'editBtn')] | //a[contains(@href, '/edit') or contains(@class, 'edit')]");
    private static final By BIO_INPUT = By.cssSelector(
            "textarea[name='bio'], textarea[name='about'], textarea[placeholder*='Bio'], textarea[placeholder*='haqqında']");
    private static final By SAVE_PROFILE_BTN = By.xpath(
            "//button[contains(normalize-space(), 'Save') or contains(normalize-space(), 'Yadda saxla') or contains(@class, 'saveBtn')]");
    private static final By PRIVACY_SWITCH = By.cssSelector(
            "input[type='checkbox'][name*='privacy'], .privacy-switch, div[class*='visibilitySwitch']");
    private static final By SHARE_LINK_BTN = By.xpath(
            "//button[contains(normalize-space(), 'Share') or contains(normalize-space(), 'Paylaş')] | //*[contains(@class, 'shareBtn')]");
    private static final By BACK_TO_DASHBOARD_LINK = By.cssSelector(
            "a[href*='/dashboard'], .go-back");

    public ProfilePage(WebDriver driver) {
        super(driver);
    }

    public boolean isLoaded() {
        return isElementVisible(CONTACT_INFO_HEADING) || isElementVisible(PROFILE_AVATAR) || isElementVisible(STATUS_BADGE);
    }

    public boolean hasContactInfoSection() {
        return isElementPresent(CONTACT_INFO_HEADING);
    }

    public boolean hasPortfolioSection() {
        return isElementPresent(PORTFOLIO_SECTION);
    }

    public boolean hasExperienceSection() {
        return isElementPresent(EXPERIENCE_SECTION);
    }

    public boolean hasSkillsSection() {
        return isElementPresent(SKILLS_SECTION);
    }

    public boolean hasLanguagesSection() {
        return isElementPresent(LANGUAGES_SECTION);
    }

    public boolean hasKeywordsSection() {
        return isElementPresent(KEYWORDS_SECTION);
    }

    public boolean hasPrivateStatusIndicator() {
        String text = getTextSafe(STATUS_BADGE).toLowerCase();
        return text.contains("private") || isElementPresent(STATUS_BADGE);
    }

    public boolean hasAvatar() {
        return isElementPresent(PROFILE_AVATAR);
    }

    @Step("Profil sahibinin tam adını əldə et")
    public String getProfileFullName() {
        return getTextSafe(PROFILE_FULL_NAME);
    }

    @Step("Profil redaktə pəncərəsini aç")
    public boolean openEditProfile() {
        if (isElementPresent(EDIT_PROFILE_BTN)) {
            click(EDIT_PROFILE_BTN);
            return true;
        }
        return false;
    }

    @Step("Profil haqqında (Bio) mətnini yenilə: {bioText}")
    public void updateBio(String bioText) {
        if (isElementPresent(BIO_INPUT)) {
            type(BIO_INPUT, bioText);
        }
    }

    @Step("Profil dəyişikliklərini yadda saxla")
    public void saveProfileChanges() {
        if (isElementPresent(SAVE_PROFILE_BTN)) {
            click(SAVE_PROFILE_BTN);
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ignored) {}
        }
    }

    @Step("Profilin ictimai (Public) paylaşılan URL linkini əldə et")
    public String getPublicProfileUrl() {
        if (isElementPresent(SHARE_LINK_BTN)) {
            click(SHARE_LINK_BTN);
        }
        return driver.getCurrentUrl();
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
