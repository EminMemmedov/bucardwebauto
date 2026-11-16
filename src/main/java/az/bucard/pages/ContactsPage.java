package az.bucard.pages;

import io.qameta.allure.Step;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.util.List;

public final class ContactsPage extends BasePage {
    private static final By ADD_CONTACT_BTN = By.xpath(
            "//button[contains(normalize-space(), 'Add Contact') or contains(normalize-space(), 'Kontakt əlavə et') or contains(@class, 'addContact')] | //*[contains(@class, 'addBtn')]");
    private static final By CONTACT_NAME_INPUT = By.cssSelector(
            "input[name='fullName'], input[name='name'], input[placeholder*='Name'], input[placeholder*='Ad']");
    private static final By CONTACT_PHONE_INPUT = By.cssSelector(
            "input[name='phone'], input[type='tel'], input[placeholder*='Phone'], input[placeholder*='Telefon']");
    private static final By CONTACT_EMAIL_INPUT = By.cssSelector(
            "input[name='email'], input[type='email'], input[placeholder*='Email'], input[placeholder*='E-poçt']");
    private static final By SUBMIT_CONTACT_BTN = By.xpath(
            "//button[contains(normalize-space(), 'Save') or contains(normalize-space(), 'Yadda saxla') or contains(normalize-space(), 'Əlavə et') or contains(@class, 'submitBtn')]");
    private static final By CONTACT_CARDS = By.cssSelector(
            ".contact-card, .contact-item, div[class*='contactCard'], div[class*='contactRow']");
    private static final By DELETE_CONTACT_BTN = By.xpath(
            "//button[contains(normalize-space(), 'Delete') or contains(normalize-space(), 'Sil')] | //*[contains(@class, 'deleteContact')]");

    public ContactsPage(WebDriver driver) {
        super(driver);
    }

    public boolean isLoaded() {
        return isElementVisible(ADD_CONTACT_BTN) || isElementVisible(CONTACT_CARDS);
    }

    public boolean hasAddContactButton() {
        return isElementPresent(ADD_CONTACT_BTN);
    }

    public int getContactsCount() {
        return driver.findElements(CONTACT_CARDS).size();
    }

    @Step("Yeni kontakt əlavə et: '{fullName}', '{phone}', '{email}'")
    public boolean addContact(String fullName, String phone, String email) {
        if (!isElementPresent(ADD_CONTACT_BTN)) {
            return false;
        }
        click(ADD_CONTACT_BTN);

        if (isElementPresent(CONTACT_NAME_INPUT)) {
            type(CONTACT_NAME_INPUT, fullName);
        }
        if (phone != null && isElementPresent(CONTACT_PHONE_INPUT)) {
            type(CONTACT_PHONE_INPUT, phone);
        }
        if (email != null && isElementPresent(CONTACT_EMAIL_INPUT)) {
            type(CONTACT_EMAIL_INPUT, email);
        }

        if (isElementPresent(SUBMIT_CONTACT_BTN)) {
            click(SUBMIT_CONTACT_BTN);
            try {
                Thread.sleep(1500);
            } catch (InterruptedException ignored) {}
            return true;
        }
        return false;
    }

    @Step("Kontaktın mövcudluğunu yoxla: '{name}'")
    public boolean hasContact(String name) {
        List<WebElement> cards = driver.findElements(CONTACT_CARDS);
        for (WebElement card : cards) {
            try {
                if (card.getText().contains(name)) {
                    return true;
                }
            } catch (Exception ignored) {}
        }
        return false;
    }

    @Step("Kontaktı sil: '{name}'")
    public boolean deleteContact(String name) {
        List<WebElement> cards = driver.findElements(CONTACT_CARDS);
        for (WebElement card : cards) {
            try {
                if (card.getText().contains(name)) {
                    List<WebElement> deleteButtons = card.findElements(DELETE_CONTACT_BTN);
                    if (!deleteButtons.isEmpty()) {
                        deleteButtons.get(0).click();
                        Thread.sleep(1000);
                        return true;
                    }
                }
            } catch (Exception ignored) {}
        }
        return false;
    }
}
