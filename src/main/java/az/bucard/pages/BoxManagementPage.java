package az.bucard.pages;

import io.qameta.allure.Step;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.util.List;

public final class BoxManagementPage extends BasePage {
    private static final By CREATE_BOX_BUTTON = By.xpath(
            "//button[contains(normalize-space(), 'Create Box') or contains(normalize-space(), 'Qutu yarat') or contains(@class, 'createBox')] | //*[contains(@class, 'addBox')]");
    private static final By BOX_NAME_INPUT = By.cssSelector(
            "input[name='boxName'], input[name='name'], input[placeholder*='Box name'], input[placeholder*='Qutunun adı']");
    private static final By SUBMIT_BOX_BUTTON = By.xpath(
            "//button[contains(normalize-space(), 'Create') or contains(normalize-space(), 'Yarat') or contains(normalize-space(), 'Təsdiq') or contains(@class, 'confirmBtn')]");
    private static final By BOX_ITEMS = By.cssSelector(
            "a[href*='/box-details/'], div[class*='boxCard'], div[class*='box-item']");
    private static final By BOX_MENU_BUTTON = By.cssSelector(
            ".boxDots, .boxMenu, button[class*='menu'], svg[class*='dots']");
    private static final By RENAME_BOX_OPTION = By.xpath(
            "//*[contains(normalize-space(), 'Rename') or contains(normalize-space(), 'Adı dəyiş')]");
    private static final By DELETE_BOX_OPTION = By.xpath(
            "//*[contains(normalize-space(), 'Delete') or contains(normalize-space(), 'Sil')]");
    private static final By CONFIRM_DELETE_BUTTON = By.xpath(
            "//button[contains(normalize-space(), 'Delete') or contains(normalize-space(), 'Sil') or contains(normalize-space(), 'Bəli')]");

    public BoxManagementPage(WebDriver driver) {
        super(driver);
    }

    public boolean isLoaded() {
        return isElementVisible(CREATE_BOX_BUTTON) || isElementVisible(BOX_ITEMS);
    }

    public boolean hasCreateBoxButton() {
        return isElementPresent(CREATE_BOX_BUTTON);
    }

    @Step("Yeni qutu yarat: '{boxName}'")
    public boolean createBox(String boxName) {
        if (!isElementPresent(CREATE_BOX_BUTTON)) {
            return false;
        }
        click(CREATE_BOX_BUTTON);
        if (isElementPresent(BOX_NAME_INPUT)) {
            type(BOX_NAME_INPUT, boxName);
            if (isElementPresent(SUBMIT_BOX_BUTTON)) {
                click(SUBMIT_BOX_BUTTON);
                try {
                    Thread.sleep(1500);
                } catch (InterruptedException ignored) {}
                return true;
            }
        }
        return false;
    }

    @Step("Qutunun mövcudluğunu yoxla: '{boxName}'")
    public boolean isBoxPresent(String boxName) {
        List<WebElement> boxes = driver.findElements(BOX_ITEMS);
        for (WebElement box : boxes) {
            try {
                if (box.getText().contains(boxName)) {
                    return true;
                }
            } catch (Exception ignored) {}
        }
        return false;
    }

    @Step("Mövcud qutunu sil: '{boxName}'")
    public boolean deleteBox(String boxName) {
        List<WebElement> boxes = driver.findElements(BOX_ITEMS);
        for (WebElement box : boxes) {
            try {
                if (box.getText().contains(boxName)) {
                    List<WebElement> menus = box.findElements(BOX_MENU_BUTTON);
                    if (!menus.isEmpty()) {
                        menus.get(0).click();
                        if (isElementPresent(DELETE_BOX_OPTION)) {
                            click(DELETE_BOX_OPTION);
                            if (isElementPresent(CONFIRM_DELETE_BUTTON)) {
                                click(CONFIRM_DELETE_BUTTON);
                                Thread.sleep(1000);
                                return true;
                            }
                        }
                    }
                }
            } catch (Exception ignored) {}
        }
        return false;
    }

    @Step("Avtotestlər zamanı yaradılmış müvəqqəti qutuların təmizlənməsi")
    public void cleanupAutoTestBoxes() {
        try {
            List<WebElement> boxes = driver.findElements(BOX_ITEMS);
            for (WebElement box : boxes) {
                if (box.getText().contains("AUTO_TEST")) {
                    List<WebElement> menus = box.findElements(BOX_MENU_BUTTON);
                    if (!menus.isEmpty()) {
                        menus.get(0).click();
                        if (isElementPresent(DELETE_BOX_OPTION)) {
                            click(DELETE_BOX_OPTION);
                            if (isElementPresent(CONFIRM_DELETE_BUTTON)) {
                                click(CONFIRM_DELETE_BUTTON);
                            }
                        }
                    }
                }
            }
        } catch (Exception ignored) {}
    }
}
