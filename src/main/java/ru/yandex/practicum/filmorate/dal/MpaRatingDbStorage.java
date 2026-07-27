package ru.yandex.practicum.filmorate.dal;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.MpaRating;

import java.util.Collection;
import java.util.Optional;

@Component
public class MpaRatingDbStorage extends BaseDbStorage<MpaRating> {
    private static final String FIND_ALL_MPA_RATINGS_QUERY = """
                SELECT *
                FROM mpa_rating
            """;
    private static final String FIND_MPA_RATING_BY_ID_QUERY = """
                SELECT *
                FROM mpa_rating
                WHERE mpa_rating_id = ?
            """;
    private static final String FIND_MPA_RATING_BY_FILM_ID_QUERY = """
                SELECT mr.*
                FROM mpa_rating AS mr
                JOIN film AS f ON mr.mpa_rating_id = f.mpa_rating_id
                WHERE f.id = ?
            """;
    private static final String COUNT_MPA_QUERY = """
                SELECT COUNT(mpa_rating_id)
                FROM mpa_rating
            """;

    public MpaRatingDbStorage(JdbcTemplate jdbc, RowMapper<MpaRating> mapper) {
        super(jdbc, mapper);
    }

    public Collection<MpaRating> findAll() {
        return findMany(FIND_ALL_MPA_RATINGS_QUERY);
    }

    public Optional<MpaRating> findById(Long mpaId) {
        return findOne(FIND_MPA_RATING_BY_ID_QUERY, mpaId);
    }

    public Long getTotalNumberMpa() {
        return count(COUNT_MPA_QUERY);
    }

    public Optional<MpaRating> findMpaByFilmId(Long filmId) {
        return findOne(FIND_MPA_RATING_BY_FILM_ID_QUERY, filmId);
    }
}