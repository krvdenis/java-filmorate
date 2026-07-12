package ru.yandex.practicum.filmorate.model;

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

    private MpaRating mpa; // кажется, лучше добавить Long вместо MpaRating
    private Set<Genre> genres = new HashSet<>(); // кажется, лучше добавить Long вместо Genre

    @Positive
    private int duration;

    private Set<Long> likes = new HashSet<>(); // нужно будет убрать

    public int getTotalLikes() { //нужно будет заменить из-за того, что убираю likes
        return likes.size();
    } // ни к село ни к городу

}
