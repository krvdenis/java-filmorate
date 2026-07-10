package ru.yandex.practicum.filmorate.mapper;

import ru.yandex.practicum.filmorate.dto.UserDto;
import ru.yandex.practicum.filmorate.model.User;

public class UserMapper {
    public static UserDto mapToUserDto (User user) {
        UserDto dto = new UserDto();
        dto.setId(user.getId());
        dto.setLogin(user.getLogin());
        dto.setName(user.getName());
        dto.setEmail(user.getEmail());
        dto.setBirthday(user.getBirthday());

        return dto;
    }
}
