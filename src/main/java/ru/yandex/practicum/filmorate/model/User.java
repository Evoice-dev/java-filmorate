package ru.yandex.practicum.filmorate.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import ru.yandex.practicum.filmorate.exceptions.ValidationException;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

import static ru.yandex.practicum.filmorate.FilmorateApplication.log;

@Data
public class User {
    public int id;
    public String email;
    public String login;
    public String name;
    public LocalDate birthday;
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @JsonCreator
    public User(@JsonProperty("email") String email, @JsonProperty("name") String name, @JsonProperty("login") String login, @JsonProperty("birthday") String birthday) throws ValidationException {
        if (email == null || email.isEmpty()) {
            log.error("Ошибка - Не указана почта пользователя");
            log.error("Пользователь не создан");
            throw new ValidationException("Необходимо указать почту");
        } else if (!email.contains("@")) {
            log.error("Ошибка - Неккоректный формат почты");
            log.error("Пользователь не создан");
            throw new ValidationException("Неккоректный формат почты");
        }
        if (login.isEmpty()) {
            log.error("Ошибка - Необходимо указать логин");
            log.error("Пользователь не создан");
            throw new ValidationException("Необходимо указать логин");
        } else if (login.contains(" ")) {
            log.error("Ошибка - Логин не должен сожержать пробелы");
            log.error("Пользователь не создан");
            throw new ValidationException("Логин не должен сожержать пробелы");
        }

        if (name == null) {
            this.name = login;
        } else {
            this.name = name;
        }

        LocalDate convertBirthday = LocalDate.parse(birthday, formatter);
        LocalDate nowDate = LocalDate.now();

        if (convertBirthday.isAfter(nowDate)) {
            log.error("Ошибка - Дата рождения не может быть в будущем");
            log.error("Пользователь не создан");
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
