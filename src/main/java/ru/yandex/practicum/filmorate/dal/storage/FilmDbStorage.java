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
import ru.yandex.practicum.filmorate.service.GenreService;

import java.sql.Date;
import java.util.Collection;
import java.util.Optional;

@Component("filmDbStorage")
@Slf4j
public class FilmDbStorage extends BaseDbStorage<Film> implements FilmStorage {
    private static final String FIND_ALL_FILMS_QUERY = "SELECT * FROM film";
    //    private static final String FIND_FILM_BY_ID_WITH_GENRE_QUERY =
//            "SELECT f.ID, f.NAME, f.DESCRIPTION, f.RELEASE_DATE, f.DURATION, f.MPA_RATING_ID, g.GENRE_ID, g.NAME " +
//            "FROM film AS f " +
//            "JOIN film_genre AS fg ON fg.FILM_ID = f.ID " +
//            "JOIN GENRE g ON fg.GENRE_ID = g.GENRE_ID WHERE f.id = ?";
    private static final String FIND_FILM_BY_ID = "SELECT * FROM film WHERE id = ?";
    private static final String INSERT_FILM_QUERY = "INSERT INTO film (name, mpa_rating_id, release_date, "
            + "description, duration) VALUES (?, ?, ?, ?, ?)";
    private static final String INSERT_FILM_GENRE_QUERY = "INSERT INTO film_genre (film_id, genre_id) VALUES (?, ?)";
    private static final String UPDATE_FILM__QUERY = "UPDATE film SET name = ?, mpa_rating_id = ?, " +
            "release_date = ?, description = ?, duration = ? WHERE id = ?";
//    private static final String UPDATE_FILM_MPA_RATING_QUERY = "UPDATE film SET mpa_rating_id = ? WHERE id = ?";
//    private static final String UPDATE_FILM_RELEASE_DATA_QUERY = "UPDATE film SET release_date = ? WHERE id = ?";
//    private static final String UPDATE_FILM_DESCRIPTION_QUERY = "UPDATE film SET description = ? WHERE id = ?";
//    private static final String UPDATE_FILM_DURATION_QUERY = "UPDATE film SET duration = ? WHERE id = ?";
    private static final String INSERT_FILM_LIKE_QUERY = "INSERT INTO film_like (film_id, user_id) VALUES (?, ?)";
    private static final String DELETE_FILM_LIKE_QUERY = "DELETE FROM film_like WHERE film_id = ? AND user_id = ?";
    private static final String FIND_MOST_POPULAR_FILMS_QUERY =
            "SELECT f.* " +
                    "FROM film AS f " +
                    "LEFT JOIN film_like AS fl ON f.id = fl.film_id " +
                    "GROUP BY f.id, f.name " +
                    "ORDER BY COUNT(fl.user_id) DESC " +
                    "LIMIT ?";

    GenreDbStorage genreDbStorage; // исправить на сервис

    public FilmDbStorage(JdbcTemplate jdbc, RowMapper<Film> mapper, GenreDbStorage genreDbStorage) {
        super(jdbc, mapper);
        this.genreDbStorage = genreDbStorage;
    }

    @Override
    public Film createFilm(Film film) {
        long id = insert(
                INSERT_FILM_QUERY,
                film.getName(),
                film.getMpa().getId(),
                Date.valueOf(film.getReleaseDate()),
                film.getDescription(),
                film.getDuration());
        film.setId(id); //как проверить полные дубликаты?
        log.debug("Начинается добавление жанров к фильму");
        for (Genre genre : film.getGenres()) {
            addGenresToFilm(film.getId(), genre.getId());
        }
//        Нужно заполнить поля для genre name и mpa name
        return film;
    }

    @Override
    public Film updateFilm(Film film) { // нужно добавить здесь добавление жанра, если genre.genreId различаются

        //стоит ограничения на null в таблице
        update(UPDATE_FILM__QUERY, film.getName(), film.getMpa().getId(), film.getReleaseDate(), film.getDescription(),
                    film.getDuration(), film.getId());
        //нужно обновить названия жанров и названия mpa здесь или в сервисе?
        log.info("Данные успешно обновлены");
        return findFilmById(film.getId()).orElseThrow(() -> new NotFoundException("Фильм с ID " + film.getId() + " не найден после обновления")
        );
    }

    @Override
    public Collection<Film> findAllFilms() {
        return findMany(FIND_ALL_FILMS_QUERY);
    } //реализовать добавление жанров к Film

    @Override
    public Optional<Film> findFilmById(Long filmId) {
        Optional<Film> filmOptional = findOne(FIND_FILM_BY_ID, filmId);
        return filmOptional;
    } //реализовать добавление жанров к Film

    @Override
    public Film addLike(Film film, Long userId) {
        try {
            insertWithoutGeneratedKey(INSERT_FILM_LIKE_QUERY, film.getId(), userId);
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

    public void addGenresToFilm(Long filmId, Long genreId) {
        Optional<Genre> genreOptional = genreDbStorage.findById(genreId);
        if (genreOptional.isEmpty()) {
            log.error("Жанр не найден. ID: {}", filmId);
        }
        Genre genre = genreOptional.orElseThrow(() -> new NotFoundException("Жанр с id = " + genreId
                + " не найден"));
        insertWithoutGeneratedKey(INSERT_FILM_GENRE_QUERY, filmId, genreId);
    }

}
