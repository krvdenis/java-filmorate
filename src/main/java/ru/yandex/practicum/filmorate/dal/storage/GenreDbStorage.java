package ru.yandex.practicum.filmorate.dal.storage;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Genre;

import java.util.Collection;
import java.util.Optional;

@Component
public class GenreDbStorage extends BaseDbStorage<Genre> {
    private static final String INSERT_GENRE_QUERY = "INSERT INTO genre (name) VALUES (?)";
    private static final String FIND_ALL_GENRE_QUERY = "SELECT * FROM genre";
    private static final String FIND_GENRE_BY_ID_QUERY = "SELECT * FROM genre WHERE genre_id = ?";
    private static final String COUNT_GENRES_QUERY = "SELECT COUNT(genre_id) FROM genre";
    private static final String FIND_GENRES_BY_FILM_ID_QUERY = "SELECT g.genre_id, g.name FROM genre AS g" +
            " JOIN film_genre AS fg ON g.genre_id = fg.genre_id WHERE fg.film_id = ?";

    public GenreDbStorage(JdbcTemplate jdbc, RowMapper<Genre> mapper) {
        super(jdbc, mapper);
    }

    public Genre create(String name) {
        long id = insert(INSERT_GENRE_QUERY, name);
        Genre genre = new Genre();
        genre.setId(id);
        genre.setName(name);
        return genre;
    }

    public Collection<Genre> findAll() {
        return findMany(FIND_ALL_GENRE_QUERY);
    }

    public Optional<Genre> findById(Long genreId) {
        return findOne(FIND_GENRE_BY_ID_QUERY, genreId);
    }

    public Long getTotalGenres() {
        return count(COUNT_GENRES_QUERY);
    }

    public Collection<Genre> findGenresByFilmId(Long filmId) {
        return findMany(FIND_GENRES_BY_FILM_ID_QUERY, filmId);
    }
}
