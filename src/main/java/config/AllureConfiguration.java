package config;

import io.qameta.allure.Allure;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;

import java.io.ByteArrayInputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class AllureConfiguration {

    /**
     * Настройка Allure перед запуском тестов
     */
    public static void configure() {
        System.setProperty("allure.results.directory", "target/allure-results");
        System.setProperty("allure.link.issue.pattern", "https://example.com/issue/{}");
        System.setProperty("allure.link.tms.pattern", "https://example.com/tms/{}");
    }

    /**
     * Сделать скриншот и прикрепить к отчету
     */
    public static void takeScreenshot(WebDriver driver, String name) {
        if (driver == null) {
            System.err.println("WebDriver is null, cannot take screenshot");
            return;
        }

        try {
            if (driver instanceof TakesScreenshot) {
                byte[] screenshot = ((TakesScreenshot) driver).getScreenshotAs(OutputType.BYTES);
                Allure.addAttachment(
                        name + " - " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")),
                        "image/png",
                        new ByteArrayInputStream(screenshot),
                        ".png"
                );
                System.out.println("📸 Скриншот сохранен: " + name);
            }
        } catch (Exception e) {
            System.err.println("Не удалось сделать скриншот: " + e.getMessage());
        }
    }

    /**
     * Прикрепить текст к отчету
     */
    public static void attachText(String name, String content) {
        Allure.addAttachment(name, "text/plain", content);
    }

    /**
     * Прикрепить HTML к отчету
     */
    public static void attachHtml(String name, String html) {
        Allure.addAttachment(name, "text/html", html);
    }

    /**
     * Добавить информацию о системе
     */
    public static void addSystemInfo() {
        String systemInfo = String.format(
                "=== System Information ===\n" +
                        "OS: %s\n" +
                        "OS Version: %s\n" +
                        "OS Arch: %s\n" +
                        "Java Version: %s\n" +
                        "User: %s\n" +
                        "Time: %s\n" +
                        "==========================",
                System.getProperty("os.name"),
                System.getProperty("os.version"),
                System.getProperty("os.arch"),
                System.getProperty("java.version"),
                System.getProperty("user.name"),
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
        );

        attachText("System Information", systemInfo);
    }
}
