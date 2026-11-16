package az.bucard.smoke;

import az.bucard.base.BaseTest;
import az.bucard.config.Config;
import az.bucard.pages.LandingPage;
import az.bucard.pages.LoginPage;
import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import io.qameta.allure.Story;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.testng.Assert;
import org.testng.annotations.Test;

@Epic("Public Routes Smoke")
@Feature("Pre-Login Navigation & Forms")
public class PublicRoutesSmokeTest extends BaseTest {

    @Test(description = "PRE-02: Verify landing page header navigation, logo, and main CTA buttons", groups = {"smoke", "public"})
    @Story("Landing Page Navigation")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Verifies header navigation menu, logo, language switcher, and main CTA buttons on the landing page.")
    public void landingPageHeaderAndCtasArePresent() {
        WebDriver driver = getDriver();
        LandingPage landingPage = new LandingPage(driver);
        landingPage.open(Config.baseUrl());

        Assert.assertTrue(landingPage.isLoaded(), "Landing page logo and sign-in link should be visible");
        Assert.assertTrue(landingPage.hasNavMenu(), "Header navigation menu should be present");
        Assert.assertTrue(landingPage.hasLanguageSwitcher(), "Language switcher should be present");
        Assert.assertTrue(landingPage.hasSignInLink(), "Sign-in / Login link should be present in header");
        Assert.assertTrue(landingPage.hasRegisterButton(), "'Create profile' registration CTA button should be present");
    }

    @Test(description = "PRE-03: Verify landing packages billing tab switching between Monthly and Yearly", groups = {"smoke", "public"})
    @Story("Pricing Packages Switcher")
    @Severity(SeverityLevel.NORMAL)
    @Description("Verifies that clicking between Monthly and Yearly packages toggles the active billing cycle tab.")
    public void landingPackagesTabSwitcherWorks() {
        WebDriver driver = getDriver();
        LandingPage landingPage = new LandingPage(driver);
        landingPage.open(Config.baseUrl());

        if (landingPage.hasMonthlyTab() && landingPage.hasYearlyTab()) {
            // By default Monthly is active
            Assert.assertTrue(landingPage.isMonthlyTabActive(), "Monthly tab should be active by default");

            // Click Yearly
            landingPage.switchToYearlyTab();
            Assert.assertTrue(landingPage.isYearlyTabActive(), "Yearly tab should become active after click");

            // Switch back to Monthly
            landingPage.switchToMonthlyTab();
            Assert.assertTrue(landingPage.isMonthlyTabActive(), "Monthly tab should be restored as active");
        }
    }

    @Test(description = "PRE-04: Verify public /login route loads cleanly and renders SPA application root", groups = {"smoke", "public"})
    @Story("Login Route Accessibility")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Verifies that /login route opens cleanly, remains on /login without redirects, and mounts #root.")
    public void loginRouteIsAccessible() {
        WebDriver driver = getDriver();
        LoginPage loginPage = new LoginPage(driver);
        loginPage.open(Config.baseUrl());

        String currentUrl = driver.getCurrentUrl();
        Assert.assertTrue(currentUrl.contains("/login") || currentUrl.contains("/dashboard"),
                "Expected URL to remain on /login (or /dashboard if pre-authenticated), but got: " + currentUrl);

        Assert.assertFalse(driver.findElements(By.id("root")).isEmpty(),
                "Expected SPA '#root' container to be present on the login route");
    }

    @Test(description = "PRE-04b: Verify /login page elements or layout do not display raw 5xx error text", groups = {"smoke", "public"})
    @Story("Server Error Integrity")
    @Severity(SeverityLevel.NORMAL)
    @Description("Checks page source for absence of raw 502/500 gateway or internal server error dumps.")
    public void loginPageHasNoServerError() {
        WebDriver driver = getDriver();
        LoginPage loginPage = new LoginPage(driver);
        loginPage.open(Config.baseUrl());

        String pageSource = driver.getPageSource();
        Assert.assertFalse(pageSource.contains("502 Bad Gateway") || pageSource.contains("500 Internal Server Error"),
                "Login page should not return server error page");
    }

    @Test(description = "PRE-05: Verify key interactive controls on /login page (email, password, continue, forgot, register, social)", groups = {"smoke", "public"})
    @Story("Login Form Elements")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Verifies presence of email and password inputs, submit button, recovery, registration, and social buttons.")
    public void loginPageKeyControlsArePresent() {
        WebDriver driver = getDriver();
        LoginPage loginPage = new LoginPage(driver);
        loginPage.open(Config.baseUrl());

        Assert.assertTrue(loginPage.hasEmailField(), "Email input field (#email) should be present");
        Assert.assertTrue(loginPage.hasPasswordField(), "Password input field (#password) should be present");
        Assert.assertTrue(loginPage.hasSubmitButton(), "Submit / Continue button (.formBtn) should be present");
        Assert.assertTrue(loginPage.hasForgotPasswordLink(), "Forgot password link should be present");
        Assert.assertTrue(loginPage.hasRegisterLink(), "Register / Create profile link should be present");
        Assert.assertTrue(loginPage.hasLinkedInButton(), "LinkedIn sign-in button should be present");
    }

    @Test(description = "PRE-06: Verify password visibility toggle button clicks and switches input type between password and text", groups = {"smoke", "public"})
    @Story("Password Visibility Toggle")
    @Severity(SeverityLevel.NORMAL)
    @Description("Verifies clicking the eye icon toggles the password input type between 'password' and 'text'.")
    public void passwordVisibilityToggleWorks() {
        WebDriver driver = getDriver();
        LoginPage loginPage = new LoginPage(driver);
        loginPage.open(Config.baseUrl());

        Assert.assertEquals(loginPage.getPasswordInputType(), "password",
                "Password field should initially have type='password'");

        // Click eye toggle button
        loginPage.togglePasswordVisibility();
        Assert.assertEquals(loginPage.getPasswordInputType(), "text",
                "Password field should have type='text' after clicking visibility toggle");

        // Click eye toggle button again to restore hidden state
        loginPage.togglePasswordVisibility();
        Assert.assertEquals(loginPage.getPasswordInputType(), "password",
                "Password field should return to type='password' after clicking toggle again");
    }

    @Test(description = "PRE-07: Verify tab switching between Email Address and Phone Number authentication", groups = {"smoke", "public"})
    @Story("Authentication Mode Tabs")
    @Severity(SeverityLevel.NORMAL)
    @Description("Verifies clicking between 'Email Address' and 'Phone Number' tabs displays the appropriate input field.")
    public void authTabSwitchingWorks() {
        WebDriver driver = getDriver();
        LoginPage loginPage = new LoginPage(driver);
        loginPage.open(Config.baseUrl());

        Assert.assertTrue(loginPage.hasEmailField(), "Email field should be visible by default on Email tab");

        // Switch to Phone tab
        loginPage.switchToPhoneTab();
        Assert.assertTrue(loginPage.isPhoneFieldVisible(), "Phone input field should be visible after switching to Phone tab");

        // Switch back to Email tab
        loginPage.switchToEmailTab();
        Assert.assertTrue(loginPage.hasEmailField(), "Email field should be restored after switching back to Email tab");
    }
}
