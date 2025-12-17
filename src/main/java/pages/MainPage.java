package pages;

import annotations.Path;
import org.openqa.selenium.*;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

@Path("/")
public class MainPage extends AbsBasePage<MainPage> {

    private final By popupContainer = By.cssSelector("div.sc-piuiz2-1.kdOQht");
    private final By allCoursesSection = By.xpath("//p[text()='Все курсы']/following-sibling::div");
    private final By specificCategoryLinks = By.xpath("//p[text()='Все курсы']/following-sibling::div//a[contains(@href, '/categories/')]");
    private String selectedCategorySlug;

    public MainPage(WebDriver driver) {
        super(driver);
    }

    /**
     * Найти и навести на элемент "Обучение"
     */
    public MainPage hoverOverTrainingMenu() {
        System.out.println("Наводим курсор на меню 'Обучение'...");

        WebElement trainingElement = findTrainingElement();

        if (trainingElement == null) {
            throw new RuntimeException("Элемент 'Обучение' не найден на странице");
        }

        System.out.println("Найден элемент: " + trainingElement.getTagName() +
                " с текстом: '" + trainingElement.getText() + "'");

        highlightElement(trainingElement, "3px solid #FF0000");

        Actions actions = new Actions(driver);
        actions.moveToElement(trainingElement).perform();

        System.out.println("Курсор наведен на элемент 'Обучение'");

        waitForPopupToAppear();

        return this;
    }

    private WebElement findTrainingElement() {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));

        // Способ 1: По точному тексту (надежнее)
        List<WebElement> elements = driver.findElements(By.xpath("//*[text()='Обучение' or contains(text(), 'Обучение')]"));

        for (WebElement element : elements) {
            try {
                if (element.isDisplayed()) {
                    System.out.println("Найден элемент 'Обучение': " + element.getTagName());
                    return element;
                }
            } catch (Exception ignored) {
            }
        }

        try {
            return driver.findElement(By.xpath("//div[contains(@class, 'sc-piuiz2-')]//ancestor::div[contains(text(), 'Обучение') or contains(@class, 'training')]"));
        } catch (Exception ignored) {
        }

        return null;
    }

    /**
     * Найти родительский контейнер для элемента
     */
    private WebElement findParentContainer(WebElement element) {
        try {
            WebElement parent = (WebElement) ((JavascriptExecutor) driver)
                    .executeScript("return arguments[0].closest('div');", element);
            return parent;
        } catch (Exception e) {
            return element;
        }
    }

    /**
     * Ожидать появления попапа
     */
    private void waitForPopupToAppear() {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(5));

        try {
            System.out.println("Ожидаем появления попапа...");
            wait.until(ExpectedConditions.visibilityOfElementLocated(popupContainer));
            System.out.println("✓ Попап появился");

            Thread.sleep(1000);

        } catch (TimeoutException e) {
            System.out.println("Попап не появился автоматически, проверяем альтернативно...");
            checkPopupAlternative();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Альтернативная проверка попапа
     */
    private void checkPopupAlternative() {
        try {
            List<WebElement> sections = driver.findElements(allCoursesSection);
            if (!sections.isEmpty()) {
                System.out.println("✓ Секция 'Все курсы' найдена");
                return;
            }

            List<WebElement> categories = driver.findElements(specificCategoryLinks);
            if (!categories.isEmpty()) {
                System.out.println("✓ Найдены ссылки на категории: " + categories.size());
                return;
            }

            System.out.println("⚠️ Попап не найден альтернативными методами");

        } catch (Exception e) {
            System.err.println("Ошибка при альтернативной проверке: " + e.getMessage());
        }
    }


    public List<WebElement> getCourseCategories() {
        System.out.println("Получаем категории курсов из попапа...");

        hoverOverTrainingMenu();

        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(5));
        wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//p[text()='Направления']/following-sibling::div")
        ));

        List<WebElement> categories = driver.findElements(
                By.cssSelector(".sc-4zz0i4-0.dZepSJ")
        );

        System.out.println("Найдено ссылок категорий: " + categories.size());

        return categories.stream()
                .filter(cat -> {
                    try {
                        String href = cat.getAttribute("href");
                        String text = cat.getText().trim();

                        // Проверяем, что это категория курса
                        return href != null &&
                                href.contains("/categories/") &&
                                !text.isEmpty() &&
                                !text.contains("Мои курсы") &&
                                !text.contains("Показать все") &&
                                !text.contains("События") &&
                                !text.contains("Другое");
                    } catch (Exception e) {
                        return false;
                    }
                })
                .collect(Collectors.toList());
    }

    /**
     * Получить названия всех категорий
     */
    public List<String> getCategoryNames() {
        return getCourseCategories().stream()
                .map(WebElement::getText)
                .map(String::trim)
                .filter(text -> !text.isEmpty())
                .distinct()
                .toList();
    }

    /**
     * Выбрать случайную категорию курсов и вернуть её slug
     */
    public String selectRandomCategory() {
        System.out.println("\nВыбираем случайную категорию курсов...");

        List<WebElement> categories = getCourseCategories();

        if (categories.isEmpty()) {
            System.out.println("⚠️ Категории не найдены. Открываем общий каталог.");
            driver.get("https://otus.ru/catalog/courses");
            return "Все курсы";
        }

        Random random = new Random();
        int randomIndex = random.nextInt(categories.size());
        WebElement randomCategory = categories.get(randomIndex);

        String categoryName = randomCategory.getText().trim();
        String categoryUrl = randomCategory.getAttribute("href");

        String categorySlug = extractSlugFromUrl(categoryUrl);
        this.selectedCategorySlug = categorySlug; // Сохраняем slug

        System.out.println("🎲 Выбрана категория: " + categoryName);
        System.out.println("🔗 URL: " + categoryUrl);
        System.out.println("📝 Slug: " + categorySlug);

        highlightElement(randomCategory, "3px solid #00FF00");

        try {
            System.out.println("🖱️ Кликаем по категории...");
            randomCategory.click();

            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
            wait.until(ExpectedConditions.or(
                    ExpectedConditions.urlContains("categories"),
                    ExpectedConditions.urlContains("catalog/courses")
            ));

            System.out.println("✓ Переход на страницу категории выполнен");

        } catch (Exception e) {
            System.err.println("❌ Ошибка при клике: " + e.getMessage());
            System.out.println("🔄 Открываем URL напрямую...");
            driver.get(categoryUrl);
        }

        return categoryName;
    }

    /**
     * Извлечь slug из URL категории
     */
    private String extractSlugFromUrl(String url) {
        if (url == null || url.isEmpty()) {
            return "";
        }

        try {

            String[] parts = url.split("/");

            for (int i = parts.length - 1; i >= 0; i--) {
                if (!parts[i].isEmpty() &&
                        !parts[i].contains("otus.ru") &&
                        !parts[i].contains("http")) {

                    String slug = parts[i].split("\\?")[0];
                    System.out.println("Извлечен slug из URL: " + slug);
                    return slug;
                }
            }
        } catch (Exception e) {
            System.err.println("Ошибка при извлечении slug из URL: " + e.getMessage());
        }

        return "";
    }

    /**
     * Получить slug выбранной категории
     */
    public String getSelectedCategorySlug() {
        return selectedCategorySlug;
    }

}
