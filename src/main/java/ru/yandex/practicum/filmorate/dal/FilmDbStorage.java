package ru.yandex.practicum.filmorate.dal;

import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.DuplicateDataException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Component("filmDbStorage")
@Slf4j
public class FilmDbStorage extends BaseDbStorage<Film> implements FilmStorage {
    private static final String FIND_ALL_FILMS_QUERY = """
                SELECT
                    f.id,
                    f.name,
                    f.release_date,
                    f.description,
                    f.duration,
                    mr.mpa_rating_id,
                    mr.name AS mpa_name,
                    STRING_AGG(g.name, ',') WITHIN GROUP (ORDER BY g.genre_id) AS genres_name,
                    STRING_AGG(g.genre_id::TEXT, ',') WITHIN GROUP (ORDER BY g.genre_id) AS genres_id,
                    STRING_AGG(fl.user_id::TEXT, ',') WITHIN GROUP (ORDER BY fl.user_id) AS users_id
                FROM film AS f
                LEFT JOIN film_like AS fl ON f.id = fl.film_id
                LEFT JOIN mpa_rating AS mr ON f.mpa_rating_id = mr.mpa_rating_id
                LEFT JOIN film_genre AS fg ON f.id = fg.film_id
                LEFT JOIN genre AS g ON fg.genre_id = g.genre_id
                GROUP BY
                    f.id, f.name, f.release_date, f.description, f.duration,
                    mr.mpa_rating_id, mr.name
            """;
    private static final String FIND_FILM_BY_ID = """
                SELECT
                    f.id,
                    f.name,
                    f.release_date,
                    f.description,
                    f.duration,
                    mr.mpa_rating_id,
                    mr.name AS mpa_name,
                    STRING_AGG(g.name, ',') WITHIN GROUP (ORDER BY g.genre_id) AS genres_name,
                    STRING_AGG(g.genre_id::TEXT, ',') WITHIN GROUP (ORDER BY g.genre_id) AS genres_id,
                    STRING_AGG(fl.user_id::TEXT, ',') WITHIN GROUP (ORDER BY fl.user_id) AS users_id
                FROM film AS f
                LEFT JOIN film_like AS fl ON f.id = fl.film_id
                LEFT JOIN mpa_rating AS mr ON f.mpa_rating_id = mr.mpa_rating_id
                LEFT JOIN film_genre AS fg ON f.id = fg.film_id
                LEFT JOIN genre AS g ON fg.genre_id = g.genre_id
                WHERE f.id = ?
                GROUP BY
                    f.id, f.name, f.release_date, f.description, f.duration,
                    mr.mpa_rating_id, mr.name
            """;
    private static final String INSERT_FILM_QUERY = """
                INSERT INTO film (name, mpa_rating_id, release_date, description, duration)
                VALUES (?, ?, ?, ?, ?)
            """;
    private static final String INSERT_FILM_GENRE_QUERY = """
                INSERT INTO film_genre (film_id, genre_id)
                VALUES (?, ?)
            """;
    private static final String UPDATE_FILM_GENRE_QUERY = """
                MERGE INTO film_genre (film_id, genre_id)
                KEY (film_id, genre_id)
                VALUES (?, ?)
            """;
    private static final String UPDATE_FILM__QUERY = """
                UPDATE film SET name = ?, mpa_rating_id = ?, release_date = ?, description = ?, duration = ?
                WHERE id = ?
            """;
    private static final String INSERT_FILM_LIKE_QUERY = """
                INSERT INTO film_like (film_id, user_id)
                VALUES (?, ?)
            """;
    private static final String DELETE_FILM_LIKE_QUERY = """
                DELETE FROM film_like
                WHERE film_id = ? AND user_id = ?
            """;
    private static final String FIND_MOST_POPULAR_FILMS_QUERY = """
                SELECT
                    f.id,
                    f.name,
                    f.release_date,
                    f.description,
                    f.duration,
                    mr.mpa_rating_id,
                    mr.name AS mpa_name,
                    STRING_AGG(g.name, ',') WITHIN GROUP (ORDER BY g.genre_id) AS genres_name,
                    STRING_AGG(g.genre_id::TEXT, ',') WITHIN GROUP (ORDER BY g.genre_id) AS genres_id,
                    STRING_AGG(fl.user_id::TEXT, ',') WITHIN GROUP (ORDER BY fl.user_id) AS users_id
                FROM film AS f
                LEFT JOIN film_like AS fl ON f.id = fl.film_id
                LEFT JOIN mpa_rating AS mr ON f.mpa_rating_id = mr.mpa_rating_id
                LEFT JOIN film_genre AS fg ON f.id = fg.film_id
                LEFT JOIN genre AS g ON fg.genre_id = g.genre_id
                GROUP BY
                    f.id, f.name, f.release_date, f.description, f.duration,
                    mr.mpa_rating_id, mr.name
                ORDER BY COUNT(fl.user_id) DESC
                LIMIT ?
            """;
    private static final String DELETE_FILM_QUERY = "DELETE FROM film";
    private final GenreDbStorage genreDbStorage;
    private final MpaRatingDbStorage mpaRatingDbStorage;

    public FilmDbStorage(JdbcTemplate jdbc, RowMapper<Film> mapper, GenreDbStorage genreDbStorage,
                         MpaRatingDbStorage mpaRatingDbStorage) {
        super(jdbc, mapper);
        this.genreDbStorage = genreDbStorage;
        this.mpaRatingDbStorage = mpaRatingDbStorage;
    }

    @Override
    public Film createFilm(Film film) {
        Long mpaId = (film.getMpa() != null) ? film.getMpa().getId() : null;
        long id = insert(
                INSERT_FILM_QUERY,
                film.getName(),
                mpaId,
                film.getReleaseDate(),
                film.getDescription(),
                film.getDuration());
        film.setId(id);
        insertGenresToFilm(film.getId(), film.getGenres(), INSERT_FILM_GENRE_QUERY);
        return film;
    }

    @Override
    public Film updateFilm(Film film) {
        Long mpaId = film.getMpa() != null ? film.getMpa().getId() : null;

        update(UPDATE_FILM__QUERY, film.getName(), mpaId, film.getReleaseDate(), film.getDescription(),
                film.getDuration(), film.getId());
        insertGenresToFilm(film.getId(), film.getGenres(), UPDATE_FILM_GENRE_QUERY);
        mpaRatingDbStorage.findMpaByFilmId(film.getId()).ifPresent(film::setMpa);
        Collection<Genre> genres = genreDbStorage.findGenresByFilmId(film.getId());
        film.setGenres(new HashSet<>(genres));
        return film;
    }

    @Override
    public Collection<Film> findAllFilms() {
        return findMany(FIND_ALL_FILMS_QUERY);
    }

    @Override
    public Optional<Film> findFilmById(Long filmId) {
        return findOne(FIND_FILM_BY_ID, filmId);
    }

    @Override
    public Film addLike(Film film, Long userId) {
        try {
            insertWithoutGeneratedKey(INSERT_FILM_LIKE_QUERY, film.getId(), userId);
            film.getLikes().add(userId);
        } catch (DuplicateKeyException e) {
            log.warn("Пользователь с ID {} уже поставил лайк фильму с ID {}", userId, film.getId());
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
    public Collection<Film> getMostPopularFilmsByLikes(int count) {
        return findMany(FIND_MOST_POPULAR_FILMS_QUERY, count);
    }

    public void clear() {
        clear(DELETE_FILM_QUERY);
    }

    private void insertGenresToFilm(Long filmId, Set<Genre> genres, String query) {
        if (genres == null || genres.isEmpty()) {
            log.debug("Жанры для фильма {} не указаны", filmId);
            return;
        }

        Set<Long> genreIds = genres.stream()
                .map(Genre::getId)
                .collect(Collectors.toSet());

        Collection<Genre> existingGenres = genreDbStorage.findGenresByIds(genreIds);
        Set<Long> validGenreIds = existingGenres.stream()
                .map(Genre::getId)
                .collect(Collectors.toSet());

        Set<Genre> validGenres = genres.stream()
                .filter(genre -> validGenreIds.contains(genre.getId()))
                .collect(Collectors.toSet());

        if (validGenres.isEmpty()) {
            log.warn("Ни один из указанных жанров не найден в БД для фильма {}", filmId);
            return;
        }

        List<Object[]> batchArgs = validGenres.stream()
                .map(genre -> new Object[]{filmId, genre.getId()})
                .collect(Collectors.toList());

        jdbc.batchUpdate(query, batchArgs);

        log.info("Для фильма {} добавлено {} связей с жанрами", filmId, batchArgs.size());
    }
}