package ru.yandex.practicum.filmorate.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.FilmService;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/films")
@RequiredArgsConstructor
public class FilmController {
    private final FilmService filmService;

    @PostMapping
    public Film addFilm(@RequestBody Film film) {
        log.info("POST /films — запрос на добавление фильма: {}", film);
        Film created = filmService.addFilm(film);
        log.info("POST /films — фильм добавлен с id={}: {}", created.getId(), created);
        return created;
    }

    @PutMapping
    public Film updateFilm(@RequestBody Film film) {
        log.info("PUT /films — запрос на обновление фильма: {}", film);
        Film updated = filmService.updateFilm(film);
        log.info("PUT /films — фильм обновлён: {}", updated);
        return updated;
    }

    @GetMapping
    public List<Film> getAllFilms() {
        log.info("GET /films — запрос списка всех фильмов");
        List<Film> films = filmService.getAllFilms();
        log.info("GET /films — возвращено фильмов: {}", films.size());
        return films;
    }

    @GetMapping("/{id}")
    public Film getFilm(@PathVariable int id) {
        log.info("GET /films/{} — запрос фильма по id", id);
        Film film = filmService.getFilmById(id);
        log.info("GET /films/{} — фильм найден: {}", id, film);
        return film;
    }

    @PutMapping("/{id}/like/{userId}")
    public void addLike(@PathVariable int id, @PathVariable int userId) {
        log.info("PUT /films/{}/like/{} — пользователь ставит лайк", id, userId);
        filmService.addLike(id, userId);
        log.info("PUT /films/{}/like/{} — лайк поставлен", id, userId);
    }

    @DeleteMapping("/{id}/like/{userId}")
    public void removeLike(@PathVariable int id, @PathVariable int userId) {
        log.info("DELETE /films/{}/like/{} — пользователь убирает лайк", id, userId);
        filmService.removeLike(id, userId);
        log.info("DELETE /films/{}/like/{} — лайк убран", id, userId);
    }

    @GetMapping("/popular")
    public List<Film> getPopular(@RequestParam(defaultValue = "10") int count) {
        log.info("GET /films/popular?count={} — запрос популярных фильмов", count);
        List<Film> popular = filmService.getPopularFilms(count);
        log.info("GET /films/popular?count={} — возвращено фильмов: {}", count, popular.size());
        return popular;
    }
}