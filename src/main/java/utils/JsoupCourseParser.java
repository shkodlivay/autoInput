package utils;

import dto.CourseDto;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class JsoupCourseParser {

    /**
     * Парсинг курсов из HTML (совместимый с Selenium)
     */
    public static List<CourseDto> parseCoursesFromHtml(String html) {
        List<CourseDto> courses = new ArrayList<>();
        Document doc = Jsoup.parse(html);

        Elements courseElements = doc.select("a.sc-zzdkm7-0");

        System.out.println("Jsoup: найдено элементов с классом sc-zzdkm7-0: " + courseElements.size());

        for (Element courseElement : courseElements) {
            try {
                Element titleElement = courseElement.select("h6.sc-1yg5ro0-1 div.sc-hrqzy3-1").first();
                if (titleElement == null) {
                    continue;
                }

                String title = titleElement.text().trim();
                if (title.isEmpty()) {
                    continue;
                }

                Element dateElement = courseElement.select(".sc-157icee-1 .sc-hrqzy3-1").first();
                String dateText = dateElement != null ? dateElement.text().trim() : "";

                String url = courseElement.attr("href");
                if (url.isEmpty()) {
                    continue;
                }

                if (!url.startsWith("http")) {
                    url = "https://otus.ru" + url;
                }

                if (isNoDateMessage(dateText)) {
                    courses.add(new CourseDto(title, url));
                    continue;
                }

                String datePart = extractDatePart(dateText);

                LocalDate startDate = CourseDto.parseDate(datePart);

                if (startDate != null) {
                    courses.add(new CourseDto(title, startDate, url));
                } else {
                    courses.add(new CourseDto(title, url));
                }

            } catch (Exception e) {
                System.err.println("Jsoup: Ошибка при парсинге курса: " + e.getMessage());
            }
        }

        System.out.println("Jsoup: успешно распарсено курсов: " + courses.size());
        return courses;
    }

    /**
     * Извлечь часть с датой из текста
     */
    private static String extractDatePart(String dateText) {
        if (dateText == null || dateText.isEmpty()) {
            return "";
        }

        String[] parts = dateText.split("·");
        return parts[0].trim();
    }

    /**
     * Проверка сообщений об отсутствии даты (совместимая с Selenium)
     */
    private static boolean isNoDateMessage(String text) {
        if (text == null || text.isEmpty()) {
            return true;
        }

        String lowerText = text.toLowerCase();

        if (lowerText.contains("объявлено позже") ||
                lowerText.contains("дата уточняется") ||
                lowerText.contains("будет объявлено") ||
                lowerText.contains("не указана") ||
                lowerText.contains("скоро объявим") ||
                lowerText.contains("дата старта уточняется")) {
            return true;
        }

        boolean hasValidFormat = text.matches(".*\\d{1,2}\\s+[а-я]+,\\s+\\d{4}.*");

        return !hasValidFormat;
    }

    /**
     * Фильтрация курсов с датой
     */
    public static List<CourseDto> filterCoursesWithDate(List<CourseDto> courses) {
        return courses.stream()
                .filter(CourseDto::hasDate)
                .collect(java.util.stream.Collectors.toList());
    }

    /**
     * Найти курсы с самой ранней датой с помощью Jsoup
     */
    public static List<CourseDto> findEarliestCoursesWithJsoup(String html) {
        List<CourseDto> allCourses = parseCoursesFromHtml(html);
        List<CourseDto> coursesWithDates = filterCoursesWithDate(allCourses);

        System.out.println("Jsoup: курсов с датами: " + coursesWithDates.size());

        if (coursesWithDates.isEmpty()) {
            System.out.println("Jsoup: Нет курсов с указанной датой");
            return List.of();
        }

        Optional<LocalDate> minDate = coursesWithDates.stream()
                .map(course -> course.getStartDate().orElse(null))
                .filter(date -> date != null)
                .reduce((date1, date2) -> date1.isBefore(date2) ? date1 : date2);

        if (minDate.isPresent()) {
            LocalDate earliestDate = minDate.get();
            System.out.println("Jsoup: самая ранняя дата: " + earliestDate);

            return coursesWithDates.stream()
                    .filter(course -> course.getStartDate()
                            .map(date -> date.equals(earliestDate))
                            .orElse(false))
                    .collect(java.util.stream.Collectors.toList());
        }

        return List.of();
    }

    /**
     * Найти курсы с самой поздней датой с помощью Jsoup
     */
    public static List<CourseDto> findLatestCoursesWithJsoup(String html) {
        List<CourseDto> allCourses = parseCoursesFromHtml(html);
        List<CourseDto> coursesWithDates = filterCoursesWithDate(allCourses);

        if (coursesWithDates.isEmpty()) {
            System.out.println("Jsoup: Нет курсов с указанной датой");
            return List.of();
        }

        Optional<LocalDate> maxDate = coursesWithDates.stream()
                .map(course -> course.getStartDate().orElse(null))
                .filter(date -> date != null)
                .reduce((date1, date2) -> date1.isAfter(date2) ? date1 : date2);

        if (maxDate.isPresent()) {
            LocalDate latestDate = maxDate.get();
            System.out.println("Jsoup: самая поздняя дата: " + latestDate);

            return coursesWithDates.stream()
                    .filter(course -> course.getStartDate()
                            .map(date -> date.equals(latestDate))
                            .orElse(false))
                    .collect(java.util.stream.Collectors.toList());
        }

        return List.of();
    }

    /**
     * DTO для информации со страницы курса
     */
    public static class CoursePageInfo {
        private final String title;
        private final String dateText;
        private final String url;

        public CoursePageInfo(String title, String dateText, String url) {
            this.title = title != null ? title.trim() : "";
            this.dateText = dateText != null ? dateText.trim() : "";
            this.url = url != null ? url.trim() : "";
        }

        public String getTitle() {
            return title;
        }

        public String getDateText() {
            return dateText;
        }

        public String getUrl() {
            return url;
        }

        @Override
        public String toString() {
            return String.format("CoursePageInfo{title='%s', dateText='%s', url='%s'}",
                    title, dateText, url);
        }
    }

    /**
     * Парсинг конкретной страницы курса по URL с помощью Jsoup
     */
    public static CoursePageInfo parseCoursePage(String url) {
        try {
            System.out.println("Jsoup: парсим страницу курса: " + url);

            Document doc = Jsoup.connect(url)
                    .timeout(10000)
                    .get();

            String title = "";
            Element titleElement = doc.select("h1").first();

            if (titleElement != null) {
                title = titleElement.text();
            } else {
                titleElement = doc.select("h1.sc-1yg5ro0-1").first();
                if (titleElement != null) {
                    title = titleElement.text();
                }
            }

            String dateText = "";

            String[] dateSelectors = {
                    ".sc-157icee-1 .sc-hrqzy3-1",
                    ".course-date",
                    ".start-date",
                    "[class*='date']",
                    ".course-info__date"
            };

            for (String selector : dateSelectors) {
                Element dateElement = doc.select(selector).first();
                if (dateElement != null && !dateElement.text().trim().isEmpty()) {
                    dateText = dateElement.text().trim();
                    System.out.println("Jsoup: дата найдена через селектор '" + selector + "': " + dateText);
                    break;
                }
            }

            CoursePageInfo info = new CoursePageInfo(title, dateText, url);
            System.out.println("Jsoup: результат парсинга: " + info);

            return info;

        } catch (IOException e) {
            System.err.println("Jsoup: Ошибка при парсинге страницы курса: " + url + " - " + e.getMessage());
            return new CoursePageInfo("", "", url);
        } catch (Exception e) {
            System.err.println("Jsoup: Неожиданная ошибка при парсинге: " + url + " - " + e.getMessage());
            return new CoursePageInfo("", "", url);
        }
    }

}
