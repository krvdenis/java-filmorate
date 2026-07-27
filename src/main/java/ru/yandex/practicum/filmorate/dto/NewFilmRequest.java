package ru.yandex.practicum.filmorate.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;
import ru.yandex.practicum.filmorate.annotation.NotBefore1895Dec28;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.MpaRating;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

/**
 * Film.
 */
@Data
public class NewFilmRequest {
    @NotBlank
    private String name;

    @NotBefore1895Dec28(message = "Дата должна быть после 28 декабря 1895 года")
    private LocalDate releaseDate;

    @Size(max = 200)
    private String description;

    private MpaRating mpa;
    private Set<Genre> genres = new HashSet<>();

    @Positive
    private int duration;
}
