package ru.yandex.practicum.filmorate.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Data
public class UserDto {
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Long id;
    private String login;
    private String name;
    private String email;
    private LocalDate birthday;
    private final Set<Long> friends = new HashSet<>();
}
