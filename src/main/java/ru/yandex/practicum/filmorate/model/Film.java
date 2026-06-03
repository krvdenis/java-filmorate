package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.*;
import lombok.Data;
import ru.yandex.practicum.filmorate.annotations.NotBefore1895Dec28;

import java.time.LocalDate;

/**
 * Film.
 */
@Data
public class Film {

    private Long id;
    @NotBlank
    private String name;

    @Size(max = 200)
    private String description;
    @NotBefore1895Dec28(message = "Дата должна быть после 28 декабря 1895 года")
    private LocalDate releaseDate;

    @Positive
    private int duration;
}
