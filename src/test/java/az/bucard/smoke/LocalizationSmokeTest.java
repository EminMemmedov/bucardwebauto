package az.bucard.smoke;

import az.bucard.base.BaseTest;
import az.bucard.config.Config;
import az.bucard.pages.LandingPage;
import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import io.qameta.allure.Story;
import org.openqa.selenium.WebDriver;
import org.testng.Assert;
import org.testng.annotations.Test;

@Epic("Bucard Veb Platforması")
@Feature("Lokalizasiya və Çoxdillilik (AZ / EN / RU)")
public class LocalizationSmokeTest extends BaseTest {

    @Test(description = "DİL-01: Dil seçim menyusunun mövcudluğu və ilkin dil vəziyyəti", groups = {"smoke", "public"})
    @Story("Dil Seçimi Elementi")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Ana səhifədə dil seçim düyməsinin (.selectLang) mövcud olduğunu və ilkin dil göstəricisinin təyin edildiyini yoxlayır.")
    public void languageSwitcherIsPresentAndHasInitialValue() {
        WebDriver driver = getDriver();
        LandingPage landingPage = new LandingPage(driver);
        landingPage.open(Config.baseUrl());

        Assert.assertTrue(landingPage.hasLanguageSwitcher(), "Xederdə dil seçim elementi (.selectLang) görünməlidir");

        String currentLang = landingPage.getCurrentLanguageCode();
        Assert.assertFalse(currentLang.isEmpty(), "İlkin seçilmiş dil kodu boş olmamalıdır");

        attachScreenshot("İlkin Dil Vəziyyəti: " + currentLang);
    }

    @Test(description = "DİL-02: Dili İngilis dilinə (EN) dəyişmə və interfeysin uyğunlaşması", groups = {"smoke", "public"})
    @Story("İngilis Dili (EN)")
    @Severity(SeverityLevel.NORMAL)
    @Description("Dil menyusundan 'English' seçdikdə dil kodunun 'EN' olduğunu və interfeysin ingilis dilinə keçdiyini təsdiqləyir.")
    public void switchLanguageToEnglishWorks() {
        WebDriver driver = getDriver();
        LandingPage landingPage = new LandingPage(driver);
        landingPage.open(Config.baseUrl());

        landingPage.switchLanguage("en");

        String currentLang = landingPage.getCurrentLanguageCode();
        Assert.assertEquals(currentLang, "EN", "Dil İngilis dilinə dəyişildikdən sonra göstərici 'EN' olmalıdır");

        attachScreenshot("İngilis Dili İnterfeysi (EN)");
    }

    @Test(description = "DİL-03: Dili Rus dilinə (RU) dəyişmə və interfeysin uyğunlaşması", groups = {"smoke", "public"})
    @Story("Rus Dili (RU)")
    @Severity(SeverityLevel.NORMAL)
    @Description("Dil menyusundan 'Русский' seçdikdə dil kodunun 'РУ' olduğunu və interfeysin rus dilinə keçdiyini təsdiqləyir.")
    public void switchLanguageToRussianWorks() {
        WebDriver driver = getDriver();
        LandingPage landingPage = new LandingPage(driver);
        landingPage.open(Config.baseUrl());

        landingPage.switchLanguage("ru");

        String currentLang = landingPage.getCurrentLanguageCode();
        Assert.assertEquals(currentLang, "РУ", "Dil Rus dilinə dəyişildikdən sonra göstərici 'РУ' olmalıdır");

        attachScreenshot("Rus Dili İnterfeysi (RU)");
    }

    @Test(description = "DİL-04: Dili Azərbaycan dilinə (AZ) geri qaytarılması", groups = {"smoke", "public"})
    @Story("Azərbaycan Dili (AZ)")
    @Severity(SeverityLevel.NORMAL)
    @Description("Fərqli dildən yenidən 'Azərbaycan -ca' seçdikdə əsas dil kodunun 'AZ' olaraq bərpa olunduğunu yoxlayır.")
    public void switchLanguageBackToAzerbaijaniWorks() {
        WebDriver driver = getDriver();
        LandingPage landingPage = new LandingPage(driver);
        landingPage.open(Config.baseUrl());

        // Əvvəlcə EN-ə, sonra təkrar AZ-a keçid
        landingPage.switchLanguage("en");
        Assert.assertEquals(landingPage.getCurrentLanguageCode(), "EN");
        attachScreenshot("1. İngilis Dilinə Keçid (EN)");

        landingPage.switchLanguage("az");
        String currentLang = landingPage.getCurrentLanguageCode();
        Assert.assertEquals(currentLang, "AZ", "Azərbaycan dilinə qayıtdıqda göstərici 'AZ' olmalıdır");

        attachScreenshot("2. Azərbaycan Dilinə Geri Qayıdış (AZ)");
    }

    @Test(description = "DİL-05: Səhifə yeniləndikdə (Page Reload) seçilmiş dilin saxlanılması", groups = {"smoke", "public"})
    @Story("Dilin Saxlanması (Persistence)")
    @Severity(SeverityLevel.NORMAL)
    @Description("Dil dəyişdirildikdən sonra səhifəni yenilədikdə (F5) seçilmiş dil parametrinin itmədiyini təsdiqləyir.")
    public void languagePreferencePersistsAfterPageReload() {
        WebDriver driver = getDriver();
        LandingPage landingPage = new LandingPage(driver);
        landingPage.open(Config.baseUrl());

        landingPage.switchLanguage("en");
        Assert.assertEquals(landingPage.getCurrentLanguageCode(), "EN");

        driver.navigate().refresh();
        landingPage.isLoaded();

        String langAfterReload = landingPage.getCurrentLanguageCode();
        Assert.assertEquals(langAfterReload, "EN",
                "Səhifə yeniləndikdən sonra seçilmiş 'EN' dili qorunmalıdır, cari: " + langAfterReload);

        attachScreenshot("Səhifə Yeniləndikdən Sonra Dilin Saxlanması Təsdiqi");
    }
}
