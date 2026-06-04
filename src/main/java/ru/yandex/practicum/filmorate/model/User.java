package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

import java.time.LocalDate;

/**
 * User.
 */
@Data
public class User {
    private Long id;
    @Email
    @NotBlank
    private String email;

    @NotBlank
    @Pattern(
            regexp = "^[a-zA-Zа-яА-я0-9_@.-]{4,16}$",
            message = "Логин должен содержать от 4 до 16 символов и может состоять из: букв, цифр, @, ., -, _."
    )
    private String login;
    private String name;
    @PastOrPresent
    private LocalDate birthday;
}
