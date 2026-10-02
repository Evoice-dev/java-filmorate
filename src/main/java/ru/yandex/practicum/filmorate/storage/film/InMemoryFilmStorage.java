package ru.yandex.practicum.filmorate.storage.film;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static ru.yandex.practicum.filmorate.FilmorateApplication.log;

@Component
public class InMemoryFilmStorage implements FilmStorage {
    private final Map<Integer, Film> films = new LinkedHashMap<>();
    private int nextId = 1;

    @Override
    public Film add(Film film) {
        log.info("Попытка добавить фильм");
        film.setId(nextId++);
        films.put(film.getId(), film);
        log.info("Фильм успешно добавлен");
        return film;
    }

    @Override
    public Film update(Film film) {
        log.info("Попытка редактирования фильма");
        if (!films.containsKey(film.getId())) {
            throw new NotFoundException("Такого фильма не существует");
        }
        films.put(film.getId(), film);
        log.info("Фильм успешно отредактирован");
        return film;
    }

    @Override
    public Film getById(int id) {
        return films.get(id);
    }

    @Override
    public List<Film> getAll() {
        log.info("Произошел запрос всех фильмов");
        return new ArrayList<>(films.values());
    }

    @Override
    public void delete(int id) {
        films.remove(id);
    }
}