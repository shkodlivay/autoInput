package modules;

import com.google.inject.AbstractModule;
import org.openqa.selenium.WebDriver;
import pages.CatalogPage;
import pages.CoursePage;
import pages.MainPage;

public class PageGuiceModule extends AbstractModule {

    private final WebDriver driver;

    public PageGuiceModule(WebDriver driver) {
        this.driver = driver;
    }

    @Override
    protected void configure() {
        bind(CatalogPage.class).toInstance(new CatalogPage(driver));
        bind(CoursePage.class).toInstance(new CoursePage(driver));
        bind(MainPage.class).toInstance(new MainPage(driver));
    }
}
