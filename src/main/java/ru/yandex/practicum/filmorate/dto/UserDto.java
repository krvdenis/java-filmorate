package ru.yandex.practicum.filmorate.dto;

import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class UserDto {
    private Long id;
    private String login;
    private String name;
    private String email;
    private LocalDate birthday;
    private List<Long> friends;
}