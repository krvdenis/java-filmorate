package ru.yandex.practicum.filmorate.mapper;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import ru.yandex.practicum.filmorate.dto.GenreDto;
import ru.yandex.practicum.filmorate.model.Genre;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class GenreMapper {

    public static GenreDto mapToGenreDto(Genre genre) {
        GenreDto dto = null;

        if (genre != null) {
            dto = new GenreDto();
            dto.setId(genre.getId());
            dto.setName(genre.getName());
        }
        return dto;
    }

    public static Genre mapToGenre(GenreDto genreDto) {
        Genre genre = null;

        if (genreDto != null) {
            genre = new Genre();
            genre.setId(genreDto.getId());
            genre.setName(genreDto.getName());
        }
        return genre;
    }
}