package extensions;

import com.google.inject.Guice;
import com.google.inject.Injector;
import config.AllureConfiguration;
import factory.WebDriverFactory;
import io.qameta.allure.Allure;
import modules.PageGuiceModule;
import modules.ToolsGuiceModule;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.openqa.selenium.WebDriver;

import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

public class UIExtension implements BeforeEachCallback, AfterEachCallback {

    private Injector injector;
    private WebDriver driver;
    private String testId;
    private long startTime;

    static {
        AllureConfiguration.configure();
    }

    @Override
    public void afterEach(ExtensionContext context) throws Exception {
        String testName = context.getDisplayName();
        long duration = System.currentTimeMillis() - startTime;

        try {
            Allure.label("duration", String.valueOf(duration));
            Allure.label("test_id", testId);
            System.out.printf("⏱️  Время выполнения теста '%s': %d мс%n", testName, duration);

            resetBrowserState();

        } catch (Exception e) {
            System.err.println("Ошибка в afterEach: " + e.getMessage());
            AllureConfiguration.takeScreenshot(driver, "ERROR in afterEach - " + testName);
        } finally {
            closeBrowser();
            System.out.println("✅ Состояние сброшено для теста: " + testName + "\n");
        }
    }

    @Override
    public void beforeEach(ExtensionContext context) throws Exception {
        testId = UUID.randomUUID().toString();
        startTime = System.currentTimeMillis();

        String testName = context.getDisplayName();
        String className = context.getTestClass().map(Class::getSimpleName).orElse("Unknown");
        String methodName = context.getTestMethod().map(Method::getName).orElse("Unknown");

        System.out.println("\n" + "=".repeat(80));
        System.out.println("🚀 ЗАПУСК ТЕСТА: " + testName);
        System.out.println("📁 Класс: " + className);
        System.out.println("🔧 Метод: " + methodName);
        System.out.println("🆔 ID теста: " + testId);
        System.out.println("=".repeat(80));

        setupAllureForTest(className, methodName, testName, testId);

        driver = WebDriverFactory.getDriver();

        configureBrowser(driver);

        injector = Guice.createInjector(
                new PageGuiceModule(driver),
                new ToolsGuiceModule(driver)
        );

        injector.injectMembers(context.getTestInstance().get());

        Allure.step("✅ Тестовое окружение настроено");
    }

    /**
     * Настройка Allure для конкретного теста
     */
    private void setupAllureForTest(String className, String methodName, String testName, String testId) {
        // Устанавливаем метки для Allure
        Allure.label("testClass", className);
        Allure.label("testMethod", methodName);
        Allure.label("testId", testId);
        Allure.label("startTime", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));

        // Устанавливаем эпик и фичу в зависимости от класса теста
        if (className.contains("CatalogCourse")) {
            Allure.epic("Каталог курсов");
            Allure.feature("Сценарий 1: Поиск курса по имени");
            Allure.story("Поиск и открытие курса из каталога");
        } else if (className.contains("CatalogCoursesDate")) {
            Allure.epic("Каталог курсов");
            Allure.feature("Сценарий 2: Поиск по датам");
            Allure.story("Поиск курсов с самой ранней и поздней датой начала");
        } else if (className.contains("MainPage") || className.contains("Scenario3")) {
            Allure.epic("Главная страница");
            Allure.feature("Сценарий 3: Навигация по меню");
            Allure.story("Выбор случайной категории из меню 'Обучение'");
        }

        Allure.description(String.format(
                "Тест: %s\n" +
                        "Класс: %s\n" +
                        "Метод: %s\n" +
                        "Тест ID: %s\n" +
                        "Время начала: %s",
                testName, className, methodName, testId,
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
        ));

        AllureConfiguration.addSystemInfo();
    }

    /**
     * Настройка браузера
     */
    private void configureBrowser(WebDriver driver) {
        Allure.step("Настройка браузера", () -> {
            try {
                driver.manage().window().maximize();
                driver.manage().deleteAllCookies();
                driver.get("about:blank");
                System.out.println("🌐 Браузер настроен и готов к работе");
            } catch (Exception e) {
                System.err.println("Ошибка настройки браузера: " + e.getMessage());
                throw e;
            }
        });
    }

    /**
     * Сброс состояния браузера
     */
    private void resetBrowserState() {
        Allure.step("Сброс состояния браузера", () -> {
            try {
                if (driver != null) {
                    // Очистка cookies
                    driver.manage().deleteAllCookies();
                    driver.get("about:blank");

                    System.out.println("🔄 Состояние браузера сброшено");
                }
            } catch (Exception e) {
                System.err.println("Ошибка при сбросе состояния: " + e.getMessage());
            }
        });
    }

    /**
     * Закрытие браузера
     */
    private void closeBrowser() {
        try {
            if (driver != null) {
                driver.quit();
                driver = null;
            }
        } catch (Exception e) {
            System.err.println("Ошибка при закрытии браузера: " + e.getMessage());
        }
    }

}
