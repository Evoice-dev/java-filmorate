package ru.yandex.practicum.filmorate.storage.film;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Repository("filmDbStorage")
@RequiredArgsConstructor
public class FilmDbStorage implements FilmStorage {
    private static final String SELECT_FILMS = "SELECT f.id, f.name, f.description, f.release_date, f.duration, "
            + "f.mpa_id, m.name AS mpa_name FROM films f LEFT JOIN mpa m ON m.id = f.mpa_id ";

    private static final RowMapper<Film> MAPPER = (rs, rowNum) -> {
        Film film = new Film();
        film.setId(rs.getInt("id"));
        film.setName(rs.getString("name"));
        film.setDescription(rs.getString("description"));
        film.setReleaseDate(rs.getDate("release_date").toLocalDate());
        film.setDuration(rs.getInt("duration"));
        int mpaId = rs.getInt("mpa_id");
        if (!rs.wasNull()) {
            film.setMpa(new Mpa(mpaId, rs.getString("mpa_name")));
        }
        return film;
    };

    private final JdbcTemplate jdbc;

    @Override
    public Film add(Film film) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbc.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(
                    "INSERT INTO films (name, description, release_date, duration, mpa_id) VALUES (?, ?, ?, ?, ?)",
                    new String[]{"id"});
            ps.setString(1, film.getName());
            ps.setString(2, film.getDescription());
            ps.setDate(3, Date.valueOf(film.getReleaseDate()));
            ps.setInt(4, film.getDuration());
            if (film.getMpa() != null) {
                ps.setInt(5, film.getMpa().getId());
            } else {
                ps.setNull(5, Types.INTEGER);
            }
            return ps;
        }, keyHolder);
        film.setId(Objects.requireNonNull(keyHolder.getKey()).intValue());
        saveGenres(film);
        return getById(film.getId()).orElseThrow();
    }

    @Override
    public Film update(Film film) {
        int updated = jdbc.update(
                "UPDATE films SET name = ?, description = ?, release_date = ?, duration = ?, mpa_id = ? WHERE id = ?",
                film.getName(), film.getDescription(), Date.valueOf(film.getReleaseDate()), film.getDuration(),
                film.getMpa() == null ? null : film.getMpa().getId(), film.getId());
        if (updated == 0) {
            throw new NotFoundException("Фильм с id=" + film.getId() + " не найден");
        }
        jdbc.update("DELETE FROM film_genres WHERE film_id = ?", film.getId());
        saveGenres(film);
        return getById(film.getId()).orElseThrow();
    }

    @Override
    public Optional<Film> getById(int id) {
        return fillDetails(jdbc.query(SELECT_FILMS + "WHERE f.id = ?", MAPPER, id)).stream().findFirst();
    }

    @Override
    public List<Film> getAll() {
        return fillDetails(jdbc.query(SELECT_FILMS + "ORDER BY f.id", MAPPER));
    }

    @Override
    public void delete(int id) {
        jdbc.update("DELETE FROM films WHERE id = ?", id);
    }

    @Override
    public void addLike(int filmId, int userId) {
        jdbc.update("MERGE INTO likes (film_id, user_id) KEY (film_id, user_id) VALUES (?, ?)", filmId, userId);
    }

    @Override
    public void removeLike(int filmId, int userId) {
        jdbc.update("DELETE FROM likes WHERE film_id = ? AND user_id = ?", filmId, userId);
    }

    @Override
    public List<Film> getPopular(int count) {
        return fillDetails(jdbc.query(SELECT_FILMS
                + "LEFT JOIN (SELECT film_id, COUNT(*) AS cnt FROM likes GROUP BY film_id) l ON l.film_id = f.id "
                + "ORDER BY COALESCE(l.cnt, 0) DESC, f.id LIMIT ?", MAPPER, count));
    }

    private void saveGenres(Film film) {
        if (film.getGenres() == null || film.getGenres().isEmpty()) {
            return;
        }
        Set<Integer> genreIds = film.getGenres().stream()
                .map(Genre::getId)
                .collect(Collectors.toCollection(LinkedHashSet::new));
        List<Object[]> batch = new ArrayList<>();
        for (Integer genreId : genreIds) {
            batch.add(new Object[]{film.getId(), genreId});
        }
        jdbc.batchUpdate("INSERT INTO film_genres (film_id, genre_id) VALUES (?, ?)", batch);
    }

    private List<Film> fillDetails(List<Film> films) {
        if (films.isEmpty()) {
            return films;
        }
        Map<Integer, Film> byId = films.stream()
                .collect(Collectors.toMap(Film::getId, Function.identity(), (a, b) -> a, LinkedHashMap::new));
        String placeholders = String.join(",", Collections.nCopies(byId.size(), "?"));
        Object[] ids = byId.keySet().toArray();

        jdbc.query("SELECT fg.film_id, g.id, g.name FROM film_genres fg JOIN genres g ON g.id = fg.genre_id "
                        + "WHERE fg.film_id IN (" + placeholders + ") ORDER BY g.id",
                rs -> {
                    byId.get(rs.getInt("film_id")).getGenres().add(new Genre(rs.getInt("id"), rs.getString("name")));
                },
                ids);

        jdbc.query("SELECT film_id, user_id FROM likes WHERE film_id IN (" + placeholders + ")",
                rs -> {
                    byId.get(rs.getInt("film_id")).getLikes().add(rs.getInt("user_id"));
                },
                ids);
        return films;
    }
}
