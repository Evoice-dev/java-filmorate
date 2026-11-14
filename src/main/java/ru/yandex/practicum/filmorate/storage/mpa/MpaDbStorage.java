package ru.yandex.practicum.filmorate.storage.mpa;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class MpaDbStorage implements MpaStorage {
    private static final RowMapper<Mpa> MAPPER =
            (rs, rowNum) -> new Mpa(rs.getInt("id"), rs.getString("name"));

    private final JdbcTemplate jdbc;

    @Override
    public List<Mpa> getAll() {
        return jdbc.query("SELECT id, name FROM mpa ORDER BY id", MAPPER);
    }

    @Override
    public Optional<Mpa> getById(int id) {
        return jdbc.query("SELECT id, name FROM mpa WHERE id = ?", MAPPER, id)
                .stream()
                .findFirst();
    }
}
