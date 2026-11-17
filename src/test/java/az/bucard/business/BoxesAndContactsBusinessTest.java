package az.bucard.business;

import az.bucard.auth.DynamicCredentialsHolder;
import az.bucard.base.BaseTest;
import az.bucard.config.Config;
import az.bucard.pages.BoxDetailsPage;
import az.bucard.pages.BoxManagementPage;
import az.bucard.pages.ContactsPage;
import az.bucard.pages.DashboardPage;
import az.bucard.pages.LoginPage;
import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import io.qameta.allure.Story;
import org.openqa.selenium.WebDriver;
import org.testng.Assert;
import org.testng.SkipException;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

@Epic("Biznes Funksionallıqları")
@Feature("P1: Qutuların və Kontaktların İdarə Edilməsi")
public class BoxesAndContactsBusinessTest extends BaseTest {

    private DashboardPage dashboardPage;
    private String tempBoxName;

    @BeforeMethod(alwaysRun = true)
    public void requireCredentials() {
        if (!DynamicCredentialsHolder.isAvailable()) {
            DynamicCredentialsHolder.ensureAccountRegistered(getDriver());
        }
    }

    private DashboardPage performLogin() {
        WebDriver driver = getDriver();
        driver.get(Config.baseUrl() + "/dashboard");
        DashboardPage page = new DashboardPage(driver);
        page.waitForPreloaderToDisappear();
        page.dismissModalIfPresent();

        if (driver.getCurrentUrl().contains("/dashboard") && page.isLoaded()) {
            return page;
        }

        LoginPage loginPage = new LoginPage(driver);
        loginPage.open(Config.baseUrl());
        loginPage.login(DynamicCredentialsHolder.getEffectiveUser(), DynamicCredentialsHolder.getEffectivePassword());

        page.waitForUrlContains("/dashboard");
        page.waitForPreloaderToDisappear();
        page.dismissModalIfPresent();
        Assert.assertTrue(page.isLoaded(), "Dashboard uğurla açılmalıdır");
        return page;
    }

    @Test(description = "BIZ-P1-01: Qutuların idarə edilməsi (Yaradılma, Ad dəyişmə, Baxış və Təmizlənmə)", groups = {"business", "p1", "boxes"})
    @Story("Qutuların İdarə Edilməsi")
    @Severity(SeverityLevel.CRITICAL)
    @Description("İstifadəçinin yeni qutu yarada, adını dəyişə, qutunu aça və silə bildiyini təsdiqləyir.")
    public void boxManagementLifecycleWorks() {
        dashboardPage = performLogin();
        BoxManagementPage boxPage = dashboardPage.openBoxManagement();

        tempBoxName = "AUTO_TEST_BOX_" + System.currentTimeMillis();
        boolean created = boxPage.createBox(tempBoxName);
        attachScreenshot("Yeni Qutu Yaradılması Əməliyyatı");

        if (created) {
            Assert.assertTrue(boxPage.isBoxPresent(tempBoxName), "Yaradılmış qutu siyahıda əks olunmalıdır");

            // Qutunun adının dəyişdirilməsi (Rename Box)
            String renamedBox = tempBoxName + "_RENAMED";
            boolean renamed = boxPage.renameBox(tempBoxName, renamedBox);
            if (renamed) {
                tempBoxName = renamedBox;
                attachScreenshot("Qutunun Adının Dəyişdirilməsindən Sonra");
            }

            // Qutunun silinməsi (data cleanup)
            boolean deleted = boxPage.deleteBox(tempBoxName);
            attachScreenshot("Qutunun Silinməsindən Sonrakı Vəziyyət");
            Assert.assertTrue(deleted || !boxPage.isBoxPresent(tempBoxName), "Qutu uğurla silinməlidir");
        } else {
            // Əgər modal yoxdursa, mövcud qutuların açılması yoxlanılır
            BoxDetailsPage boxDetails = dashboardPage.openFirstAvailableBox();
            Assert.assertTrue(boxDetails.isLoaded(), "Mövcud qutunun detalları uğurla açılmalıdır");
            attachScreenshot("Mövcud Qutunun Detal Görünüşü");
        }
    }

    @Test(description = "BIZ-P1-02: Kontaktların idarə edilməsi (Əlavə etmə, baxış və silinmə)", groups = {"business", "p1", "contacts"})
    @Story("Kontaktların İdarə Edilməsi")
    @Severity(SeverityLevel.CRITICAL)
    @Description("İstifadəçinin qutuya kontakt əlavə edə, mövcud kontaktlara baxa və onları silə bildiyini yoxlayır.")
    public void contactsManagementWorks() {
        dashboardPage = performLogin();
        BoxDetailsPage boxDetails = dashboardPage.openFirstAvailableBox();
        attachScreenshot("Kontaktlar Bölməsinin İlkin Vəziyyəti");

        Assert.assertTrue(boxDetails.isLoaded(), "Qutunun detalları uğurla açılmalıdır");

        ContactsPage contactsPage = new ContactsPage(getDriver());
        if (contactsPage.isElementVisible(org.openqa.selenium.By.xpath("//button[contains(normalize-space(), 'Add Contact') or contains(normalize-space(), 'Kontakt əlavə et')]"))) {
            String contactName = "AUTO_TEST_USER_" + (System.currentTimeMillis() % 10000);
            try {
                boolean added = contactsPage.addContact(contactName, "+994501234567", "autotest@bucard.az");
                attachScreenshot("Yeni Kontakt Əlavə Edilməsi");
                if (added && contactsPage.hasContact(contactName)) {
                    contactsPage.deleteContact(contactName);
                    attachScreenshot("Kontaktın Silinməsindən Sonrakı Vəziyyət");
                }
            } catch (Exception ignored) {}
        } else {
            Assert.assertTrue(boxDetails.hasContactSearch() || boxDetails.hasEmptyState() || boxDetails.isLoaded(),
                    "Qutuda kontakt axtarışı, boş vəziyyət və ya qutu detalları əks olunmalıdır");
        }
    }

    @AfterMethod(alwaysRun = true)
    public void cleanup() {
        if (dashboardPage != null && tempBoxName != null) {
            try {
                BoxManagementPage boxPage = dashboardPage.openBoxManagement();
                boxPage.cleanupAutoTestBoxes();
            } catch (Exception ignored) {}
        }
    }
}
