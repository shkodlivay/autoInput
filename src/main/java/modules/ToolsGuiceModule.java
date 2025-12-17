package modules;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.interactions.Actions;

public class ToolsGuiceModule extends AbstractModule {

    private final WebDriver driver;

    public ToolsGuiceModule(WebDriver driver) {
        this.driver = driver;
    }

    @Override
    protected void configure() {
        Waiters.Waiter waiter = new Waiters.Waiter(driver);
        bind(Waiters.Waiter.class).toInstance(waiter);
    }

    @Provides
    public Actions provideActions() {
        return new Actions(driver);
    }

}