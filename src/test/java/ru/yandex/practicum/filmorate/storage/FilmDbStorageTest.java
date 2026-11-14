package ru.yandex.practicum.filmorate.storage;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.film.FilmDbStorage;
import ru.yandex.practicum.filmorate.storage.user.UserDbStorage;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@JdbcTest
@AutoConfigureTestDatabase
@Import({FilmDbStorage.class, UserDbStorage.class})
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class FilmDbStorageTest {
    private final FilmDbStorage filmStorage;
    private final UserDbStorage userStorage;

    private Film newFilm(String name, int mpaId, int... genreIds) {
        Film film = new Film(name, "Описание " + name, "2001-02-03", 120);
        film.setMpa(new Mpa(mpaId, null));
        for (int genreId : genreIds) {
            film.getGenres().add(new Genre(genreId, null));
        }
        return film;
    }

    private User newUser(String login) {
        return userStorage.add(new User(login + "@mail.ru", login, login, "1990-01-01"));
    }

    @Test
    void testAddAndFindFilmById() {
        Film created = filmStorage.add(newFilm("Фильм", 3, 2, 1));

        assertThat(filmStorage.getById(created.getId()))
                .isPresent()
                .hasValueSatisfying(film -> {
                    assertThat(film).hasFieldOrPropertyWithValue("id", created.getId());
                    assertThat(film).hasFieldOrPropertyWithValue("name", "Фильм");
                    assertThat(film).hasFieldOrPropertyWithValue("duration", 120);
                    assertThat(film.getReleaseDate()).hasToString("2001-02-03");
                    assertThat(film.getMpa().getId()).isEqualTo(3);
                    assertThat(film.getMpa().getName()).isEqualTo("PG-13");
                    assertThat(film.getGenres()).extracting(Genre::getId).containsExactly(1, 2);
                    assertThat(film.getGenres()).extracting(Genre::getName).containsExactly("Комедия", "Драма");
                });
    }

    @Test
    void testFindUnknownFilm() {
        assertThat(filmStorage.getById(9999)).isEmpty();
    }

    @Test
    void testUpdateFilm() {
        Film created = filmStorage.add(newFilm("Старый", 1, 1, 2));
        Film changed = newFilm("Новый", 4, 6);
        changed.setId(created.getId());

        Film updated = filmStorage.update(changed);

        assertThat(updated).hasFieldOrPropertyWithValue("name", "Новый");
        assertThat(updated.getMpa().getName()).isEqualTo("R");
        assertThat(updated.getGenres()).extracting(Genre::getId).containsExactly(6);
    }

    @Test
    void testUpdateUnknownFilm() {
        Film film = newFilm("Нет", 1);
        film.setId(9999);

        assertThatThrownBy(() -> filmStorage.update(film)).isInstanceOf(NotFoundException.class);
    }

    @Test
    void testGetAll() {
        Film a = filmStorage.add(newFilm("A", 1));
        Film b = filmStorage.add(newFilm("B", 2));

        assertThat(filmStorage.getAll()).extracting(Film::getId).contains(a.getId(), b.getId());
    }

    @Test
    void testDelete() {
        Film film = filmStorage.add(newFilm("Удалить", 1, 1));

        filmStorage.delete(film.getId());

        assertThat(filmStorage.getById(film.getId())).isEmpty();
    }

    @Test
    void testAddAndRemoveLike() {
        Film film = filmStorage.add(newFilm("Лайк", 1));
        User user = newUser("liker");

        filmStorage.addLike(film.getId(), user.getId());
        assertThat(filmStorage.getById(film.getId()).orElseThrow().getLikes()).containsExactly(user.getId());

        filmStorage.removeLike(film.getId(), user.getId());
        assertThat(filmStorage.getById(film.getId()).orElseThrow().getLikes()).isEmpty();
    }

    @Test
    void testGetPopular() {
        Film film1 = filmStorage.add(newFilm("Один", 1));
        Film film2 = filmStorage.add(newFilm("Два", 1));
        Film film3 = filmStorage.add(newFilm("Три", 1));
        User u1 = newUser("u1");
        User u2 = newUser("u2");
        User u3 = newUser("u3");
        filmStorage.addLike(film2.getId(), u1.getId());
        filmStorage.addLike(film2.getId(), u2.getId());
        filmStorage.addLike(film2.getId(), u3.getId());
        filmStorage.addLike(film3.getId(), u1.getId());
        filmStorage.addLike(film3.getId(), u2.getId());
        filmStorage.addLike(film1.getId(), u1.getId());

        List<Film> popular = filmStorage.getPopular(2);

        assertThat(popular).extracting(Film::getId).containsExactly(film2.getId(), film3.getId());
    }
}
