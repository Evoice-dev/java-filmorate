package ru.yandex.practicum.filmorate.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import ru.yandex.practicum.filmorate.exceptions.ValidationException;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import static ru.yandex.practicum.filmorate.FilmorateApplication.log;

@Data
public class User {
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private int id;
    private String email;
    private String login;
    private String name;
    private LocalDate birthday;
    private Set<Long> friends = new HashSet<>();

    @JsonCreator
    public User(@JsonProperty("email") String email,
                @JsonProperty("name") String name,
                @JsonProperty("login") String login,
                @JsonProperty("birthday") String birthday) throws ValidationException {
        if (email == null || email.isEmpty()) {
            log.error("Ошибка - Не указана почта пользователя");
            throw new ValidationException("Необходимо указать почту");
        }
        if (!email.contains("@")) {
            log.error("Ошибка - Некорректный формат почты");
            throw new ValidationException("Неккоректный формат почты");
        }

        if (login == null || login.isEmpty()) {
            log.error("Ошибка - Необходимо указать логин");
            throw new ValidationException("Необходимо указать логин");
        }
        if (login.contains(" ")) {
            log.error("Ошибка - Логин не должен содержать пробелы");
            throw new ValidationException("Логин не должен сожержать пробелы");
        }

        if (name == null || name.isEmpty()) {
            this.name = login;
        } else {
            this.name = name;
        }

        if (birthday == null) {
            throw new ValidationException("Необходимо указать дату рождения");
        }

        LocalDate convertBirthday = LocalDate.parse(birthday, formatter);
        if (convertBirthday.isAfter(LocalDate.now())) {
            log.error("Ошибка - Дата рождения не может быть в будущем");
            throw new ValidationException("Дата рождения не может быть в будущем");
        }

        this.email = email;
        this.login = login;
        this.birthday = convertBirthday;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return id == user.id;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}