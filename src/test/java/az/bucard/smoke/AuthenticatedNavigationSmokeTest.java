package az.bucard.smoke;

import az.bucard.base.BaseTest;
import az.bucard.config.Config;
import az.bucard.pages.BoxDetailsPage;
import az.bucard.pages.DashboardPage;
import az.bucard.pages.LoginPage;
import az.bucard.pages.ProfilePage;
import az.bucard.pages.SearchPage;
import az.bucard.pages.SettingsPage;
import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import io.qameta.allure.Story;
import org.openqa.selenium.WebDriver;
import org.testng.Assert;
import org.testng.SkipException;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.List;

@Epic("Authenticated Smoke")
@Feature("Dashboard & Internal Services")
public class AuthenticatedNavigationSmokeTest extends BaseTest {

    @BeforeMethod(alwaysRun = true)
    public void checkCredentials() {
        if (!Config.isAuthAvailable()) {
            throw new SkipException(
                    "BUCARD_TEST_USER / BUCARD_TEST_PASSWORD are not provided. "
                    + "Authenticated smoke tests require explicit test credentials via environment variables or system properties."
            );
        }
    }

    private DashboardPage performLogin() {
        WebDriver driver = getDriver();
        LoginPage loginPage = new LoginPage(driver);
        loginPage.open(Config.baseUrl());
        loginPage.login(Config.testUser(), Config.testPassword());

        DashboardPage dashboardPage = new DashboardPage(driver);
        dashboardPage.waitForUrlContains("/dashboard");
        dashboardPage.waitForPreloaderToDisappear();
        Assert.assertTrue(dashboardPage.isLoaded(), "Dashboard heading ('All Boxes') or box cards should load after valid login");
        dashboardPage.dismissModalIfPresent();
        return dashboardPage;
    }

    @Test(description = "POST-01: Verify authenticated login transitions to Dashboard ('All Boxes')", groups = {"authenticated", "read-only"})
    public void loginAndDashboardLoad() {
        DashboardPage dashboardPage = performLogin();
        Assert.assertTrue(dashboardPage.isLoaded(), "Dashboard should be loaded");
    }

    @Test(description = "POST-02: Verify Dashboard header elements, bonus badge, share action, and notice banner", groups = {"authenticated", "read-only"}, dependsOnMethods = "loginAndDashboardLoad")
    public void dashboardHeaderAndElements() {
        DashboardPage dashboardPage = performLogin();

        Assert.assertTrue(dashboardPage.hasHeaderSearchInput(), "Header search input field should be present");
        Assert.assertTrue(dashboardPage.hasBonusBadge(), "Bonus balance indicator badge should be present");
        Assert.assertTrue(dashboardPage.hasShareButton(), "Share action element should be present");
        Assert.assertTrue(dashboardPage.hasEmailConfirmationNotice(), "Email confirmation notice banner should be present");
    }

    @Test(description = "POST-03: Verify Dashboard contains Box cards and member counts", groups = {"authenticated", "read-only"}, dependsOnMethods = "loginAndDashboardLoad")
    public void dashboardBoxesListVerification() {
        DashboardPage dashboardPage = performLogin();

        int boxCount = dashboardPage.visibleBoxCount();
        Assert.assertTrue(boxCount > 0, "Dashboard should contain at least 1 box card, found: " + boxCount);

        List<String> names = dashboardPage.getBoxNames();
        Assert.assertFalse(names.isEmpty(), "Box names list should not be empty");
    }

    @Test(description = "POST-04: Verify read-only navigation to Profile and presence of card sections", groups = {"authenticated", "read-only"}, dependsOnMethods = "loginAndDashboardLoad")
    public void profilePageReadSmoke() {
        DashboardPage dashboardPage = performLogin();
        ProfilePage profilePage = dashboardPage.openProfile();

        Assert.assertTrue(profilePage.isLoaded(), "Profile page should load from Dashboard sidebar");
        Assert.assertTrue(profilePage.hasAvatar(), "Profile avatar should be present");
        Assert.assertTrue(profilePage.hasPrivateStatusIndicator(), "Private status indicator badge should be present");
    }

    @Test(description = "POST-05: Verify read-only navigation to Search page and search form controls", groups = {"authenticated", "read-only"}, dependsOnMethods = "loginAndDashboardLoad")
    public void searchPageReadSmoke() {
        DashboardPage dashboardPage = performLogin();
        SearchPage searchPage = dashboardPage.openSearch();

        Assert.assertTrue(searchPage.isLoaded(), "Search page should load from Dashboard sidebar");
        searchPage.enterQuery("test_smoke_check");
        searchPage.submitSearch();

        Assert.assertTrue(searchPage.hasSearchResults() || searchPage.hasEmptyState() || searchPage.isLoaded(),
                "Search should display results or clean empty state without crashing");
    }

    @Test(description = "POST-06: Verify read-only navigation to Settings and visibility of standard sections", groups = {"authenticated", "read-only"}, dependsOnMethods = "loginAndDashboardLoad")
    public void settingsPageReadSmoke() {
        DashboardPage dashboardPage = performLogin();
        SettingsPage settingsPage = dashboardPage.openSettings();

        Assert.assertTrue(settingsPage.isLoaded(), "Settings page should load from Dashboard sidebar");
        Assert.assertTrue(settingsPage.hasAccountCenter() || settingsPage.hasPlanSection() || settingsPage.hasHelpSection(),
                "Settings page should display core section navigation links");
    }

    @Test(description = "POST-07: Verify opening a Box details view displays contact search and member list / empty state", groups = {"authenticated", "read-only"}, dependsOnMethods = "loginAndDashboardLoad")
    public void boxDetailsReadSmoke() {
        DashboardPage dashboardPage = performLogin();
        BoxDetailsPage boxDetailsPage = dashboardPage.openFirstAvailableBox();

        Assert.assertTrue(boxDetailsPage.isLoaded(), "Box details view should load upon clicking a Box card");
        Assert.assertTrue(boxDetailsPage.hasContactSearch() || boxDetailsPage.hasEmptyState() || boxDetailsPage.hasAddContactButton(),
                "Box details should display search field or member/empty state");
    }
}
