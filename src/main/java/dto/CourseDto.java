package dto;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Optional;

public class CourseDto {
    private final String title;
    private final Optional<LocalDate> startDate; // Используем Optional для даты
    private final String url;
    private final boolean hasDate;

    public CourseDto(String title, LocalDate startDate, String url) {
        this.title = title;
        this.startDate = Optional.ofNullable(startDate);
        this.url = url;
        this.hasDate = startDate != null;
    }

    public CourseDto(String title, String url) {
        this.title = title;
        this.startDate = Optional.empty();
        this.url = url;
        this.hasDate = false;
    }

    public String getTitle() {
        return title;
    }

    public Optional<LocalDate> getStartDate() {
        return startDate;
    }

    public String getUrl() {
        return url;
    }

    public boolean hasDate() {
        return hasDate;
    }

    /**
     * Парсинг даты из строки
     */
    public static LocalDate parseDate(String dateString) {
        if (dateString == null || dateString.trim().isEmpty()) {
            return null;
        }

        String lowerDate = dateString.toLowerCase();
        if (lowerDate.contains("объявлено позже") ||
                lowerDate.contains("дата уточняется") ||
                lowerDate.contains("будет объявлено") ||
                lowerDate.contains("не указана")) {
            return null;
        }

        try {
            String cleanDate = dateString.trim()
                    .replace("·", "")
                    .replace("•", "")
                    .trim();

            String[] parts = cleanDate.split("\\s+");
            if (parts.length >= 3) {
                String day = parts[0];
                String month = parts[1].replace(",", "");
                String year = parts[2];

                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("d MMMM yyyy", new Locale("ru"));
                return LocalDate.parse(day + " " + month + " " + year, formatter);
            }
        } catch (Exception e) {
            System.err.println("Ошибка парсинга даты: '" + dateString + "' - " + e.getMessage());
        }
        return null;
    }

    @Override
    public String toString() {
        return String.format("Course{title='%s', startDate=%s, hasDate=%s, url='%s'}",
                title,
                startDate.map(LocalDate::toString).orElse("не указана"),
                hasDate,
                url);
    }
}
