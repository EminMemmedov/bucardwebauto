package az.bucard.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

public final class SearchPage extends BasePage {
    private static final By PAGE_HEADING = By.xpath(
            "//*[self::h1 or self::h2 or self::h3 or self::p][contains(normalize-space(), 'Search personal or business profiles') or contains(normalize-space(), 'Search personal or business')]");
    private static final By SEARCH_FIELD = By.cssSelector(
            "#bucardSearch, input[name='search'], input[type='search'], input[placeholder*='Search']");
    private static final By SEARCH_BUTTON = By.xpath(
            "//button[normalize-space()='Search' or .//*[normalize-space()='Search'] or @type='submit']");
    private static final By RESULTS_CONTAINER = By.cssSelector(
            ".search-results, div[class*='search-result'], div[class*='results']");
    private static final By EMPTY_STATE = By.xpath(
            "//*[contains(normalize-space(), 'not found') or contains(normalize-space(), 'No results') or contains(normalize-space(), 'Tapılmadı')]");

    public SearchPage(WebDriver driver) {
        super(driver);
    }

    public boolean isLoaded() {
        return isElementVisible(PAGE_HEADING) || isElementVisible(SEARCH_FIELD);
    }

    public void enterQuery(String query) {
        clearAndType(SEARCH_FIELD, query);
    }

    public void submitSearch() {
        click(SEARCH_BUTTON);
    }

    public boolean hasSearchResults() {
        return isElementPresent(RESULTS_CONTAINER);
    }

    public boolean hasEmptyState() {
        return isElementPresent(EMPTY_STATE);
    }

    public String getEmptyStateText() {
        return getTextSafe(EMPTY_STATE);
    }
}
