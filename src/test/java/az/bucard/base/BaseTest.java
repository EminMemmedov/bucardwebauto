package az.bucard.base;

import az.bucard.driver.DriverFactory;
import az.bucard.utils.ScreenshotUtil;
import io.qameta.allure.Attachment;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.testng.ITestResult;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;

import java.nio.file.Path;

public abstract class BaseTest {
    private static final ThreadLocal<WebDriver> DRIVER_HOLDER = new ThreadLocal<>();

    @BeforeMethod(alwaysRun = true)
    public void setUp() {
        WebDriver driver = DriverFactory.createDriver();
        DRIVER_HOLDER.set(driver);
    }

    @AfterMethod(alwaysRun = true)
    public void tearDown(ITestResult result) {
        WebDriver driver = DRIVER_HOLDER.get();
        if (driver != null) {
            try {
                if (result.getStatus() == ITestResult.FAILURE) {
                    attachScreenshot("❌ Xəta anında ekran görüntüsü");
                    Path screenshot = ScreenshotUtil.captureFailure(driver, result.getName());
                    if (screenshot != null) {
                        result.setAttribute("screenshotPath", screenshot.toString());
                    }
                } else if (result.getStatus() == ITestResult.SUCCESS) {
                    attachScreenshot("📸 Uğurlu icra skrinşotu");
                }
            } finally {
                DriverFactory.quitDriver(driver);
                DRIVER_HOLDER.remove();
            }
        }
    }

    @Attachment(value = "{title}", type = "image/png")
    public byte[] attachScreenshot(String title) {
        WebDriver driver = DRIVER_HOLDER.get();
        if (driver instanceof TakesScreenshot takesScreenshot) {
            try {
                return takesScreenshot.getScreenshotAs(OutputType.BYTES);
            } catch (Exception ignored) {
            }
        }
        return new byte[0];
    }

    protected WebDriver getDriver() {
        WebDriver driver = DRIVER_HOLDER.get();
        if (driver == null) {
            throw new IllegalStateException("WebDriver is not initialized for the current thread");
        }
        return driver;
    }
}
