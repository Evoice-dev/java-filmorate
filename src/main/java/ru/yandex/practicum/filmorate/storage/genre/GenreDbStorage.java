package ru.yandex.practicum.filmorate.storage.genre;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Genre;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class GenreDbStorage implements GenreStorage {
    private static final RowMapper<Genre> MAPPER =
            (rs, rowNum) -> new Genre(rs.getInt("id"), rs.getString("name"));

    private final JdbcTemplate jdbc;

    @Override
    public List<Genre> getAll() {
        return jdbc.query("SELECT id, name FROM genres ORDER BY id", MAPPER);
    }

    @Override
    public Optional<Genre> getById(int id) {
        return jdbc.query("SELECT id, name FROM genres WHERE id = ?", MAPPER, id)
                .stream()
                .findFirst();
    }

    @Override
    public List<Genre> getByIds(Collection<Integer> ids) {
        if (ids.isEmpty()) {
            return List.of();
        }
        String placeholders = String.join(",", Collections.nCopies(ids.size(), "?"));
        return jdbc.query("SELECT id, name FROM genres WHERE id IN (" + placeholders + ") ORDER BY id",
                MAPPER, ids.toArray());
    }
}
