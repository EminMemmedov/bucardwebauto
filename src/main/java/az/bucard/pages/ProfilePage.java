package az.bucard.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

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
            "//*[contains(@class, 'status') or contains(@class, 'badge') or contains(translate(normalize-space(), 'PRIVATE', 'private'), 'private')]");
    private static final By PROFILE_AVATAR = By.cssSelector(
            "img[class*='avatar'], img[class*='profile'], div[class*='avatar'], img[alt*='avatar']");
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
        return isElementPresent(STATUS_BADGE);
    }

    public boolean hasAvatar() {
        return isElementPresent(PROFILE_AVATAR);
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
