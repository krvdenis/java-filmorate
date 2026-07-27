package ru.yandex.practicum.filmorate.dto;

import lombok.Data;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

@Data
public class FilmDto {
    private Long id;
    private String name;
    private LocalDate releaseDate;
    private String description;
    private MpaRatingDto mpa;
    private List<GenreDto> genres;
    private int duration;
    private Set<Long> likes;

    public int getTotalLikes() { //нужно будет заменить из-за того, что убираю likes
        return likes.size();
    }
}
