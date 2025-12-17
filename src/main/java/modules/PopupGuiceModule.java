package modules;

import com.google.inject.AbstractModule;
import org.openqa.selenium.WebDriver;

public class PopupGuiceModule extends AbstractModule {

    private final WebDriver driver;

    public PopupGuiceModule(WebDriver driver) {
        this.driver = driver;
    }

    @Override
    protected void configure() {
    }
}
