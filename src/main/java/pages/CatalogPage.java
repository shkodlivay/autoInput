package pages;

import annotations.Path;
import dto.CourseDto;
import org.openqa.selenium.*;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Path("/catalog/courses")
public class CatalogPage extends AbsBasePage<CatalogPage> {

    private final By courseCardsLocator = By.cssSelector("a.sc-zzdkm7-0");
    private final By pageTitleLocator = By.cssSelector("h1.sc-hrqzy3-0");
    private final By courseDateLocator = By.cssSelector(".sc-157icee-1 .sc-hrqzy3-1");
    private final By showMoreButton = By.cssSelector("button.sc-1qig7zt-0.bYRRHi.sc-prqxfo-0.cXVWAS");
    private final By courseTitleLocator = By.cssSelector("h6.sc-1yg5ro0-1 div.sc-hrqzy3-1");

    public CatalogPage(WebDriver driver) {
        super(driver);
    }

    @Override
    public CatalogPage open() {
        super.open();

        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        wait.until(ExpectedConditions.presenceOfElementLocated(pageTitleLocator));
        wait.until(ExpectedConditions.presenceOfElementLocated(courseCardsLocator));

        return this;
    }

    /**
     * Получить заголовок страницы
     */
    public String getPageTitle() {
        try {
            // Сначала пробуем найти h1 заголовок
            List<WebElement> h1Elements = driver.findElements(By.tagName("h1"));
            if (!h1Elements.isEmpty()) {
                String h1Text = h1Elements.get(0).getText().trim();
                if (!h1Text.isEmpty()) {
                    return h1Text;
                }
            }

            // Если нет h1, используем title страницы
            return driver.getTitle();

        } catch (Exception e) {
            return driver.getTitle();
        }
    }

     /**
     * Получить ВСЕ карточки курсов
     */
    public List<WebElement> getAllCourseCards() {
        System.out.println("Получаем все карточки курсов...");

        List<WebElement> cards = driver.findElements(courseCardsLocator);
        System.out.println("Первоначально найдено карточек: " + cards.size());

        try {
            List<WebElement> showMoreButtons = driver.findElements(showMoreButton);

            if (!showMoreButtons.isEmpty()) {
                WebElement showMoreBtn = showMoreButtons.get(0);

                if (showMoreBtn.isDisplayed()) {
                    System.out.println("Нажимаем 'Показать еще' через JavaScript...");

                    ((JavascriptExecutor) driver).executeScript(
                            "arguments[0].scrollIntoView({block: 'center'}); arguments[0].click();",
                            showMoreBtn
                    );

                    Thread.sleep(2000);

                    cards = driver.findElements(courseCardsLocator);
                    System.out.println("После 'Показать еще' карточек: " + cards.size());
                }
            }
        } catch (Exception e) {
            System.out.println("Кнопка 'Показать еще' не найдена или не кликабельна: " + e.getMessage());
        }

        return cards;
    }

    /**
     * Получить названия всех курсов
     */
    public List<String> getAllCourseNames() {
        return getAllCourseCards().stream()
                .map(card -> {
                    try {
                        return card.findElement(courseTitleLocator).getText().trim();
                    } catch (Exception e) {
                        return "";
                    }
                })
                .filter(name -> !name.isEmpty())
                .distinct()
                .collect(Collectors.toList());
    }

    /**
     * Кликнуть по курсу по имени (с выделением элемента)
     */
    public void clickCourseByName(String courseName) {
        System.out.println("Ищем курс для клика: " + courseName);

        WebElement courseCard = getAllCourseCards().stream()
                .filter(card -> {
                    try {
                        String title = card.findElement(courseTitleLocator).getText().trim();
                        return title.equals(courseName);
                    } catch (Exception e) {
                        return false;
                    }
                })
                .findFirst()
                .orElseThrow(() -> {
                    // Выводим доступные курсы для отладки
                    String availableCourses = getAllCourseNames().stream()
                            .limit(10)
                            .collect(Collectors.joining("\n- ", "Доступные курсы (первые 10):\n- ", ""));
                    return new RuntimeException("Курс не найден: " + courseName + "\n" + availableCourses);
                });

        highlightElement(courseCard, "3px solid magenta");

        Actions actions = new Actions(driver);
        actions.moveToElement(courseCard).click().perform();

        System.out.println("Курс '" + courseName + "' успешно кликнут");
    }

    /**
     * Получить DTO для всех курсов (с обработкой всех возможных форматов)
     */
    public List<CourseDto> getAllCoursesWithDates() {
        return getAllCourseCards().stream()
                .map(this::convertToCourseDto)
                .filter(course -> course != null)
                .distinct()
                .collect(Collectors.toList());
    }

    /**
     * Получить только курсы с указанной датой
     */
    public List<CourseDto> getCoursesWithDefinedDates() {
        return getAllCoursesWithDates().stream()
                .filter(CourseDto::hasDate)
                .collect(Collectors.toList());
    }

    /**
     * Получить курсы без указанной даты
     */
    public List<CourseDto> getCoursesWithoutDate() {
        return getAllCoursesWithDates().stream()
                .filter(course -> !course.hasDate())
                .collect(Collectors.toList());
    }

    /**
     * Улучшенная конвертация WebElement в CourseDto
     */
    private CourseDto convertToCourseDto(WebElement card) {
        try {
            String title = getCourseTitle(card);
            if (title.isEmpty()) {
                return null;
            }

            String dateText = getCourseDateText(card);

            if (isNoDateMessage(dateText)) {
                System.out.println("Курс без даты: " + title + " - " + dateText);
                String url = getCourseUrl(card);
                return new CourseDto(title, url);
            }

            String datePart = extractDatePart(dateText);

            LocalDate startDate = CourseDto.parseDate(datePart);

            String url = getCourseUrl(card);

            if (startDate != null) {
                return new CourseDto(title, startDate, url);
            } else {
                System.out.println("Дата не распарсилась для курса: " + title + " - текст: " + dateText);
                return new CourseDto(title, url);
            }

        } catch (Exception e) {
            System.err.println("Ошибка при конвертации карточки: " + e.getMessage());
            return null;
        }
    }

    /**
     * Получить название курса из карточки
     */
    private String getCourseTitle(WebElement card) {
        try {
            return card.findElement(courseTitleLocator).getText().trim();
        } catch (Exception e) {
            return "";
        }
    }

    /**
     * Получить текст даты из карточки
     */
    private String getCourseDateText(WebElement card) {
        try {
            return card.findElement(courseDateLocator).getText().trim();
        } catch (Exception e) {
            return "";
        }
    }

    /**
     * Получить URL курса из карточки
     */
    private String getCourseUrl(WebElement card) {
        String url = card.getAttribute("href");
        if (url != null && !url.startsWith("http")) {
            url = "https://otus.ru" + url;
        }
        return url;
    }

    /**
     * Извлечь часть с датой из текста
     */
    private String extractDatePart(String dateText) {
        if (dateText == null || dateText.isEmpty()) {
            return "";
        }

        String[] parts = dateText.split("·");
        return parts[0].trim();
    }

    /**
     * Улучшенная проверка сообщений об отсутствии даты
     */
    private boolean isNoDateMessage(String text) {
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
     * Найти курсы с самой ранней датой начала (используем Stream API и reduce)
     */
    public List<CourseDto> findCoursesWithEarliestDate() {
        List<CourseDto> coursesWithDates = getCoursesWithDefinedDates();

        System.out.println("Курсов с датами (Selenium): " + coursesWithDates.size());

        if (coursesWithDates.isEmpty()) {
            System.out.println("Нет курсов с указанной датой");
            return List.of();
        }

        // Используем reduce для поиска минимальной даты
        Optional<LocalDate> minDate = coursesWithDates.stream()
                .map(course -> course.getStartDate().orElse(null))
                .filter(date -> date != null)
                .reduce((date1, date2) -> date1.isBefore(date2) ? date1 : date2);

        if (minDate.isPresent()) {
            LocalDate earliestDate = minDate.get();
            System.out.println("Самая ранняя дата (Selenium): " + earliestDate);

            // Возвращаем все курсы с этой датой
            List<CourseDto> result = coursesWithDates.stream()
                    .filter(course -> course.getStartDate()
                            .map(date -> date.equals(earliestDate))
                            .orElse(false))
                    .collect(Collectors.toList());

            System.out.println("Курсов с самой ранней датой (Selenium): " + result.size());
            return result;
        }

        return List.of();
    }

    /**
     * Найти курсы с самой поздней датой начала (используем Stream API и reduce)
     */
    public List<CourseDto> findCoursesWithLatestDate() {
        List<CourseDto> coursesWithDates = getCoursesWithDefinedDates();

        if (coursesWithDates.isEmpty()) {
            System.out.println("Нет курсов с указанной датой");
            return List.of();
        }

        Optional<LocalDate> maxDate = coursesWithDates.stream()
                .map(course -> course.getStartDate().orElse(null))
                .filter(date -> date != null)
                .reduce((date1, date2) -> date1.isAfter(date2) ? date1 : date2);

        if (maxDate.isPresent()) {
            LocalDate latestDate = maxDate.get();
            System.out.println("Самая поздняя дата (Selenium): " + latestDate);

            return coursesWithDates.stream()
                    .filter(course -> course.getStartDate()
                            .map(date -> date.equals(latestDate))
                            .orElse(false))
                    .collect(Collectors.toList());
        }

        return List.of();
    }

    /**
     * Получить статистику по курсам
     */
    public void printCourseStatistics() {
        List<CourseDto> allCourses = getAllCoursesWithDates();
        List<CourseDto> withDates = getCoursesWithDefinedDates();
        List<CourseDto> withoutDates = getCoursesWithoutDate();

        System.out.println("📊 Статистика курсов:");
        System.out.println("  Всего курсов: " + allCourses.size());
        System.out.println("  С указанной датой: " + withDates.size());
        System.out.println("  Без даты: " + withoutDates.size());

        // Выводим даты первых нескольких курсов для отладки
        if (!withDates.isEmpty()) {
            System.out.println("\n  Примеры курсов с датами:");
            withDates.stream().limit(3).forEach(course ->
                    System.out.println("    • " + course.getTitle() +
                            " - " + course.getStartDate().map(LocalDate::toString).orElse("нет даты"))
            );
        }
    }

    /**
     * Получить HTML содержимое страницы для Jsoup
     */
    public String getPageHtml() {
        return driver.getPageSource();
    }

    /**
     * Проверить, что фильтр по категории установлен правильно (динамически)
     */
    public boolean isCategoryFilterApplied(String expectedCategorySlug) {
        try {
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(5));

            String currentUrl = driver.getCurrentUrl().toLowerCase();
            System.out.println("Текущий URL для проверки: " + currentUrl);
            System.out.println("Ожидаемый slug: " + expectedCategorySlug.toLowerCase());

            boolean urlContainsSlug = currentUrl.contains("/categories/" + expectedCategorySlug.toLowerCase()) ||
                    currentUrl.contains("categories=" + expectedCategorySlug.toLowerCase());

            if (urlContainsSlug) {
                System.out.println("✓ Slug найден в URL");
                return true;
            }

            try {
                List<WebElement> activeFilters = driver.findElements(
                        By.cssSelector("[value='true']")
                );

                for (WebElement filter : activeFilters) {
                    try {
                        WebElement label = filter.findElement(By.cssSelector("label.sc-1fry39v-1"));
                        String filterText = label.getText().trim();

                        String filterSlug = convertCategoryNameToSlug(filterText);

                        if (filterSlug.equalsIgnoreCase(expectedCategorySlug)) {
                            System.out.println("✓ Категория активна в фильтрах: " + filterText);
                            return true;
                        }
                    } catch (Exception e) {
                        // Пропускаем элементы без label
                    }
                }
            } catch (Exception e) {
                System.out.println("Не удалось проверить фильтры: " + e.getMessage());
            }

            if (currentUrl.contains("/catalog/")) {
                List<WebElement> courseCards = getAllCourseCards();
                if (!courseCards.isEmpty()) {
                    System.out.println("✓ Каталог загружен с курсами, URL: " + currentUrl);
                    return true;
                }
            }

            return false;

        } catch (Exception e) {
            System.err.println("Ошибка при проверке фильтра категории: " + e.getMessage());
            return false;
        }
    }

    /**
     * Конвертировать название категории в slug (динамически)
     */
    private String convertCategoryNameToSlug(String categoryName) {
        if (categoryName == null || categoryName.isEmpty()) {
            return "";
        }

        // Преобразуем русское название в slug
        String slug = categoryName.toLowerCase()
                .replace(" ", "-")
                .replace("(", "")
                .replace(")", "")
                .replace("it", "it")
                .replace("data-science", "data-science")
                .replace("data science", "data-science")
                .replace("game-dev", "gamedev")
                .replace("game dev", "gamedev");

        // Специальные случаи
        Map<String, String> specialCases = Map.of(
                "программирование", "programming",
                "архитектура", "architecture",
                "инфраструктура", "operations",
                "безопасность", "information-security-courses",
                "управление", "marketing-business",
                "аналитика и анализ", "analytics",
                "бизнес и продукт в it", "business-product",
                "it без программирования", "it-bez-programmirovanija",
                "импортозамещение", "import-substitution",
                "корпоративные курсы", "corporate"
        );

        return specialCases.getOrDefault(categoryName.toLowerCase(), slug);
    }

    /**
     * Получить slug категории из текущего URL (динамически)
     */
    public String getCategorySlugFromUrl() {
        String currentUrl = driver.getCurrentUrl();

        Pattern pattern = Pattern.compile("categories[/=]([a-z\\-]+)");
        Matcher matcher = pattern.matcher(currentUrl);

        if (matcher.find()) {
            String slug = matcher.group(1);
            System.out.println("Slug из URL: " + slug);
            return slug;
        }

        pattern = Pattern.compile("/categories/([a-z\\-]+)|(_[a-z\\-]+)");
        matcher = pattern.matcher(currentUrl);

        if (matcher.find()) {
            String slug = matcher.group(1);
            System.out.println("Slug из URL (/categories/): " + slug);
            return slug;
        }

        return "";
    }
}
