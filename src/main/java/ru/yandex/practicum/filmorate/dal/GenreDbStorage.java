package ru.yandex.practicum.filmorate.dal;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Genre;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@Component
public class GenreDbStorage extends BaseDbStorage<Genre> {
    private static final String FIND_ALL_GENRE_QUERY = """
                SELECT *
                FROM genre
            """;
    private static final String FIND_GENRE_BY_ID_QUERY = """
                SELECT *
                FROM genre
                WHERE genre_id = ?
            """;
    private static final String COUNT_GENRES_QUERY = """
                SELECT COUNT(genre_id)
                FROM genre
            """;
    private static final String FIND_GENRES_BY_FILM_ID_QUERY = """
                SELECT g.*
                FROM genre AS g
                JOIN film_genre AS fg ON g.genre_id = fg.genre_id
                WHERE fg.film_id = ?
            """;
    private final NamedParameterJdbcTemplate namedJdbc;

    public GenreDbStorage(JdbcTemplate jdbc, RowMapper<Genre> mapper) {
        super(jdbc, mapper);
        this.namedJdbc = new NamedParameterJdbcTemplate(jdbc);
    }

    public Collection<Genre> findAll() {
        return findMany(FIND_ALL_GENRE_QUERY);
    }

    public Optional<Genre> findById(Long genreId) {
        return findOne(FIND_GENRE_BY_ID_QUERY, genreId);
    }

    public Long getTotalNumberGenres() {
        return count(COUNT_GENRES_QUERY);
    }

    public Collection<Genre> findGenresByFilmId(Long filmId) {
        return findMany(FIND_GENRES_BY_FILM_ID_QUERY, filmId);
    }

    public Collection<Genre> findGenresByIds(Set<Long> genreIds) {
        if (genreIds == null || genreIds.isEmpty()) {
            return Collections.emptyList();
        }

        String query = "SELECT genre_id, name FROM genre WHERE genre_id IN (:genreIds)";

        Map<String, Object> params = Map.of("genreIds", genreIds);
        return namedJdbc.query(query, params, mapper);
    }
}