package ru.yandex.practicum.filmorate.service;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.exceptions.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.genre.GenreStorage;
import ru.yandex.practicum.filmorate.storage.mpa.MpaStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

@Service
@Validated
public class FilmService {
    private final FilmStorage filmStorage;
    private final UserStorage userStorage;
    private final MpaStorage mpaStorage;
    private final GenreStorage genreStorage;

    public FilmService(@Qualifier("filmDbStorage") FilmStorage filmStorage,
                       @Qualifier("userDbStorage") UserStorage userStorage,
                       MpaStorage mpaStorage,
                       GenreStorage genreStorage) {
        this.filmStorage = filmStorage;
        this.userStorage = userStorage;
        this.mpaStorage = mpaStorage;
        this.genreStorage = genreStorage;
    }

    public Film addFilm(@Valid Film film) {
        prepareMpaAndGenres(film);
        return filmStorage.add(film);
    }

    public Film updateFilm(@Valid Film film) {
        getFilmById(film.getId());
        prepareMpaAndGenres(film);
        return filmStorage.update(film);
    }

    public List<Film> getAllFilms() {
        return filmStorage.getAll();
    }

    public Film getFilmById(int id) {
        return filmStorage.getById(id)
                .orElseThrow(() -> new NotFoundException("Фильм с id=" + id + " не найден"));
    }

    public void addLike(int filmId, int userId) {
        getFilmById(filmId);
        checkUser(userId);
        filmStorage.addLike(filmId, userId);
    }

    public void removeLike(int filmId, int userId) {
        getFilmById(filmId);
        checkUser(userId);
        filmStorage.removeLike(filmId, userId);
    }

    public List<Film> getPopularFilms(int count) {
        if (count <= 0) {
            throw new ValidationException("Параметр count должен быть положительным");
        }
        return filmStorage.getPopular(count);
    }

    private void checkUser(int userId) {
        if (userStorage.getById(userId).isEmpty()) {
            throw new NotFoundException("Пользователь с id=" + userId + " не найден");
        }
    }

    private void prepareMpaAndGenres(Film film) {
        if (film.getMpa() != null) {
            int mpaId = film.getMpa().getId();
            film.setMpa(mpaStorage.getById(mpaId)
                    .orElseThrow(() -> new NotFoundException("Рейтинг MPA с id=" + mpaId + " не найден")));
        }

        if (film.getGenres() == null || film.getGenres().isEmpty()) {
            film.setGenres(new ArrayList<>());
            return;
        }

        Set<Integer> genreIds = film.getGenres().stream()
                .map(Genre::getId)
                .collect(Collectors.toCollection(TreeSet::new));
        List<Genre> genres = genreStorage.getByIds(genreIds);
        if (genres.size() != genreIds.size()) {
            Set<Integer> found = genres.stream().map(Genre::getId).collect(Collectors.toSet());
            genreIds.removeAll(found);
            throw new NotFoundException("Жанры с id=" + genreIds + " не найдены");
        }
        film.setGenres(new ArrayList<>(genres));
    }
}
