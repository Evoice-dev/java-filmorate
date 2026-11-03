package ru.yandex.practicum.filmorate.storage.film;

import ru.yandex.practicum.filmorate.model.Film;

import java.util.List;
import java.util.Optional;

public interface FilmStorage {
    Film add(Film film);

    Film update(Film film);

    Optional<Film> getById(int id);

    List<Film> getAll();

    void delete(int id);
}