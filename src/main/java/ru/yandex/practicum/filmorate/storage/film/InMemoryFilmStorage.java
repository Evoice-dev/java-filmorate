package ru.yandex.practicum.filmorate.storage.film;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Component("inMemoryFilmStorage")
public class InMemoryFilmStorage implements FilmStorage {
    private final Map<Integer, Film> films = new LinkedHashMap<>();
    private int nextId = 1;

    @Override
    public Film add(Film film) {
        film.setId(nextId++);
        films.put(film.getId(), film);
        return film;
    }

    @Override
    public Film update(Film film) {
        Film existing = films.get(film.getId());
        if (existing == null) {
            throw new NotFoundException("Такого фильма не существует");
        }
        film.setLikes(existing.getLikes());
        films.put(film.getId(), film);
        return film;
    }

    @Override
    public Optional<Film> getById(int id) {
        return Optional.ofNullable(films.get(id));
    }

    @Override
    public List<Film> getAll() {
        return new ArrayList<>(films.values());
    }

    @Override
    public void delete(int id) {
        films.remove(id);
    }

    @Override
    public void addLike(int filmId, int userId) {
        films.get(filmId).getLikes().add(userId);
    }

    @Override
    public void removeLike(int filmId, int userId) {
        films.get(filmId).getLikes().remove(userId);
    }

    @Override
    public List<Film> getPopular(int count) {
        return films.values().stream()
                .sorted(Comparator.comparingInt((Film f) -> f.getLikes().size()).reversed())
                .limit(count)
                .collect(Collectors.toList());
    }
}
