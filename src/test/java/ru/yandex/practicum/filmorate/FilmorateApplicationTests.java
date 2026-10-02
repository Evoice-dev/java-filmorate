package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import ru.yandex.practicum.filmorate.exceptions.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class FilmorateApplicationTests {

    @Test
    @DisplayName("Корректный фильм создаётся")
    void shouldCreateValidFilm() throws ValidationException {
        Film film = new Film("Inception", "Dreams", "2010-07-16", 148);

        assertEquals("Inception", film.getName());
        assertEquals("Dreams", film.getDescription());
        assertEquals(2010, film.getReleaseDate().getYear());
        assertEquals(7, film.getReleaseDate().getMonthValue());
        assertEquals(16, film.getReleaseDate().getDayOfMonth());
    }

    @Test
    @DisplayName("Пустое название")
    void shouldRejectEmptyName() {
        ValidationException ex = assertThrows(ValidationException.class,
                () -> new Film("", "Desc", "2010-07-16", 100));
        assertEquals("Название не может быть пустым", ex.getMessage());
    }


    @Test
    @DisplayName("Описание ровно 200 символов - допустимо")
    void shouldAccept200CharDescription() throws ValidationException {
        String desc = "a".repeat(200);
        assertDoesNotThrow(() -> new Film("Name", desc, "2010-07-16", 100));
    }

    @Test
    @DisplayName("Описание 201 символ")
    void shouldReject201CharDescription() {
        String desc = "a".repeat(201);
        ValidationException ex = assertThrows(ValidationException.class,
                () -> new Film("Name", desc, "2010-07-16", 100));
        assertEquals("Максимальная длина описания - 200 символов", ex.getMessage());
    }

    @Test
    @DisplayName("Дата релиза ровно 28.12.1895 - допустима")
    void shouldAcceptFirstEverFilmDate() throws ValidationException {
        Film film = new Film("L'Arrivée d'un train", "First film",
                "1895-12-28", 1);
        assertNotNull(film.getReleaseDate());
    }

    @Test
    @DisplayName("Дата релиза до 28.12.1895")
    void shouldRejectReleaseBeforeMinimum() {
        ValidationException ex = assertThrows(ValidationException.class,
                () -> new Film("Name", "Desc", "1895-12-19", 100));
        assertEquals("Дата релиза должна быть не раньше 28 декабря 1895 года", ex.getMessage());
    }

    @Test
    @DisplayName("Дата релиза ровно 28.12.1895 - проходит по коду (граничный случай)")
    void shouldAcceptExactlyMinimumDateInCode() throws ValidationException {
        Film film = new Film("Name", "Desc", "1895-12-28", 1);
        assertNotNull(film.getReleaseDate());
    }

    @Test
    @DisplayName("duration = 0 - допустимо")
    void shouldAcceptZeroDuration() throws ValidationException {
        Film film = new Film("Name", "Desc", "2010-07-16", 0);
        assertEquals(0, film.getDuration());
    }

    @Test
    @DisplayName("duration < 0")
    void shouldRejectNegativeDuration() {
        ValidationException ex = assertThrows(ValidationException.class,
                () -> new Film("Name", "Desc", "2010-07-16", -1));
        assertEquals("Продолжительность должна быть положительным числом", ex.getMessage());
    }

    @Test
    @DisplayName("equals работает по id")
    void shouldCompareById() throws ValidationException {
        Film a = new Film("A", "D", "2000-01-01", 90);
        Film b = new Film("B", "D", "2001-02-02", 100);
        a.setId(1);
        b.setId(1);
        assertEquals(a, b);
    }

    @Test
    @DisplayName("Корректный пользователь создаётся")
    void shouldCreateValidUser() throws ValidationException {
        User user = new User("test@mail.ru", "Test", "testlogin", "2002-03-31");

        assertEquals("test@mail.ru", user.getEmail());
        assertEquals("testlogin", user.getLogin());
        assertEquals("Test", user.getName());
        assertEquals(2002, user.getBirthday().getYear());
        assertEquals(3, user.getBirthday().getMonthValue());
        assertEquals(31, user.getBirthday().getDayOfMonth());
    }

    @Test
    @DisplayName("Пустое имя заменяется логином")
    void shouldUseLoginAsNameIfNameIsEmpty() throws ValidationException {
        User user = new User("test@mail.ru", "", "testlogin", "2002-03-31");
        assertEquals("testlogin", user.getName());
    }

    @Test
    @DisplayName("Дата рождения = сегодняшний день - допустима")
    void shouldAcceptBirthdayToday() throws ValidationException {
        String today = java.time.LocalDate.now()
                .format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        User user = new User("test@mail.ru", "Test", "testlogin", today);
        assertNotNull(user.getBirthday());
    }

    @Test
    @DisplayName("Пустой email")
    void shouldRejectEmptyEmail() {
        ValidationException ex = assertThrows(ValidationException.class,
                () -> new User("", "Test", "testlogin", "2002-03-31"));
        assertEquals("Необходимо указать почту", ex.getMessage());
    }

    @Test
    @DisplayName("Email без @")
    void shouldRejectEmailWithoutAt() {
        ValidationException ex = assertThrows(ValidationException.class,
                () -> new User("testmail.ru", "Test", "testlogin", "2002-03-31"));
        assertEquals("Неккоректный формат почты", ex.getMessage());
    }

    @Test
    @DisplayName("Пустой login")
    void shouldRejectEmptyLogin() {
        ValidationException ex = assertThrows(ValidationException.class,
                () -> new User("test@mail.ru", "Test", "", "2002-03-31"));
        assertEquals("Необходимо указать логин", ex.getMessage());
    }

    @Test
    @DisplayName("Login с пробелом")
    void shouldRejectLoginWithSpace() {
        ValidationException ex = assertThrows(ValidationException.class,
                () -> new User("test@mail.ru", "Test", "test login", "2002-03-31"));
        assertEquals("Логин не должен сожержать пробелы", ex.getMessage());
    }

    @Test
    @DisplayName("Дата рождения в будущем")
    void shouldRejectFutureBirthday() {
        String tomorrow = java.time.LocalDate.now().plusDays(1)
                .format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        ValidationException ex = assertThrows(ValidationException.class,
                () -> new User("test@mail.ru", "Test", "testlogin", tomorrow));
        assertEquals("Дата рождения не может быть в будущем", ex.getMessage());
    }

    @Test
    @DisplayName("Неверный формат даты")
    void shouldRejectInvalidDateFormat() {
        assertThrows(java.time.format.DateTimeParseException.class,
                () -> new User("test@mail.ru", "Test", "testlogin", "30.03.2002"));
    }

    @Test
    @DisplayName("email = null")
    void shouldRejectNullEmail() {
        assertThrows(ValidationException.class,
                () -> new User(null, "Test", "testlogin", "2002-03-31"));
    }

    @Test
    @DisplayName("equals работает по id")
    void shouldCompareByIdUser() throws ValidationException {
        User a = new User("a@mail.ru", "A", "a", "2000-01-01");
        User b = new User("b@mail.ru", "B", "b", "2001-02-02");
        a.setId(1);
        b.setId(1);
        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
    }

}
