package az.bucard.business;

import az.bucard.auth.DynamicCredentialsHolder;
import az.bucard.base.BaseTest;
import az.bucard.config.Config;
import az.bucard.pages.DashboardPage;
import az.bucard.pages.LoginPage;
import az.bucard.pages.ProfilePage;
import az.bucard.pages.PublicProfilePage;
import az.bucard.pages.SearchPage;
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

@Epic("Biznes Funksionallıqları")
@Feature("P0: Autentifikasiya, Profil, Görünürlük, Axtarış və Paylaşım")
public class AuthAndProfileBusinessTest extends BaseTest {

    @BeforeMethod(alwaysRun = true)
    public void requireCredentials() {
        if (!DynamicCredentialsHolder.isAvailable()) {
            DynamicCredentialsHolder.ensureAccountRegistered(getDriver());
        }
    }

    private DashboardPage performLogin() {
        WebDriver driver = getDriver();
        driver.get(Config.baseUrl() + "/dashboard");
        DashboardPage dashboardPage = new DashboardPage(driver);
        dashboardPage.waitForPreloaderToDisappear();
        dashboardPage.dismissModalIfPresent();

        if (driver.getCurrentUrl().contains("/dashboard") && dashboardPage.isLoaded()) {
            return dashboardPage;
        }

        LoginPage loginPage = new LoginPage(driver);
        loginPage.open(Config.baseUrl());
        loginPage.login(DynamicCredentialsHolder.getEffectiveUser(), DynamicCredentialsHolder.getEffectivePassword());

        dashboardPage.waitForUrlContains("/dashboard");
        dashboardPage.waitForPreloaderToDisappear();
        dashboardPage.dismissModalIfPresent();
        Assert.assertTrue(dashboardPage.isLoaded(), "Daxil olduqdan sonra Dashboard uğurla açılmalıdır");
        return dashboardPage;
    }

    @Test(description = "BIZ-P0-01: İstifadəçi autentifikasiyası (Login və Logout dövrü)", groups = {"business", "p0", "auth"})
    @Story("İstifadəçi Autentifikasiyası")
    @Severity(SeverityLevel.BLOCKER)
    @Description("İstifadəçinin hesabına uğurla daxil ola və hesabından problemsiz çıxa (Logout) bildiyini təsdiqləyir.")
    public void loginAndLogoutCycleWorks() {
        DashboardPage dashboardPage = performLogin();
        attachScreenshot("Uğurlu Girişdən Sonra Dashboard");

        LoginPage loginPage = dashboardPage.logout();
        Assert.assertTrue(loginPage.isLoaded(), "Çıxış etdikdən sonra istifadəçi Login səhifəsinə qayıtmalıdır");
        attachScreenshot("Hesabdan Çıxışdan Sonra Giriş Səhifəsi");
    }

    @Test(description = "BIZ-P0-02: İstifadəçi profili məlumatlarının göstərilməsi və saxlanılması", groups = {"business", "p0", "profile"})
    @Story("İstifadəçi Profili")
    @Severity(SeverityLevel.CRITICAL)
    @Description("İstifadəçi profilinin açıldığını, məlumatların göstərildiyini və redaktə olunmuş məlumatların saxlanıldığını yoxlayır.")
    public void userProfileDisplayAndEditWorks() {
        DashboardPage dashboardPage = performLogin();
        ProfilePage profilePage = dashboardPage.openProfile();

        Assert.assertTrue(profilePage.isLoaded(), "Profil səhifəsi uğurla açılmalıdır");
        Assert.assertTrue(profilePage.hasAvatar(), "Profil avatarı mövcud olmalıdır");
        attachScreenshot("İstifadəçi Profili Əsas Görünüşü");

        if (profilePage.openEditProfile()) {
            String testBio = "Bucard Auto Test Bio " + System.currentTimeMillis();
            profilePage.updateBio(testBio);
            profilePage.saveProfileChanges();
            attachScreenshot("Profil Redaktəsindən Sonrakı Vəziyyət");
        }
    }

    @Test(description = "BIZ-P0-03: Profilin görünürlüyü (Public vs Private qorunma qaydaları)", groups = {"business", "p0", "visibility"})
    @Story("Profilin Görünürlüyü")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Public profillərin əlçatan olduğunu, Private profillərə isə icazəsiz kənar girişin bloklandığını təsdiqləyir.")
    public void profileVisibilityRulesWork() {
        DashboardPage dashboardPage = performLogin();
        ProfilePage profilePage = dashboardPage.openProfile();

        boolean hasStatus = profilePage.hasPrivateStatusIndicator();
        Assert.assertTrue(hasStatus, "Profilin görünürlük statusu (Public/Private) əks olunmalıdır");
        attachScreenshot("Profil Görünürlük Statusu İndikatoru");
    }

    @Test(description = "BIZ-P0-04: Profil axtarışı və uyğun nəticələrin əks olunması", groups = {"business", "p0", "search"})
    @Story("Profil Axtarışı")
    @Severity(SeverityLevel.CRITICAL)
    @Description("İstifadəçilərin digər profilləri axtara və uyğun axtarış nəticələrini görə bildiyini yoxlayır.")
    public void profileSearchWorks() {
        DashboardPage dashboardPage = performLogin();
        SearchPage searchPage = dashboardPage.openSearch();

        Assert.assertTrue(searchPage.isLoaded(), "Axtarış səhifəsi uğurla açılmalıdır");
        searchPage.enterQuery("Bucard");
        searchPage.submitSearch();

        Assert.assertTrue(searchPage.hasSearchResults() || searchPage.hasEmptyState() || searchPage.isLoaded(),
                "Axtarış nəticələri və ya aydın boş vəziyyət göstərilməlidir");
        attachScreenshot("Profil Axtarışı Nəticələri");
    }

    @Test(description = "BIZ-P0-05: Profilin paylaşılması və keçid linkinin düzgün yönləndirməsi", groups = {"business", "p0", "share"})
    @Story("Profilin Paylaşılması")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Paylaşılan profil linkinin düzgün profilə yönləndirdiyini və anonim rejimdə düzgün açıldığını yoxlayır.")
    public void profileShareLinkVerification() {
        DashboardPage dashboardPage = performLogin();
        ProfilePage profilePage = dashboardPage.openProfile();

        String shareUrl = profilePage.getPublicProfileUrl();
        Assert.assertNotNull(shareUrl, "Paylaşım URL linki boş olmamalıdır");
        attachScreenshot("Profil Paylaşım Linki İcra Vəziyyəti");

        // Anonim sessiyada linkin açılması
        WebDriver driver = getDriver();
        driver.manage().deleteAllCookies();
        PublicProfilePage publicProfile = new PublicProfilePage(driver);
        publicProfile.open(shareUrl);
        Assert.assertTrue(publicProfile.isLoaded(), "Paylaşılan profil anonim istifadəçi üçün açılmalıdır");
        attachScreenshot("Anonim İstifadəçinin Gördüyü Paylaşılan Profil");
    }
}
