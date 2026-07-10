package ru.yandex.practicum.filmorate.dal.mappers;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Film;

import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;

@Component
public class FilmRowMapper implements RowMapper<Film> {
    @Override
    public Film mapRow(ResultSet rs, int rowNum) throws SQLException {
        Film film = new Film();
        film.setId(rs.getLong("id"));
        film.setName(rs.getString("name"));
        film.setMpaRatingId(rs.getLong("mpa_rating_id"));

        Date releaseDate = rs.getDate("release_date");
        film.setReleaseDate(releaseDate.toLocalDate());
        film.setDescription(rs.getString("description"));
        film.setDuration(rs.getInt("duration"));
        return film;
    }
}
