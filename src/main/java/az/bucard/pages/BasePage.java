package az.bucard.pages;

import az.bucard.config.Config;
import org.openqa.selenium.By;
import org.openqa.selenium.ElementClickInterceptedException;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

public abstract class BasePage {
    protected final WebDriver driver;
    protected final WebDriverWait wait;

    protected BasePage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(Config.explicitTimeoutSeconds()));
    }

    protected WebElement visible(By locator) {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
    }

    protected WebElement clickable(By locator) {
        return wait.until(ExpectedConditions.elementToBeClickable(locator));
    }

    protected boolean isElementVisible(By locator, Duration timeout) {
        try {
            new WebDriverWait(driver, timeout).until(ExpectedConditions.visibilityOfElementLocated(locator));
            return true;
        } catch (TimeoutException e) {
            return false;
        }
    }

    protected boolean isElementVisible(By locator) {
        return isElementVisible(locator, Duration.ofSeconds(Config.explicitTimeoutSeconds()));
    }

    protected boolean isElementPresent(By locator) {
        return !driver.findElements(locator).isEmpty();
    }

    protected void click(By locator) {
        try {
            clickable(locator).click();
        } catch (ElementClickInterceptedException e) {
            // Dismiss overlay or modal if present
            try {
                By dismissLocators = By.cssSelector("div.closeModal, img[alt='close-icon'], div.overlay");
                if (isElementPresent(dismissLocators)) {
                    driver.findElement(dismissLocators).click();
                    wait.until(ExpectedConditions.invisibilityOfElementLocated(By.cssSelector("div.overlay")));
                }
            } catch (Exception ignored) {
            }
            // Retry standard click or fallback to JavaScript click
            try {
                clickable(locator).click();
            } catch (Exception ex) {
                ((JavascriptExecutor) driver).executeScript("arguments[0].click();", visible(locator));
            }
        }
    }

    protected void type(By locator, String text) {
        visible(locator).sendKeys(text);
    }

    protected void clearAndType(By locator, String text) {
        WebElement element = visible(locator);
        element.clear();
        element.sendKeys(text);
    }

    protected String getTextSafe(By locator) {
        try {
            return visible(locator).getText().trim();
        } catch (TimeoutException e) {
            return "";
        }
    }

    public boolean waitForTitleContains(String titleFraction) {
        try {
            return wait.until(ExpectedConditions.titleContains(titleFraction));
        } catch (TimeoutException e) {
            return false;
        }
    }

    public boolean waitForUrlContains(String urlFraction) {
        try {
            return wait.until(ExpectedConditions.urlContains(urlFraction));
        } catch (TimeoutException e) {
            return false;
        }
    }

    public String getPageTitle() {
        return driver.getTitle();
    }

    public String getCurrentUrl() {
        return driver.getCurrentUrl();
    }
}
