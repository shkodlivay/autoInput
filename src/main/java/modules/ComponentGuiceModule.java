package modules;

import com.google.inject.AbstractModule;
import org.openqa.selenium.WebDriver;

public class ComponentGuiceModule extends AbstractModule {

    private final WebDriver driver;

    public ComponentGuiceModule(WebDriver driver) {
        this.driver = driver;
    }

    @Override
    protected void configure() {
    }
}