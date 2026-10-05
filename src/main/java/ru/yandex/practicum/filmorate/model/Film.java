package ru.yandex.practicum.filmorate.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import ru.yandex.practicum.filmorate.exceptions.ValidationException;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.Set;

import static ru.yandex.practicum.filmorate.FilmorateApplication.log;

@Data
@EqualsAndHashCode(of = "id")
public class Film {
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final LocalDate MINIMUM_DATE = LocalDate.parse("1895-12-28", formatter);

    private int id;
    private String name;
    private String description;
    private LocalDate releaseDate;
    private int duration;
    private Set<Long> likes = new HashSet<>();

    @JsonCreator
    public Film(@JsonProperty("name") String name,
                @JsonProperty("description") String description,
                @JsonProperty("releaseDate") String releaseDate,
                @JsonProperty("duration") int duration) throws ValidationException {
        if (name == null || name.isEmpty()) {
            log.error("Ошибка - Название не может быть пустым");
            throw new ValidationException("Название не может быть пустым");
        }

        if (description == null) {
            description = "";
        }
        if (description.length() > 200) {
            log.error("Ошибка - Максимальная длина описания - 200 символов");
            throw new ValidationException("Максимальная длина описания - 200 символов");
        }

        if (releaseDate == null) {
            throw new ValidationException("Необходимо указать дату релиза");
        }

        LocalDate convertRelease = LocalDate.parse(releaseDate, formatter);

        if (convertRelease.isBefore(MINIMUM_DATE)) {
            log.error("Ошибка - Дата релиза должна быть не раньше 28 декабря 1895 года");
            throw new ValidationException("Дата релиза должна быть не раньше 28 декабря 1895 года");
        }

        if (duration < 0) {
            log.error("Ошибка - Продолжительность должна быть положительным числом");
            throw new ValidationException("Продолжительность должна быть положительным числом");
        }

        this.name = name;
        this.description = description;
        this.releaseDate = convertRelease;
        this.duration = duration;
    }
}