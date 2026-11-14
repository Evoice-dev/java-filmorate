package ru.yandex.practicum.filmorate.storage;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.storage.genre.GenreDbStorage;
import ru.yandex.practicum.filmorate.storage.mpa.MpaDbStorage;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@AutoConfigureTestDatabase
@Import({GenreDbStorage.class, MpaDbStorage.class})
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class GenreMpaDbStorageTest {
    private final GenreDbStorage genreStorage;
    private final MpaDbStorage mpaStorage;

    @Test
    void testGetAllMpa() {
        assertThat(mpaStorage.getAll())
                .extracting(Mpa::getName)
                .containsExactly("G", "PG", "PG-13", "R", "NC-17");
    }

    @Test
    void testFindMpaById() {
        assertThat(mpaStorage.getById(1))
                .isPresent()
                .hasValueSatisfying(mpa -> assertThat(mpa).hasFieldOrPropertyWithValue("name", "G"));
        assertThat(mpaStorage.getById(9999)).isEmpty();
    }

    @Test
    void testGetAllGenres() {
        assertThat(genreStorage.getAll())
                .extracting(Genre::getName)
                .containsExactly("Комедия", "Драма", "Мультфильм", "Триллер", "Документальный", "Боевик");
    }

    @Test
    void testFindGenreById() {
        assertThat(genreStorage.getById(1))
                .isPresent()
                .hasValueSatisfying(genre -> assertThat(genre).hasFieldOrPropertyWithValue("name", "Комедия"));
        assertThat(genreStorage.getById(9999)).isEmpty();
    }

    @Test
    void testGetGenresByIds() {
        List<Genre> genres = genreStorage.getByIds(List.of(5, 2, 999));

        assertThat(genres).extracting(Genre::getId).containsExactly(2, 5);
        assertThat(genreStorage.getByIds(List.of())).isEmpty();
    }
}
