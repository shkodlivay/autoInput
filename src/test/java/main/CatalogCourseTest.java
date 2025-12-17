package main;

import com.google.inject.Inject;
import extensions.UIExtension;
import io.qameta.allure.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import pages.CatalogPage;
import pages.CoursePage;

import static io.qameta.allure.Allure.step;
import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(UIExtension.class)
@Epic("Каталог курсов Otus")
@Feature("Сценарий 1: Поиск курса по имени")
@DisplayName("Сценарий 1: Поиск курса по имени")
@Owner("Автоматизатор")
@Severity(SeverityLevel.BLOCKER)
@Link(name = "Otus.ru", url = "https://otus.ru")
public class CatalogCourseTest {

    @Inject
    private CatalogPage catalogPage;

    @Inject
    private CoursePage coursePage;

    @ParameterizedTest(name = "Поиск и открытие курса: {0}")
    @ValueSource(strings = {
            "Angular Developer"
    })
    @DisplayName("Параметризованный поиск курсов по имени")
    @Description("Поиск разных курсов по имени с использованием Stream API, клик и проверка открытия страницы")
    @Severity(SeverityLevel.CRITICAL)
    public void findCourseByNameAndVerifyPage(String courseName) {
        Allure.parameter("Название курса", courseName);

        step("Шаг 1: Открыть страницу каталога курсов", () -> {
            catalogPage.open();
            String currentUrl = catalogPage.getCurrentUrl();
            Allure.addAttachment("Каталог открыт", "URL: " + currentUrl);
            assertThat(currentUrl)
                    .as("Должен быть открыт каталог курсов")
                    .contains("/catalog/courses");
        });

        step("Шаг 2: Получить статистику курсов", () -> {
            int courseCount = catalogPage.getAllCourseCards().size();
            Allure.addAttachment("Количество курсов", String.valueOf(courseCount));
            assertThat(courseCount)
                    .as("В каталоге должны быть курсы")
                    .isGreaterThan(0);
        });

        step("Шаг 3: Найти курс '" + courseName + "' используя Stream API", () -> {
            String courseSlug = catalogPage.getAllCourseCards().stream()
                    .filter(card -> {
                        try {
                            String title = card.findElement(
                                    org.openqa.selenium.By.cssSelector("h6.sc-1yg5ro0-1 div.sc-hrqzy3-1")
                            ).getText().trim();
                            return title.equals(courseName);
                        } catch (Exception e) {
                            return false;
                        }
                    })
                    .findFirst()
                    .map(card -> {
                        String url = card.getAttribute("href");
                        String slug = url.substring(url.lastIndexOf("/") + 1).split("\\?")[0];
                        Allure.addAttachment("Найденный курс",
                                "Название: " + courseName + "\nSlug: " + slug + "\nURL: " + url);
                        return slug;
                    })
                    .orElseThrow(() -> {
                        String availableCourses = catalogPage.getAllCourseNames().stream()
                                .limit(5)
                                .collect(java.util.stream.Collectors.joining("\n- ", "Доступные курсы:\n- ", ""));
                        return new RuntimeException("Курс '" + courseName + "' не найден.\n" + availableCourses);
                    });

            Allure.addAttachment("Slug курса", courseSlug);
        });

        step("Шаг 4: Кликнуть по плитке курса", () -> {
            catalogPage.clickCourseByName(courseName);
            Allure.addAttachment("Клик выполнен", "Клик по курсу: " + courseName);
        });

        step("Шаг 5: Проверить что открыта страница верного курса", () -> {
            coursePage.waitForCoursePageLoaded();

            String actualCourseTitle = coursePage.getCourseTitle();
            String currentUrl = coursePage.getCurrentUrl();

            Allure.addAttachment("Страница курса",
                    "Заголовок: " + actualCourseTitle + "\nURL: " + currentUrl);

            assertThat(actualCourseTitle)
                    .as("Заголовок страницы курса должен содержать название курса")
                    .containsIgnoringCase(courseName.split("\\.")[0].trim());

            assertThat(currentUrl)
                    .as("URL должен содержать slug курса")
                    .containsPattern("/lessons/.*");

            boolean isCorrectCourse = coursePage.isCorrectCourseOpened(courseName);
            assertThat(isCorrectCourse)
                    .as("Должен быть открыт правильный курс")
                    .isTrue();
        });
    }
}
