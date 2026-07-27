package ru.yandex.practicum.filmorate;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import ru.yandex.practicum.filmorate.dal.GenreDbStorage;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Genre;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class GenreDbStorageTests {
    private final GenreDbStorage genreDbStorage;

    @Test
    public void testFindGenreById_ShouldReturnExpectedName() {
        Genre genre = genreDbStorage.findById(1L).orElseThrow(() ->
                new NotFoundException("Жанр не найден"));
        assertThat(genre).hasFieldOrPropertyWithValue("name", "Комедия");
    }

    @Test
    public void testFindGenres_ShouldReturnExpectedTotalGenre() {
        List<Genre> genres = new ArrayList<>(genreDbStorage.findAll());
        assertThat(genres).hasSize(6);
        assertThat(genres)
                .extracting("name")
                .containsExactlyInAnyOrder("Комедия", "Драма", "Боевик", "Документальный", "Мультфильм",
                        "Триллер");
    }

}
