package ru.yandex.practicum.filmorate.dal.storage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.DuplicateDataException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;

import java.sql.Date;
import java.util.Collection;
import java.util.Optional;

@Component("filmDbStorage")
@Slf4j
public class FilmDbStorage extends BaseDbStorage<Film> implements FilmStorage {
    private static final String FIND_ALL_FILMS_QUERY = "SELECT * FROM film";
    private static final String FIND_FILM_BY_ID_QUERY = "SELECT * FROM film WHERE id = ?";
    private static final String INSERT_FILM_QUERY = "INSERT INTO film (name, mpa_rating_id, release_date, "
            + "description, duration) VALUES (?, ?, ?, ?, ?)";
    private static final String UPDATE_FILM_TITLE_QUERY = "UPDATE film SET name = ? WHERE id = ?";
    private static final String UPDATE_FILM_MPA_RATING_QUERY = "UPDATE film SET mpa_rating_id = ? WHERE id = ?";
    private static final String UPDATE_FILM_RELEASE_DATA_QUERY = "UPDATE film SET release_date = ? WHERE id = ?";
    private static final String UPDATE_FILM_DESCRIPTION_QUERY = "UPDATE film SET description = ? WHERE id = ?";
    private static final String UPDATE_FILM_DURATION_QUERY = "UPDATE film SET duration = ? WHERE id = ?";
    private static final String INSERT_FILM_LIKE_QUERY = "INSERT INTO film_like (film_id, user_id) VALUES (?, ?)";
    private static final String DELETE_FILM_LIKE_QUERY = "DELETE FROM film_like WHERE film_id = ? AND user_id = ?";
    private static final String INSERT_FILM_GENRE_QUERY = "INSERT INTO film_genre (film_id, genre_id) VALUES (?, ?)";
    private static final String FIND_MOST_POPULAR_FILMS_QUERY =
            "SELECT f.title " +
                    "FROM film AS f " +
                    "LEFT JOIN film_like AS fl ON f.id = fl.film_id " +
                    "GROUP BY f.id, f.title " +
                    "ORDER BY COUNT(fl.user_id) DESC " +
                    "LIMIT ?";
    GenreDbStorage genreDbStorage;
    public FilmDbStorage(JdbcTemplate jdbc, RowMapper<Film> mapper, GenreDbStorage genreDbStorage) {
        super(jdbc, mapper);
        this.genreDbStorage = genreDbStorage;
    }

    @Override
    public Film createFilm(Film film) {
        long id = insert(
                INSERT_FILM_QUERY,
                film.getName(),
                film.getMpaRatingId(),
                Date.valueOf(film.getReleaseDate()),
                film.getDescription(),
                film.getDuration());
        film.setId(id); //как проверить полные дубликаты?
//        for (Genre genre : film.getGenres()) {
//            insert(INSERT_FILM_GENRE_QUERY, film.getId(), genre.getId());
//        }
        return film;
    }

    @Override
    public Film updateFilm(Film newFilm) {
        Optional<Film> filmOptional = findFilmById(newFilm.getId());
        if (filmOptional.isEmpty()) {
            log.error("Фильм не найден. ID: {}", newFilm.getId());
        }
        Film oldFilm = filmOptional.orElseThrow(() -> new NotFoundException("Фильм с id = " + newFilm.getId()
                + " не найден"));

        if (newFilm.getName() != null && !oldFilm.getName().equals(newFilm.getName())) {
            update(UPDATE_FILM_TITLE_QUERY, newFilm.getName(), newFilm.getId());
            log.info("Пользователь изменил имя фильма с ID {} на {}", newFilm.getId(), newFilm.getName());

        }
        if (oldFilm.getMpaRatingId() != newFilm.getMpaRatingId()) {
            update(UPDATE_FILM_MPA_RATING_QUERY, newFilm.getMpaRatingId(), newFilm.getId());
            log.info("Пользователь изменил MPA рейтинг фильма с ID {} на {}", newFilm.getId(),
                    newFilm.getMpaRatingId());
        }
        if (newFilm.getDescription() != null && !oldFilm.getDescription().equals(newFilm.getDescription())) {
            update(UPDATE_FILM_DESCRIPTION_QUERY, newFilm.getDescription(), newFilm.getId());
            log.info("Пользователь изменил описание фильма с ID {} на {}", newFilm.getId(),
                    newFilm.getDescription());
        }
        if (newFilm.getReleaseDate() != null && !oldFilm.getReleaseDate().equals(newFilm.getReleaseDate())) {
            update(UPDATE_FILM_RELEASE_DATA_QUERY, newFilm.getReleaseDate(), newFilm.getId());
            log.info("Пользователь изменил дату выхода фильма с ID {} на {}", newFilm.getId(),
                    newFilm.getReleaseDate());
        }
        if (oldFilm.getDuration() != newFilm.getDuration()) {
            update(UPDATE_FILM_DURATION_QUERY, newFilm.getDuration(), newFilm.getId());
            log.info("Пользователь изменил продолжительность фильма с ID {} на {}", newFilm.getId(),
                    newFilm.getDuration());
        }
        return findFilmById(newFilm.getId()).get();
    }

    @Override
    public Collection<Film> findAllFilms() {
        return findMany(FIND_ALL_FILMS_QUERY);
    }

    @Override
    public Optional<Film> findFilmById(Long filmId) {
        return findOne(FIND_FILM_BY_ID_QUERY, filmId);
    }

    @Override
    public Film addLike(Film film, Long userId) {
        try {
            insert(INSERT_FILM_LIKE_QUERY, userId, film.getId());
            film.getLikes().add(userId);
        } catch (DuplicateKeyException e) {
            log.warn("Пользователь с ID {} уже поставил лайк фильму с ID {}", userId,
                    film.getId());
            throw new DuplicateDataException("От одного пользователя может быть только один like");
        }
        return film;
    }

    @Override
    public void deleteLike(Film film, Long userId) {
        boolean removed = delete(DELETE_FILM_LIKE_QUERY, film.getId(), userId);
        if (removed) {
            log.info("Лайк удалён: пользователь {} убрал лайк у фильма {}", userId, film.getId());
        } else {
            log.warn("Попытка удалить отсутствующий лайк: пользователь {} не ставил лайк фильму {}", userId,
                    film.getId());
        }
    }

    @Override
    public Collection<Film> findMostPopularFilmsByLikes(int count) {
        return findMany(FIND_MOST_POPULAR_FILMS_QUERY, count);
    }

    public Film addGenreToFilm(Long filmId, Long genreId) {
        Optional<Film> filmOptional = findFilmById(filmId);
        if (filmOptional.isEmpty()) {
            log.error("Фильм не найден. ID: {}", filmId);
        }
        Film film = filmOptional.orElseThrow(() -> new NotFoundException("Фильм с id = " + filmId
                + " не найден"));
        Optional<Genre> genreOptional = genreDbStorage.findById(genreId);
        if (genreOptional.isEmpty()) {
            log.error("Жанр не найден. ID: {}", filmId);
        }
        Genre genre = genreOptional.orElseThrow(() -> new NotFoundException("Жанр с id = " + genreId
                + " не найден"));
        insert(INSERT_FILM_GENRE_QUERY, filmId, genreId);
        return findFilmById(filmId).get();
    }

}
