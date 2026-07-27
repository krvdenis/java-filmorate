package ru.yandex.practicum.filmorate;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import ru.yandex.practicum.filmorate.dal.FilmDbStorage;
import ru.yandex.practicum.filmorate.dal.UserDbStorage;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class FilmDbStorageTests {
    private final FilmDbStorage filmDbStorage;
    private final UserDbStorage userDbStorage;

    @BeforeEach
    public void clear() {
        filmDbStorage.clear();
    }

    @Test
    public void testFindFilmById_ShouldReturnUserId() {
        Film film = new Film();
        film.setName("testFilm");
        film.setReleaseDate(LocalDate.of(1990, 1, 1));
        film.setDescription("test desc");
        film.setDuration(6);

        Film createdFilm = filmDbStorage.createFilm(film);
        Optional<Film> filmOptional = filmDbStorage.findFilmById(createdFilm.getId());

        assertThat(filmOptional)
                .isPresent()
                .hasValueSatisfying(user ->
                        assertThat(user).hasFieldOrPropertyWithValue("id", createdFilm.getId())
                );
    }

    @Test
    public void testFindAllFilms_ShouldReturnExpectedTotalFilms() {
        Film film1 = new Film();
        film1.setName("testFilm1");
        film1.setReleaseDate(LocalDate.of(1990, 1, 1));
        film1.setDescription("test desc");
        film1.setDuration(6);

        Film film2 = new Film();
        film2.setName("testFilm2");
        film2.setReleaseDate(LocalDate.of(1990, 1, 1));
        film2.setDescription("test desc");
        film2.setDuration(6);

        List<Film> testFilms = List.of(film1, film2);
        testFilms.forEach(filmDbStorage::createFilm);

        Collection<Film> films = filmDbStorage.findAllFilms();
        assertThat(films)
                .isNotNull()
                .hasSize(2);

        List<Film> filmListList = new ArrayList<>(films);
        assertThat(filmListList)
                .extracting("name")
                .containsExactlyInAnyOrder("testFilm1", "testFilm2");
    }

    @Test
    public void testUpdateFilm_ShouldReturnExpectedName() {
        Film film = new Film();
        film.setName("testFilm1");
        film.setReleaseDate(LocalDate.of(1990, 1, 1));
        film.setDescription("test desc");
        film.setDuration(6);
        Long filmid = filmDbStorage.createFilm(film).getId();

        Film newfilm = new Film();
        newfilm.setId(filmid);
        newfilm.setName("testFilm28");
        newfilm.setReleaseDate(LocalDate.of(1990, 1, 1));
        newfilm.setDescription("test desc");
        newfilm.setDuration(6);

        Film updatedFilm = filmDbStorage.updateFilm(newfilm);
        assertThat(updatedFilm)
                .hasFieldOrPropertyWithValue("name", "testFilm28");
    }

    @Test
    public void testAddAndDeleteLike_ShouldReturnExpectedTotalLikes() {
        User user = new User();
        user.setLogin("user111");
        user.setName("User One");
        user.setEmail("user3@example.com");
        user.setBirthday(LocalDate.of(1990, 1, 1));
        Long userId = userDbStorage.createUser(user).getId();

        Film film = new Film();
        film.setName("testFilm1");
        film.setReleaseDate(LocalDate.of(1990, 1, 1));
        film.setDescription("test desc");
        film.setDuration(6);
        Long filmId = filmDbStorage.createFilm(film).getId();
        film = filmDbStorage.addLike(film, userId);

        List<Long> likes = new ArrayList<>(film.getLikes());

        assertThat(likes).hasSize(1);
        filmDbStorage.deleteLike(film, userId);

        film = filmDbStorage.findFilmById(filmId).orElseThrow(() -> new NotFoundException("Фильм не найден"));
        likes = new ArrayList<>(film.getLikes());
        assertThat(likes).hasSize(0);
    }
}
