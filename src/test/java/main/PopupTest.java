package main;

import com.google.inject.Inject;
import extensions.UIExtension;
import io.qameta.allure.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import pages.CatalogPage;
import pages.MainPage;

import static io.qameta.allure.Allure.step;
import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(UIExtension.class)
@Epic("Главная страница Otus")
@Feature("Сценарий 3: Навигация по меню")
@DisplayName("Сценарий 3: Выбор случайной категории из меню 'Обучение'")
@Owner("Автоматизатор")
@Severity(SeverityLevel.NORMAL)
@Link(name = "Otus.ru", url = "https://otus.ru")
public class PopupTest {

    @Inject
    private MainPage mainPage;

    @Inject
    private CatalogPage catalogPage;

    private String selectedCategoryName;

    @Test
    @DisplayName("Открыть меню 'Обучение' и выбрать случайную категорию")
    @Description("Пользователь открывает главную страницу, наводит на меню 'Обучение', выбирает случайную категорию и проверяет открытие каталога")
    public void selectRandomCategoryFromTrainingMenu() {
        step("Шаг 1: Открыть главную страницу Otus", () -> {
            mainPage.open();
            String currentUrl = mainPage.getCurrentUrl();
            Allure.addAttachment("Главная страница", "URL: " + currentUrl);

            assertThat(currentUrl)
                    .as("Должна быть открыта главная страница Otus")
                    .isEqualTo("https://otus.ru/");
        });

        step("Шаг 2: Навести курсор на меню 'Обучение'", () -> {
            mainPage.hoverOverTrainingMenu();
            Allure.addAttachment("Наведение выполнено", "Курсор наведен на меню 'Обучение'");
        });

        step("Шаг 3: Получить список всех категорий", () -> {
            java.util.List<String> categories = mainPage.getCategoryNames();
            Allure.addAttachment("Доступные категории",
                    "Найдено категорий: " + categories.size() + "\n" +
                            String.join("\n", categories.stream().limit(5).toArray(String[]::new)) +
                            (categories.size() > 5 ? "\n... и еще " + (categories.size() - 5) + " категорий" : ""));

            assertThat(categories)
                    .as("Должны быть найдены категории курсов")
                    .isNotEmpty();
        });

        step("Шаг 4: Выбрать случайную категорию", () -> {
            selectedCategoryName = mainPage.selectRandomCategory();

            String selectedCategorySlug = mainPage.getSelectedCategorySlug();

            Allure.addAttachment("Выбранная категория",
                    "Название: " + selectedCategoryName + "\n" +
                            "Slug: " + selectedCategorySlug);
        });

        step("Шаг 5: Проверить что открыт каталог с выбранной категорией", () -> {
            String currentUrl = catalogPage.getCurrentUrl();
            Allure.addAttachment("Текущий URL", currentUrl);

            assertThat(currentUrl)
                    .as("Должен быть открыт каталог курсов")
                    .containsPattern("(https://otus\\.ru/catalog/|https://otus\\.ru/categories/)");

            String actualCategorySlug = catalogPage.getCategorySlugFromUrl();

            boolean isFilterApplied = catalogPage.isCategoryFilterApplied(actualCategorySlug);

            Allure.addAttachment("Проверка категории",
                    "Выбрано из меню: " + selectedCategoryName + "\n" +
                            "Slug из меню: " + mainPage.getSelectedCategorySlug() + "\n" +
                            "Slug из URL: " + actualCategorySlug + "\n" +
                            "Фильтр применен: " + (isFilterApplied ? "ДА" : "НЕТ"));

            if (!actualCategorySlug.isEmpty() && !mainPage.getSelectedCategorySlug().isEmpty()) {
                assertThat(actualCategorySlug)
                        .as("Slug в URL должен соответствовать выбранной категории")
                        .isEqualTo(mainPage.getSelectedCategorySlug());
            } else {
                assertThat(isFilterApplied)
                        .as("Фильтр по категории должен быть применен")
                        .isTrue();
            }

            // Проверяем, что есть курсы
            int courseCount = catalogPage.getAllCourseCards().size();
            Allure.addAttachment("Количество курсов", String.valueOf(courseCount));

            assertThat(courseCount)
                    .as("На странице категории должны быть курсы")
                    .isGreaterThan(0);

            if (courseCount > 0) {
                String exampleCourses = catalogPage.getAllCourseNames().stream()
                        .limit(3)
                        .collect(java.util.stream.Collectors.joining("\n"));
                Allure.addAttachment("Примеры курсов", exampleCourses);
            }
        });

        step("Шаг 6: Проверить что на странице есть курсы", () -> {
            int courseCount = catalogPage.getAllCourseCards().size();
            Allure.addAttachment("Количество курсов", String.valueOf(courseCount));

            assertThat(courseCount)
                    .as("На странице категории должны быть курсы")
                    .isGreaterThan(0);

            if (courseCount > 0) {
                String exampleCourses = catalogPage.getAllCourseNames().stream()
                        .limit(3)
                        .collect(java.util.stream.Collectors.joining("\n"));
                Allure.addAttachment("Примеры курсов", exampleCourses);
            }
        });

        step("Шаг 7: Проверить заголовок страницы", () -> {
            String pageTitle = catalogPage.getPageTitle();
            Allure.addAttachment("Заголовок страницы", pageTitle);

            assertThat(pageTitle)
                    .as("Заголовок страницы не должен быть пустым")
                    .isNotEmpty();
        });
    }

}
