package pages;

import annotations.Path;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

@Path("/lessons/")
public class CoursePage extends AbsBasePage<CoursePage> {

    private final By courseTitleLocator = By.cssSelector("h1");

    public CoursePage(WebDriver driver) {
        super(driver);
    }

    /**
     * Ожидание загрузки страницы курса
     */
    public void waitForCoursePageLoaded() {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));

        wait.until(ExpectedConditions.presenceOfElementLocated(courseTitleLocator));

        wait.until(driver -> {
            String title = driver.findElement(courseTitleLocator).getText().trim();
            return !title.isEmpty();
        });

        System.out.println("Страница курса загружена");
        System.out.println("Заголовок: " + getCourseTitle());
        System.out.println("URL: " + driver.getCurrentUrl());
    }

    public String getCourseTitle() {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(5));
        WebElement titleElement = wait.until(
                ExpectedConditions.visibilityOfElementLocated(courseTitleLocator)
        );
        return titleElement.getText().trim();
    }

    /**
     * Извлечь slug из текущего URL
     */
    public String getSlugFromUrl() {
        String currentUrl = driver.getCurrentUrl();
        System.out.println("Полный URL для парсинга: " + currentUrl);

        if (currentUrl.contains("/lessons/")) {
            String[] parts = currentUrl.split("/lessons/");
            if (parts.length > 1) {
                String slugWithParams = parts[1];

                String slug = slugWithParams.split("\\?")[0];

                if (slug.endsWith("/")) {
                    slug = slug.substring(0, slug.length() - 1);
                }

                System.out.println("Извлеченный slug: " + slug);
                return slug;
            }
        }
        return "";
    }

    /**
     * Проверить, что открыт правильный курс
     */
    public boolean isCorrectCourseOpened(String expectedCourseName) {
        String actualTitle = getCourseTitle();
        return actualTitle.contains(expectedCourseName) ||
                expectedCourseName.contains(actualTitle);
    }
}
