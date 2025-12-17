package listeners;

import org.openqa.selenium.*;
import org.openqa.selenium.support.events.WebDriverListener;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

public class HighlightElementListener implements WebDriverListener {

    private final JavascriptExecutor js;
    private static final List<String> METHODS_TO_HIGHLIGHT = Arrays.asList(
            "click", "sendKeys", "submit", "clear"
    );

    public HighlightElementListener(WebDriver driver) {
        this.js = (JavascriptExecutor) driver;
    }

    @Override
    public void beforeAnyWebElementCall(WebElement element, Method method, Object[] args) {
        if (shouldHighlight(method)) {
            highlightElement(element, "3px solid red");
        }
    }

    @Override
    public void beforeClick(WebElement element) {
        highlightElement(element, "4px solid magenta");
    }

    @Override
    public void beforeSendKeys(WebElement element, CharSequence... keysToSend) {
        highlightElement(element, "3px solid green");
    }

    private boolean shouldHighlight(Method method) {
        return METHODS_TO_HIGHLIGHT.contains(method.getName());
    }

    private void highlightElement(WebElement element, String borderStyle) {
        try {
            String script = String.format(
                    "arguments[0].style.border = '%s'; arguments[0].style.boxShadow = '0 0 10px rgba(0,0,0,0.5)';",
                    borderStyle
            );
            js.executeScript(script, element);

            Thread.sleep(200);

            js.executeScript("arguments[0].style.border = ''; arguments[0].style.boxShadow = '';", element);

        } catch (Exception e) {
            // Игнорируем ошибки выделения
        }
    }
}
