package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
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
    private String login;
    private String name;
    private LocalDate birthday;
}
