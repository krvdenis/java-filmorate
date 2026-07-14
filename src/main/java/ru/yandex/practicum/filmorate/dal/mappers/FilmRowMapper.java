package ru.yandex.practicum.filmorate.dal.mappers;

import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.MpaRating;

import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;

@Slf4j
@Component
public class FilmRowMapper implements RowMapper<Film> {

    @Override
    public Film mapRow(ResultSet rs, int rowNum) throws SQLException {
        Film film = new Film();
        film.setId(rs.getLong("id"));
        film.setName(rs.getString("name"));
        Date releaseDate = rs.getDate("release_date");

        if (releaseDate != null) {
            film.setReleaseDate(releaseDate.toLocalDate());
        }
        film.setDescription(rs.getString("description"));
        film.setDuration(rs.getInt("duration"));

        String userIdsStr = rs.getString("users_id");
        if (userIdsStr != null && !userIdsStr.isEmpty()) {
            String[] usersId = userIdsStr.split(",");
            for (String userIdStr : usersId) {
                Long userId = Long.parseLong(userIdStr.trim());
                film.getLikes().add(userId);
            }
        }

        Long mpaId = rs.getLong("mpa_rating_id");
        if (mpaId != 0) {
            MpaRating mpaRating = new MpaRating();
            mpaRating.setId(mpaId);
            mpaRating.setName(rs.getString("mpa_name"));
            film.setMpa(mpaRating);
        } else {
            film.setMpa(null);
        }

        String genreNamesStr = rs.getString("genres_name");
        String genreIdsStr = rs.getString("genres_id");
        String[] genresName = genreNamesStr != null ? genreNamesStr.split(",") : new String[0];
        String[] genresId = genreIdsStr != null ? genreIdsStr.split(",") : new String[0];

        if (genresName.length != genresId.length) {
            log.warn("Несоответствие количества жанров: имён — {}, ID — {}",
                    genresName.length, genresId.length);
        } else {
            for (int i = 0; i < genresName.length; i++) {
                Genre genre = new Genre();
                genre.setId(Long.parseLong(genresId[i].trim()));
                genre.setName(genresName[i]);
                film.getGenres().add(genre);
            }
        }
        return film;
    }
}