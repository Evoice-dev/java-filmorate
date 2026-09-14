package ru.yandex.practicum.filmorate.controller;

import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exceptions.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.ArrayList;

import static ru.yandex.practicum.filmorate.FilmorateApplication.log;

@RestController
@RequestMapping("/films")
public class FilmController {
    private final ArrayList<Film> allFilms = new ArrayList<>();

    @PostMapping()
    public Film add(@RequestBody Film film) {
        log.info("Попытка добавить фильм");
        film.id = allFilms.size() + 1;
        allFilms.add(film);
        log.info("Фильм успешно добавлен");
        return film;
    }

    @PutMapping()
    public Film update(@RequestBody Film film) throws ValidationException {
        log.info("Попытка редактирования фильма");
        if (allFilms.contains(film)) {
            allFilms.set(allFilms.indexOf(film), film);
        } else {
            throw new ValidationException("Такого фильма не существует");
        }
        log.info("Фильм успешно отредактирован");
        return film;
    }

    @GetMapping()
    public ArrayList<Film> getAll() {
        log.info("Произошел запрос всех фильмов");
        return allFilms;
    }
}
