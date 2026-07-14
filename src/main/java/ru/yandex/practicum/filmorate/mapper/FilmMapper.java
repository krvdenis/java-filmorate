package ru.yandex.practicum.filmorate.mapper;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import ru.yandex.practicum.filmorate.dto.FilmDto;
import ru.yandex.practicum.filmorate.dto.NewFilmRequest;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.stream.Collectors;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class FilmMapper {

    public static FilmDto mapToFilmDto(Film film) {
        FilmDto dto = new FilmDto();
        dto.setId(film.getId());
        dto.setName(film.getName());
        dto.setReleaseDate(film.getReleaseDate());
        dto.setDescription(film.getDescription());
        dto.setMpa(MpaRatingMapper.mapToMpaDto(film.getMpa()));

        if (film.getGenres() != null) {
            dto.setGenres(film.getGenres().stream()
                    .sorted(Comparator.comparing(Genre::getId))
                    .map(GenreMapper::mapToGenreDto)
                    .collect(Collectors.toList()));
        } else {
            dto.setGenres(Collections.emptyList());
        }
        dto.setDuration(film.getDuration());

        if (film.getLikes() != null) {
            dto.setLikes(film.getLikes());
        } else {
            dto.setLikes(new HashSet<>());
        }
        return dto;
    }

    public static Film mapToFilmFromDto(FilmDto filmDto) {
        Film film = new Film();
        film.setId(filmDto.getId());
        film.setName(filmDto.getName());
        film.setReleaseDate(filmDto.getReleaseDate());
        film.setDescription(filmDto.getDescription());
        film.setMpa(MpaRatingMapper.mapToMpa(filmDto.getMpa()));

        if (filmDto.getGenres() != null) {
            film.setGenres(new HashSet<>(filmDto.getGenres().stream()
                    .map(GenreMapper::mapToGenre)
                    .collect(Collectors.toList())));
        } else {
            film.setGenres(new HashSet<>());
        }
        film.setDuration(filmDto.getDuration());
        return film;
    }

    public static Film mapToFilm(NewFilmRequest newFilmRequest) {
        Film film = new Film();
        film.setName(newFilmRequest.getName());
        film.setMpa(newFilmRequest.getMpa());
        film.setReleaseDate(newFilmRequest.getReleaseDate());
        film.setDescription(newFilmRequest.getDescription());
        film.setDuration(newFilmRequest.getDuration());
        film.setGenres(newFilmRequest.getGenres());
        return film;
    }
}