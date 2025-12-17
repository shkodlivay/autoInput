package listeners;

import io.qameta.allure.Allure;
import io.qameta.allure.Attachment;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.events.WebDriverListener;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;

public class AllureListener implements WebDriverListener {

    private final WebDriver driver;

    public AllureListener(WebDriver driver) {
        this.driver = driver;
    }

    /**
     * Прикрепить скриншот к Allure отчету
     */
    @Attachment(value = "Скриншот при ошибке", type = "image/png")
    public byte[] takeScreenshot() {
        return ((TakesScreenshot) driver).getScreenshotAs(OutputType.BYTES);
    }

    /**
     * Прикрепить скриншот как байты
     */
    public void attachScreenshot(String name) {
        Allure.addAttachment(name, "image/png",
                new ByteArrayInputStream(takeScreenshot()), "png");
    }

    /**
     * Прикрепить HTML страницы
     */
    @Attachment(value = "HTML страницы", type = "text/html")
    public String attachPageHtml() {
        return driver.getPageSource();
    }

    /**
     * Прикрепить текст
     */
    @Attachment(value = "{0}", type = "text/plain")
    public String attachText(String name, String text) {
        return text;
    }

    /**
     * Прикрепить JSON
     */
    @Attachment(value = "{0}", type = "application/json")
    public byte[] attachJson(String name, String json) {
        return json.getBytes(StandardCharsets.UTF_8);
    }

    /**
     * Сделать скриншот элемента
     */
    @Attachment(value = "Скриншот элемента", type = "image/png")
    public byte[] takeElementScreenshot(WebElement element) {
        return element.getScreenshotAs(OutputType.BYTES);
    }

    /**
     * Обработка исключений в тестах
     */
    public void onTestFailure(Throwable throwable) {
        takeScreenshot();
        attachPageHtml();

        attachText("Ошибка теста", throwable.getMessage());
        if (throwable.getCause() != null) {
            attachText("Причина ошибки", throwable.getCause().toString());
        }
    }

    /**
     * Обработка успешного завершения теста
     */
    public void onTestSuccess() {
        attachText("URL страницы", driver.getCurrentUrl());
        attachText("Заголовок страницы", driver.getTitle());
    }
}
