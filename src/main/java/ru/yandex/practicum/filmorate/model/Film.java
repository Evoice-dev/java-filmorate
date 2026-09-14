package ru.yandex.practicum.filmorate.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import ru.yandex.practicum.filmorate.exceptions.ValidationException;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

import static ru.yandex.practicum.filmorate.FilmorateApplication.log;

@Data
public class Film {
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final LocalDate MINIMUM_DATE = LocalDate.parse("1895-12-28", formatter);
    public int id;
    public String name;
    public String description;
    public LocalDate releaseDate;
    public int duration;

    @JsonCreator
    public Film(@JsonProperty("name") String name, @JsonProperty("description") String description, @JsonProperty("releaseDate") String releaseDate, @JsonProperty("duration") int duration) throws ValidationException {
        if ((name == null || name.isEmpty())) {
            log.error("Ошибка - Название не может быть пустым");
            log.error("Фильм не добавлен");
            throw new ValidationException("Название не может быть пустым");
        }
        if (description.length() > 200) {
            log.error("Ошибка - Максимальная длина описания - 200 символов");
            log.error("Фильм не добавлен");
            throw new ValidationException("Максимальная длина описания - 200 символов");
        }

        LocalDate convertRelease = LocalDate.parse(releaseDate, formatter);

        if (convertRelease.isBefore(MINIMUM_DATE)) {
            log.error("Ошибка - Дата релиза должна быть не раньше 28 декабря 1895 года");
            log.error("Фильм не добавлен");
            throw new ValidationException("Дата релиза должна быть не раньше 28 декабря 1895 года");
        }

        if (duration < 0) {
            log.error("Ошибка - Продолжительность должна быть положительным числом");
            log.error("Фильм не добавлен");
            throw new ValidationException("Продолжительность должна быть положительным числом");
        }

        this.name = name;
        this.description = description;
        this.releaseDate = convertRelease;
        this.duration = duration;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Film film = (Film) o;
        return id == film.id;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
