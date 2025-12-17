package components;

import annotations.Component;
import commons.AbsCommon;
import exceptions.ComponentSelectorNotValidException;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public abstract class AbsComponent extends AbsCommon {

    public AbsComponent(WebDriver driver) {
        super(driver);
    }

    {
        assertThat(waiter.waitForElementVisible(getByComponent()))
                .as("Error")
                .isTrue();
    }

    private By getByComponent() {
        Class clazz = getClass();

        if(clazz.isAnnotationPresent(Component.class)) {
            Component component = (Component) clazz.getDeclaredAnnotation(Component.class);
            String value = component.value();

            Pattern pattern = Pattern.compile("(.*?):(.*?)");
            Matcher matcher = pattern.matcher(value);

            if(matcher.find()) {
                if (matcher.group(1).equals("css")) {
                    return By.cssSelector(matcher.group(2));
                }
            }
        }

        throw new ComponentSelectorNotValidException();
    }

    public WebElement getComponentEntity() {
        return $(getByComponent());
    }

}
