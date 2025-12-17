package factory;

import exceptions.BrowserNotSupportedException;
import listeners.AllureListener;
import listeners.HighlightElementListener;
import org.openqa.selenium.MutableCapabilities;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.events.EventFiringDecorator;

import java.net.MalformedURLException;
import java.net.URL;
import java.time.Duration;

public class WebDriverFactory {

    private static final String remoteUrl = System.getProperty("remote.url", "");
    private static final String browserName = System.getProperty("browser", "chrome");
    private static final String browserVersion = System.getProperty("browser.version");

    public static WebDriver getDriver() throws MalformedURLException {
        return getDriver(browserName);
    }

    public static WebDriver getDriver(String webDriverName) throws MalformedURLException {
        WebDriver driver;

        if (!remoteUrl.isEmpty()) {
            MutableCapabilities mutableCapabilities = new DesiredCapabilities();
            mutableCapabilities.setCapability("browserName", browserName);
            mutableCapabilities.setCapability("browserVersion", browserVersion);
            driver = new RemoteWebDriver(new URL(remoteUrl), mutableCapabilities);
        } else {
            switch (webDriverName.toLowerCase()) {
                case "chrome":
                    driver = new ChromeDriver(BrowserOptionsFactory.getChromeOptions());
                    break;
                case "firefox":
                    driver = new FirefoxDriver(BrowserOptionsFactory.getFirefoxOptions());
                    break;
                default:
                    throw new BrowserNotSupportedException(webDriverName);
            }
        }

        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(15));
        driver.manage().window().maximize();

        HighlightElementListener highlightListener = new HighlightElementListener(driver);
        AllureListener allureListener = new AllureListener(driver);

        driver = new EventFiringDecorator<>(highlightListener, allureListener)
                .decorate(driver);

        return driver;
    }
}
