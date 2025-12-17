package commons;

import Waiters.Waiter;
import com.google.inject.Guice;
import com.google.inject.Inject;
import modules.ToolsGuiceModule;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.PageFactory;

public abstract class AbsCommon {

    protected WebDriver driver;

    @Inject
    protected Waiter waiter;

    @Inject
    protected Actions actions;

    public AbsCommon(WebDriver driver) {
        this.driver = driver;
        PageFactory.initElements(driver, this);
        Guice.createInjector(new ToolsGuiceModule(driver)).injectMembers(this);
    }

    protected WebElement $(By selector) {
        return driver.findElement(selector);
    }

    public String getCurrentUrl() {
        return driver.getCurrentUrl();
    }

    /**
     * Выделить элемент рамкой
     */
    protected void highlightElement(WebElement element, String borderStyle) {
        try {
            JavascriptExecutor js = (JavascriptExecutor) driver;

            String originalStyle = element.getAttribute("style");

            js.executeScript(
                    "arguments[0].setAttribute('style', arguments[1]);",
                    element,
                    "border: " + borderStyle + " !important; " +
                            "background-color: rgba(255,255,0,0.1) !important; " +
                            "z-index: 9999 !important;"
            );

            try {
                Thread.sleep(250);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

            if (originalStyle != null) {
                js.executeScript(
                        "arguments[0].setAttribute('style', arguments[1]);",
                        element,
                        originalStyle
                );
            } else {
                js.executeScript(
                        "arguments[0].removeAttribute('style');",
                        element
                );
            }

        } catch (Exception e) {
            System.out.println("Не удалось выделить элемент: " + e.getMessage());
        }
    }
}
