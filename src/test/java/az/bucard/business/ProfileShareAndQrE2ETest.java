package az.bucard.business;

import az.bucard.auth.DynamicCredentialsHolder;
import az.bucard.base.BaseTest;
import az.bucard.config.Config;
import az.bucard.driver.DriverFactory;
import az.bucard.email.EmailAccount;
import az.bucard.email.GuerrillaMailClient;
import az.bucard.email.TempEmailService;
import az.bucard.pages.BoxDetailsPage;
import az.bucard.pages.DashboardPage;
import az.bucard.pages.LoginPage;
import az.bucard.pages.PublicProfilePage;
import az.bucard.pages.RegisterPage;
import az.bucard.utils.QrCodeDecoder;
import io.qameta.allure.Allure;
import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import io.qameta.allure.Story;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;
import org.testng.SkipException;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.ByteArrayInputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;

/**
 * Web E2E scenarios for Shared Profile & QR Code Add-to-Box flows (E2E-009 — E2E-012).
 * Each test case verifies both Link and QR Code entry points with a fresh B user account.
 */
@Epic("Biznes Funksionallıqları")
@Feature("Paylaşım və QR E2E (E2E-009 — E2E-012)")
public class ProfileShareAndQrE2ETest extends BaseTest {

    private enum EntryMode {
        LINK("Link"),
        QR("QR");

        private final String label;

        EntryMode(String label) {
            this.label = label;
        }
    }

    private boolean proTargetReady = false;
    private String targetShareUrl;
    private String targetQrUrl;
    private String targetUsername;
    private String targetFullName;

    @BeforeClass(alwaysRun = true)
    public void resolvePublicProAccountA() {
        if (!Config.isProShareTargetAvailable()) {
            proTargetReady = false;
            return;
        }

        targetUsername = trimToEmpty(Config.proUsername());
        targetFullName = trimToEmpty(Config.proFullName());
        targetShareUrl = trimToEmpty(Config.proShareUrl());

        if (Config.isProLoginAvailable()) {
            WebDriver setupDriver = DriverFactory.createDriver();
            try {
                LoginPage loginPage = new LoginPage(setupDriver);
                loginPage.open(Config.baseUrl());
                loginPage.login(Config.proUser(), Config.proPassword());
                DashboardPage dashboard = new DashboardPage(setupDriver);
                dashboard.waitForUrlContains("/dashboard");
                Assert.assertTrue(dashboard.isLoaded(), "Public Pro A hesabı ilə Dashboard açılmalıdır");

                DashboardPage.ShareProfileInfo shareInfo = dashboard.openShareModalAndExtractInfo();
                if (!shareInfo.shareLink().isBlank()) {
                    targetShareUrl = shareInfo.shareLink();
                }
                if (targetUsername.isBlank() && !shareInfo.username().isBlank()) {
                    targetUsername = shareInfo.username();
                }

                if (!shareInfo.qrImageUrl().isBlank()) {
                    try {
                        targetQrUrl = QrCodeDecoder.decodeFromImageUrl(shareInfo.qrImageUrl());
                        Allure.addAttachment("QR Dekoder Məlumatı", "text/plain",
                                "QR image URL: " + shareInfo.qrImageUrl() + "\nDecoded target URL: " + targetQrUrl);
                    } catch (Exception e) {
                        Allure.addAttachment("QR Dekoder Xəbərdarlığı", "text/plain",
                                "Canlı QR şəkli dekod edilə bilmədi (" + e.getMessage() + "), paylaşım linki istifadə ediləcək.");
                    }
                }
            } finally {
                DriverFactory.quitDriver(setupDriver);
            }
        }

        if ((targetQrUrl == null || targetQrUrl.isBlank())
                && Config.proQrImageUrl() != null && !Config.proQrImageUrl().isBlank()) {
            try {
                targetQrUrl = QrCodeDecoder.decodeFromImageUrl(Config.proQrImageUrl());
                Allure.addAttachment("Konfiqurasiya QR Dekoderi", "text/plain",
                        "QR image URL: " + Config.proQrImageUrl() + "\nDecoded target URL: " + targetQrUrl);
            } catch (Exception ignored) {
            }
        }

        if (targetQrUrl == null || targetQrUrl.isBlank()) {
            targetQrUrl = targetShareUrl;
            if (targetShareUrl != null && !targetShareUrl.isBlank()) {
                Allure.addAttachment("QR Giriş Rejimi", "text/plain",
                        "Ayrı QR şəkil URL-i verilmədiyi üçün konfiqurasiya edilmiş A paylaşım URL-i istifadə edildi: " + targetQrUrl);
            }
        }

        if (targetUsername.isBlank() && !targetShareUrl.isBlank()) {
            targetUsername = extractUsernameFromUrl(targetShareUrl);
        }

        proTargetReady = !targetShareUrl.isBlank();
    }

    private void requireProAccount() {
        if (!proTargetReady) {
            throw new SkipException(
                    "Public Pro hesab A (BUCARD_PRO_USERNAME / BUCARD_PRO_SHARE_URL və ya BUCARD_PRO_USER + BUCARD_PRO_PASSWORD) konfiqurasiya edilməyib.");
        }
    }

    private String resolveEntryUrl(EntryMode mode) {
        return mode == EntryMode.QR ? targetQrUrl : targetShareUrl;
    }

    private EmailAccount createFreshUserBEmail() {
        TempEmailService emailService = new GuerrillaMailClient();
        EmailAccount account = emailService.createAccount();
        Assert.assertNotNull(account, "Yeni B istifadəçisi üçün müvəqqəti e-poçt yaradılmalıdır");
        Assert.assertTrue(account.isValid(), "B istifadəçisinin e-poçt ünvanı etibarlı olmalıdır");
        return account;
    }

    private void registerUserBFromCurrentRegisterPage(WebDriver driver, String email, String password) {
        RegisterPage registerPage = new RegisterPage(driver);
        Assert.assertTrue(registerPage.isLoaded(), "Qeydiyyat forması görünən olmalıdır");
        registerPage.fillCredentials(email, password);
        registerPage.submit();
    }

    private void waitForReturnToProfileA(WebDriver driver) {
        WebDriverWait longWait = new WebDriverWait(driver, Duration.ofSeconds(20));
        try {
            longWait.until(d -> {
                String url = d.getCurrentUrl();
                return url != null && url.contains("/profile/");
            });
        } catch (Exception ignored) {
        }

        String currentUrl = driver.getCurrentUrl();
        Assert.assertFalse(currentUrl.contains("/dashboard"),
                "B qeydiyyatdan sonra öz Dashboard-una deyil, A-nın profilinə yönləndirilməlidir. Cari URL: " + currentUrl);
        Assert.assertTrue(currentUrl.contains("/profile/"),
                "Qeydiyyatdan sonra yenidən A-nın profilinə (/profile/:username) qayıdılmalıdır. Cari URL: " + currentUrl);

        PublicProfilePage profilePage = new PublicProfilePage(driver);
        profilePage.waitForProfileReady();
        Assert.assertTrue(profilePage.isProfileOf(targetUsername, targetFullName),
                "Qeydiyyatdan sonra açılan profil A-ya aid olmalıdır (" + targetUsername + " / " + targetFullName + "). Cari URL: " + currentUrl);
    }

    private String addProfileAToBoxAndVerify(WebDriver driver, String userBEmail, String stepPrefix) {
        PublicProfilePage profilePage = new PublicProfilePage(driver);
        Assert.assertTrue(profilePage.hasAddToBoxButton(),
                stepPrefix + ": A-nın profilində '+ Add to Box' düyməsi görünməlidir");
        profilePage.clickAddToBox();

        Assert.assertTrue(profilePage.isAddToBoxModalOpen(),
                stepPrefix + ": Qutu seçimi modalı açılmalıdır");
        attachDriverScreenshot(driver, stepPrefix + " — Qutu seçimi modalı");

        String selectedBoxName = profilePage.selectFirstAvailableBoxAndConfirm();
        String boxDetailsUrl = driver.getCurrentUrl();
        Assert.assertTrue(boxDetailsUrl.contains("/box-details/"),
                stepPrefix + ": Qutuya əlavə etdikdən sonra /box-details/ səhifəsinə keçid olmalıdır");

        // Seçilmiş qutunu yenidən aç və saxlanmış profilin A-ya aid olduğunu təsdiqlə
        driver.get(boxDetailsUrl);
        BoxDetailsPage boxDetailsPage = new BoxDetailsPage(driver);
        Assert.assertTrue(boxDetailsPage.isLoaded(),
                stepPrefix + ": Seçilmiş qutu səhifəsi yenidən yüklənməlidir (" + selectedBoxName + ")");
        Assert.assertTrue(boxDetailsPage.containsProfile(targetFullName, targetUsername),
                stepPrefix + ": Qutuda A-nın profili (" + targetUsername + " / " + targetFullName + ") tapılmalıdır");
        Assert.assertFalse(boxDetailsPage.containsOnlyUser(userBEmail),
                stepPrefix + ": Qutuda A-nın profili B-nin öz profili ilə əvəzlənməməlidir");
        attachDriverScreenshot(driver, stepPrefix + " — Qutu daxilində profil A");
        return boxDetailsUrl;
    }

    private void verifyInitialEntryStillAccessible(WebDriver driver, String entryUrl, String stepPrefix) {
        PublicProfilePage publicProfile = new PublicProfilePage(driver);
        publicProfile.open(entryUrl);
        publicProfile.waitForProfileReady();
        Assert.assertTrue(publicProfile.isProfileOf(targetUsername, targetFullName),
                stepPrefix + ": İlkin paylaşım linki/QR açıldıqda A-nın profili hələ də əlçatan olmalıdır");
        attachDriverScreenshot(driver, stepPrefix + " — İlkin linkdən təkrar yoxlama");
    }

    // -------------------------------------------------------------------------
    // E2E-009: Mövcud (daxil olmuş) istifadəçi paylaşılan profili qutuya əlavə edir
    // -------------------------------------------------------------------------

    private void runE2e009Flow(EntryMode mode) {
        requireProAccount();
        WebDriver driver = getDriver();
        EmailAccount userB = createFreshUserBEmail();

        // 1. Təzə B hesabı qeydiyyatdan keçir və sistemdə daxil olmuş vəziyyətdədir
        RegisterPage registerPage = new RegisterPage(driver);
        registerPage.open(Config.baseUrl());
        DashboardPage dashboard = registerPage.registerNewUser(userB.address(), DynamicCredentialsHolder.DEFAULT_PASSWORD);
        Assert.assertTrue(dashboard.isLoaded(), "B hesabı qeydiyyatdan sonra Dashboard açılmalıdır");

        // 2. B, A-nın Link və ya QR URL-ini açır
        String entryUrl = resolveEntryUrl(mode);
        PublicProfilePage publicProfile = new PublicProfilePage(driver);
        publicProfile.open(entryUrl);
        publicProfile.waitForProfileReady();
        attachDriverScreenshot(driver, "E2E-009 [" + mode.label + "] — Açılan Profil A");

        // 3. Username və ad/soyadı A ilə müqayisə et
        Assert.assertTrue(publicProfile.isProfileOf(targetUsername, targetFullName),
                "Açılan profilin username və ad/soyadı A-ya uyğun gəlməlidir");

        // 4-5. Add to Box seç, qutunu seç, təsdiqlə və qutunu yenidən açıb yoxla
        addProfileAToBoxAndVerify(driver, userB.address(), "E2E-009 [" + mode.label + "]");
    }

    // -------------------------------------------------------------------------
    // E2E-010: Yeni istifadəçi qeydiyyatdan sonra paylaşılan profilə qayıdır
    // -------------------------------------------------------------------------

    private void runE2e010Flow(EntryMode mode) {
        requireProAccount();
        WebDriver driver = getDriver();
        EmailAccount userB = createFreshUserBEmail();
        String entryUrl = resolveEntryUrl(mode);

        // 1-2. Hesabı olmayan B, A-nın Link/QR URL-ini açır
        PublicProfilePage publicProfile = new PublicProfilePage(driver);
        publicProfile.open(entryUrl);
        publicProfile.waitForProfileReady();
        Assert.assertTrue(publicProfile.isProfileOf(targetUsername, targetFullName),
                "Anonim B əvvəlcə A-nın profilini görməlidir");
        attachDriverScreenshot(driver, "E2E-010 [" + mode.label + "] — Anonim baxış");

        // 3. Add to Box seçir və /register səhifəsinə yönləndirilir
        Assert.assertTrue(publicProfile.hasAddToBoxButton(), "'Add to Box' düyməsi görünməlidir");
        publicProfile.clickAddToBox();

        RegisterPage registerPage = new RegisterPage(driver);
        Assert.assertTrue(registerPage.waitForUrlContains("/register") && registerPage.isLoaded(),
                "Giriş etməmiş B 'Add to Box' seçdikdə /register səhifəsinə keçməlidir");

        // 4. Qeydiyyatı tamamla
        registerUserBFromCurrentRegisterPage(driver, userB.address(), DynamicCredentialsHolder.DEFAULT_PASSWORD);

        // 5. Linki təkrar açmadan A-nın profilinə qayıdışı yoxla
        waitForReturnToProfileA(driver);
        attachDriverScreenshot(driver, "E2E-010 [" + mode.label + "] — Qeydiyyatdan sonra A-ya qayıdış");

        // 6-7. Add to Box tamamla və qutunu yenidən açıb A-nı yoxla
        addProfileAToBoxAndVerify(driver, userB.address(), "E2E-010 [" + mode.label + "]");

        // 8. İlkin paylaşım linkini yenidən aç
        verifyInitialEntryStillAccessible(driver, entryUrl, "E2E-010 [" + mode.label + "]");
    }

    // -------------------------------------------------------------------------
    // E2E-011: Qeydiyyatda səhifə yenilənəndə (Refresh) paylaşılan profil itmir
    // -------------------------------------------------------------------------

    private void runE2e011Flow(EntryMode mode) {
        requireProAccount();
        WebDriver driver = getDriver();
        EmailAccount userB = createFreshUserBEmail();
        String entryUrl = resolveEntryUrl(mode);

        // 1. Anonim B paylaşım URL-ini açır və Add to Box ilə qeydiyyata keçir
        PublicProfilePage publicProfile = new PublicProfilePage(driver);
        publicProfile.open(entryUrl);
        publicProfile.waitForProfileReady();
        Assert.assertTrue(publicProfile.isProfileOf(targetUsername, targetFullName),
                "Anonim B əvvəlcə A-nın profilini görməlidir");
        publicProfile.clickAddToBox();

        RegisterPage registerPage = new RegisterPage(driver);
        Assert.assertTrue(registerPage.waitForUrlContains("/register") && registerPage.isLoaded(),
                "/register səhifəsi açılmalıdır");

        // 2. Email və şifrəni daxil et, göndərmədən səhifəni yenilə
        registerPage.fillCredentials(userB.address(), DynamicCredentialsHolder.DEFAULT_PASSWORD);
        attachDriverScreenshot(driver, "E2E-011 [" + mode.label + "] — Refresh öncəsi");
        driver.navigate().refresh();

        // 3. Refresh sonrası formanı yenidən doldur və qeydiyyatı tamamla
        RegisterPage refreshedRegisterPage = new RegisterPage(driver);
        Assert.assertTrue(refreshedRegisterPage.isLoaded(), "Refresh sonrası qeydiyyat forması bərpa olunmalıdır");
        registerUserBFromCurrentRegisterPage(driver, userB.address(), DynamicCredentialsHolder.DEFAULT_PASSWORD);

        // 4. Linki təkrar açmadan A-nın profilinə qayıdışı yoxla
        waitForReturnToProfileA(driver);
        attachDriverScreenshot(driver, "E2E-011 [" + mode.label + "] — Refresh sonrası A-ya qayıdış");

        // 5-7. Add to Box tamamla, qutuda yoxla və ilkin linki yoxla
        addProfileAToBoxAndVerify(driver, userB.address(), "E2E-011 [" + mode.label + "]");
        verifyInitialEntryStillAccessible(driver, entryUrl, "E2E-011 [" + mode.label + "]");
    }

    // -------------------------------------------------------------------------
    // E2E-012: Brauzer tam bağlanıb açıldıqda paylaşılan profil itmir
    // -------------------------------------------------------------------------

    private void runE2e012Flow(EntryMode mode) {
        requireProAccount();
        // BaseTest-in standart sürücüsünü azad edirik ki, təcrid olunmuş sabit Chrome profilindən istifadə edək
        DriverFactory.quitDriver(getDriver());

        EmailAccount userB = createFreshUserBEmail();
        String entryUrl = resolveEntryUrl(mode);
        Path profileDir = null;
        WebDriver profileDriver = null;

        try {
            profileDir = Files.createTempDirectory("bucard-e2e012-" + mode.name().toLowerCase() + "-");

            // 1. Eyni normal brauzer profilində A-nın profilini aç və Add to Box ilə qeydiyyata keç
            profileDriver = DriverFactory.createChromeWithProfile(profileDir);
            PublicProfilePage publicProfile = new PublicProfilePage(profileDriver);
            publicProfile.open(entryUrl);
            publicProfile.waitForProfileReady();
            Assert.assertTrue(publicProfile.isProfileOf(targetUsername, targetFullName),
                    "Anonim B əvvəlcə A-nın profilini görməlidir");
            publicProfile.clickAddToBox();

            RegisterPage registerPage = new RegisterPage(profileDriver);
            Assert.assertTrue(registerPage.waitForUrlContains("/register") && registerPage.isLoaded(),
                    "/register səhifəsi açılmalıdır");
            String registerUrl = profileDriver.getCurrentUrl();

            // 2. Sahələri doldur və qeydiyyatı tamamlamadan brauzeri tam bağla
            registerPage.fillCredentials(userB.address(), DynamicCredentialsHolder.DEFAULT_PASSWORD);
            attachDriverScreenshot(profileDriver, "E2E-012 [" + mode.label + "] — Brauzer bağlanmazdan əvvəl");
            DriverFactory.quitDriver(profileDriver);
            profileDriver = null;

            // 3. Eyni brauzer profilini yenidən aç və /register səhifəsini bərpa et (A-nın linkini açma!)
            profileDriver = DriverFactory.createChromeWithProfile(profileDir);
            profileDriver.get(registerUrl);
            RegisterPage restoredRegisterPage = new RegisterPage(profileDriver);
            Assert.assertTrue(restoredRegisterPage.isLoaded(),
                    "Yenidən açılmış brauzerdə qeydiyyat səhifəsi bərpa olunmalıdır");

            // 4. Sahələri doldur və qeydiyyatı tamamla
            registerUserBFromCurrentRegisterPage(profileDriver, userB.address(), DynamicCredentialsHolder.DEFAULT_PASSWORD);

            // 5. A-nın profilinə qayıdışı yoxla
            waitForReturnToProfileA(profileDriver);
            attachDriverScreenshot(profileDriver, "E2E-012 [" + mode.label + "] — Brauzer bərpasından sonra A-ya qayıdış");

            // 6-8. Add to Box tamamla, qutuda yoxla və ilkin linki yoxla
            addProfileAToBoxAndVerify(profileDriver, userB.address(), "E2E-012 [" + mode.label + "]");
            verifyInitialEntryStillAccessible(profileDriver, entryUrl, "E2E-012 [" + mode.label + "]");
        } catch (Throwable t) {
            if (profileDriver != null) {
                attachDriverScreenshot(profileDriver, "E2E-012 [" + mode.label + "] — Xəta anı skrinşotu");
            }
            if (t instanceof RuntimeException re) {
                throw re;
            }
            if (t instanceof Error err) {
                throw err;
            }
            throw new RuntimeException(t);
        } finally {
            DriverFactory.quitDriver(profileDriver);
            DriverFactory.deleteProfileDirQuietly(profileDir);
        }
    }

    private void attachDriverScreenshot(WebDriver driver, String title) {
        if (driver instanceof TakesScreenshot ts) {
            try {
                byte[] bytes = ts.getScreenshotAs(OutputType.BYTES);
                if (bytes != null && bytes.length > 0) {
                    Allure.addAttachment(title, "image/png", new ByteArrayInputStream(bytes), ".png");
                }
            } catch (Exception ignored) {
            }
        }
    }

    private static String extractUsernameFromUrl(String url) {
        if (url == null || url.isBlank()) {
            return "";
        }
        String clean = url.split("[?#]")[0].replaceAll("/+$", "");
        int idx = clean.lastIndexOf('/');
        return idx >= 0 && idx < clean.length() - 1 ? clean.substring(idx + 1).trim() : "";
    }

    private static String trimToEmpty(String value) {
        return value == null ? "" : value.trim();
    }

    // -------------------------------------------------------------------------
    // TestNG Test Methods (8 tests: Link + QR per requirement)
    // -------------------------------------------------------------------------

    @Test(description = "E2E-009 [Link]: Mövcud istifadəçi paylaşılan profili qutuya əlavə edir", groups = {"business", "p1", "share-e2e"})
    @Story("E2E-009: Daxil olmuş istifadəçinin profili qutuya əlavə etməsi")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Təzə daxil olmuş B istifadəçisi A-nın paylaşım linkini açır, məlumatları müqayisə edir və A-nı qutuya əlavə edir.")
    public void e2e009_loggedInUserAddsSharedProfileToBox_viaLink() {
        runE2e009Flow(EntryMode.LINK);
    }

    @Test(description = "E2E-009 [QR]: Mövcud istifadəçi QR kod ilə paylaşılan profili qutuya əlavə edir", groups = {"business", "p1", "share-e2e"})
    @Story("E2E-009: Daxil olmuş istifadəçinin profili qutuya əlavə etməsi")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Təzə daxil olmuş B istifadəçisi A-nın dekod olunmuş QR URL-ini açır, məlumatları müqayisə edir və A-nı qutuya əlavə edir.")
    public void e2e009_loggedInUserAddsSharedProfileToBox_viaQr() {
        runE2e009Flow(EntryMode.QR);
    }

    @Test(description = "E2E-010 [Link]: Yeni istifadəçi qeydiyyatdan sonra paylaşılan profilə qayıdır", groups = {"business", "p1", "share-e2e"})
    @Story("E2E-010: Qeydiyyatdan sonra paylaşılan profilə qayıdış")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Anonim B paylaşım linkindən 'Add to Box' seçərək qeydiyyatdan keçir və avtomatik A-nın profilinə qayıdıb qutuya əlavə edir.")
    public void e2e010_newUserReturnsToSharedProfileAfterRegister_viaLink() {
        runE2e010Flow(EntryMode.LINK);
    }

    @Test(description = "E2E-010 [QR]: Yeni istifadəçi QR koddan qeydiyyatdan sonra paylaşılan profilə qayıdır", groups = {"business", "p1", "share-e2e"})
    @Story("E2E-010: Qeydiyyatdan sonra paylaşılan profilə qayıdış")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Anonim B QR URL-dən 'Add to Box' seçərək qeydiyyatdan keçir və avtomatik A-nın profilinə qayıdıb qutuya əlavə edir.")
    public void e2e010_newUserReturnsToSharedProfileAfterRegister_viaQr() {
        runE2e010Flow(EntryMode.QR);
    }

    @Test(description = "E2E-011 [Link]: Qeydiyyatda səhifə yenilənəndə paylaşılan profil itmir", groups = {"business", "p1", "share-e2e"})
    @Story("E2E-011: Səhifə yeniləndikdə paylaşım kontekstinin qorunması")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Qeydiyyat zamanı səhifəni yenilədikdən (Refresh) sonra B qeydiyyatı tamamlayır və A-nın profilinə qayıdır.")
    public void e2e011_registerRefreshPreservesSharedProfile_viaLink() {
        runE2e011Flow(EntryMode.LINK);
    }

    @Test(description = "E2E-011 [QR]: QR koddan qeydiyyatda səhifə yenilənəndə paylaşılan profil itmir", groups = {"business", "p1", "share-e2e"})
    @Story("E2E-011: Səhifə yeniləndikdə paylaşım kontekstinin qorunması")
    @Severity(SeverityLevel.CRITICAL)
    @Description("QR URL-dən başlanan qeydiyyatda səhifəni yenilədikdən (Refresh) sonra B qeydiyyatı tamamlayır və A-nın profilinə qayıdır.")
    public void e2e011_registerRefreshPreservesSharedProfile_viaQr() {
        runE2e011Flow(EntryMode.QR);
    }

    @Test(description = "E2E-012 [Link]: Brauzer yenidən açılanda paylaşılan profil itmir", groups = {"business", "p1", "share-e2e"})
    @Story("E2E-012: Brauzer bərpasında paylaşım kontekstinin qorunması")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Qeydiyyat tamamlanmadan brauzer bağlanıb eyni profillə yenidən açıldıqdan sonra B qeydiyyatı tamamlayır və A-nın profilinə qayıdır.")
    public void e2e012_browserRestartPreservesSharedProfile_viaLink() {
        runE2e012Flow(EntryMode.LINK);
    }

    @Test(description = "E2E-012 [QR]: QR koddan brauzer yenidən açılanda paylaşılan profil itmir", groups = {"business", "p1", "share-e2e"})
    @Story("E2E-012: Brauzer bərpasında paylaşım kontekstinin qorunması")
    @Severity(SeverityLevel.CRITICAL)
    @Description("QR URL-dən qeydiyyat zamanı brauzer bağlanıb eyni profillə yenidən açıldıqdan sonra B qeydiyyatı tamamlayır və A-nın profilinə qayıdır.")
    public void e2e012_browserRestartPreservesSharedProfile_viaQr() {
        runE2e012Flow(EntryMode.QR);
    }
}
