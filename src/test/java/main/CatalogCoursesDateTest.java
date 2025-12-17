package main;

import com.google.inject.Inject;
import dto.CourseDto;
import extensions.UIExtension;
import io.qameta.allure.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import pages.CatalogPage;
import utils.JsoupCourseParser;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

import static io.qameta.allure.Allure.step;
import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(UIExtension.class)
@Epic("Каталог курсов Otus")
@Feature("Сценарий 2: Поиск курсов по дате")
@DisplayName("Сценарий 2: Поиск курсов с самой ранней и поздней датами")
@Owner("Автоматизатор")
@Severity(SeverityLevel.CRITICAL)
@Link(name = "Otus.ru", url = "https://otus.ru")
public class CatalogCoursesDateTest {

    @Inject
    private CatalogPage catalogPage;

    @Test
    @DisplayName("Найти курсы с самой ранней и поздней датой начала")
    @Description("Использование Stream API и reduce для поиска курсов с минимальной и максимальной датой начала. Проверка с использованием Jsoup.")
    public void findCoursesWithEarliestAndLatestDates() {
        step("Шаг 1: Открыть страницу каталога курсов", () -> {
            catalogPage.open();
            String currentUrl = catalogPage.getCurrentUrl();
            Allure.addAttachment("Каталог открыт", "URL: " + currentUrl);

            try {
                Thread.sleep(2000); // Ждем полной загрузки
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });

        step("Шаг 2: Вывести статистику курсов", () -> {
            catalogPage.printCourseStatistics();
        });

        step("Шаг 3: Найти курсы с самой ранней датой (Selenium + Stream API reduce)", () -> {
            List<CourseDto> earliestCoursesSelenium = catalogPage.findCoursesWithEarliestDate();

            Allure.addAttachment("Ранние курсы (Selenium)",
                    "Найдено: " + earliestCoursesSelenium.size() + " курсов\n" +
                            formatCoursesList(earliestCoursesSelenium));

            assertThat(earliestCoursesSelenium)
                    .as("Должны быть найдены курсы с самой ранней датой")
                    .isNotEmpty();

            if (!earliestCoursesSelenium.isEmpty()) {
                java.time.LocalDate earliestDate = earliestCoursesSelenium.get(0).getStartDate().orElse(null);
                Allure.addAttachment("Самая ранняя дата (Selenium)",
                        formatDate(earliestDate));
            }
        });

        step("Шаг 4: Найти курсы с самой ранней датой (Jsoup + Stream API reduce)", () -> {
            String pageHtml = catalogPage.getPageHtml();
            List<CourseDto> earliestCoursesJsoup = JsoupCourseParser.findEarliestCoursesWithJsoup(pageHtml);

            Allure.addAttachment("Ранние курсы (Jsoup)",
                    "Найдено: " + earliestCoursesJsoup.size() + " курсов\n" +
                            formatCoursesList(earliestCoursesJsoup));

            assertThat(earliestCoursesJsoup)
                    .as("Jsoup должен найти курсы с самой ранней датой")
                    .isNotEmpty();
        });

        step("Шаг 5: Найти курсы с самой поздней датой (Selenium + Stream API reduce)", () -> {
            List<CourseDto> latestCoursesSelenium = catalogPage.findCoursesWithLatestDate();

            Allure.addAttachment("Поздние курсы (Selenium)",
                    "Найдено: " + latestCoursesSelenium.size() + " курсов\n" +
                            formatCoursesList(latestCoursesSelenium));

            assertThat(latestCoursesSelenium)
                    .as("Должны быть найдены курсы с самой поздней датой")
                    .isNotEmpty();

            if (!latestCoursesSelenium.isEmpty()) {
                java.time.LocalDate latestDate = latestCoursesSelenium.get(0).getStartDate().orElse(null);
                Allure.addAttachment("Самая поздняя дата (Selenium)",
                        formatDate(latestDate));
            }
        });

        step("Шаг 6: Найти курсы с самой поздней датой (Jsoup + Stream API reduce)", () -> {
            String pageHtml = catalogPage.getPageHtml();
            List<CourseDto> latestCoursesJsoup = JsoupCourseParser.findLatestCoursesWithJsoup(pageHtml);

            Allure.addAttachment("Поздние курсы (Jsoup)",
                    "Найдено: " + latestCoursesJsoup.size() + " курсов\n" +
                            formatCoursesList(latestCoursesJsoup));

            assertThat(latestCoursesJsoup)
                    .as("Jsoup должен найти курсы с самой поздней датой")
                    .isNotEmpty();
        });

        step("Шаг 7: Сравнить результаты Selenium и Jsoup", () -> {
            String pageHtml = catalogPage.getPageHtml();

            List<CourseDto> earliestSelenium = catalogPage.findCoursesWithEarliestDate();
            List<CourseDto> earliestJsoup = JsoupCourseParser.findEarliestCoursesWithJsoup(pageHtml);

            List<CourseDto> latestSelenium = catalogPage.findCoursesWithLatestDate();
            List<CourseDto> latestJsoup = JsoupCourseParser.findLatestCoursesWithJsoup(pageHtml);

            Allure.addAttachment("Сравнение результатов",
                    "Ранние курсы:\n" +
                            "  Selenium: " + earliestSelenium.size() + " курсов\n" +
                            "  Jsoup: " + earliestJsoup.size() + " курсов\n\n" +
                            "Поздние курсы:\n" +
                            "  Selenium: " + latestSelenium.size() + " курсов\n" +
                            "  Jsoup: " + latestJsoup.size() + " курсов");

            if (!earliestSelenium.isEmpty() && !earliestJsoup.isEmpty()) {
                java.time.LocalDate seleniumDate = earliestSelenium.get(0).getStartDate().orElse(null);
                java.time.LocalDate jsoupDate = earliestJsoup.get(0).getStartDate().orElse(null);

                assertThat(jsoupDate)
                        .as("Самые ранние даты должны совпадать")
                        .isEqualTo(seleniumDate);
            }

            if (!latestSelenium.isEmpty() && !latestJsoup.isEmpty()) {
                java.time.LocalDate seleniumDate = latestSelenium.get(0).getStartDate().orElse(null);
                java.time.LocalDate jsoupDate = latestJsoup.get(0).getStartDate().orElse(null);

                assertThat(jsoupDate)
                        .as("Самые поздние даты должны совпадать")
                        .isEqualTo(seleniumDate);
            }
        });

        step("Шаг 8: Проверить данные на странице курса с помощью Jsoup", () -> {
            List<CourseDto> earliestCourses = catalogPage.findCoursesWithEarliestDate();

            if (!earliestCourses.isEmpty()) {
                CourseDto earliestCourse = earliestCourses.get(0);
                Allure.addAttachment("Проверка курса Jsoup",
                        "Курс: " + earliestCourse.getTitle() + "\n" +
                                "URL: " + earliestCourse.getUrl());

                JsoupCourseParser.CoursePageInfo pageInfo =
                        JsoupCourseParser.parseCoursePage(earliestCourse.getUrl());

                Allure.addAttachment("Результат парсинга Jsoup",
                        "Заголовок на странице: " + pageInfo.getTitle() + "\n" +
                                "Дата на странице: " + pageInfo.getDateText());

                assertThat(pageInfo.getTitle())
                        .as("Название курса на странице не должно быть пустым")
                        .isNotEmpty();
            }
        });
    }

     /**
     * Форматирование списка курсов для вывода
     */
    private String formatCoursesList(List<CourseDto> courses) {
        if (courses.isEmpty()) {
            return "Нет курсов";
        }

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < Math.min(courses.size(), 3); i++) {
            CourseDto course = courses.get(i);
            sb.append(i + 1).append(". ").append(course.getTitle())
                    .append(" - ").append(formatDate(course.getStartDate().orElse(null)))
                    .append("\n");
        }
        if (courses.size() > 3) {
            sb.append("... и еще ").append(courses.size() - 3).append(" курсов");
        }
        return sb.toString();
    }

    /**
     * Форматирование даты для вывода
     */
    private String formatDate(java.time.LocalDate date) {
        if (date == null) {
            return "Дата не указана";
        }
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMMM yyyy", new Locale("ru"));
        return date.format(formatter);
    }
}
