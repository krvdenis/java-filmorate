package ru.yandex.practicum.filmorate.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;
import ru.yandex.practicum.filmorate.annotation.NotBefore1895Dec28;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

/**
 * Film.
 */
@Data
public class Film {
    private Long id;

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

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Set<Long> likes = new HashSet<>();

    public int getTotalLikes() { //нужно будет заменить из-за того, что убираю likes
        return likes.size();
    }
}