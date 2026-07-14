package ru.yandex.practicum.filmorate.mapper;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import ru.yandex.practicum.filmorate.dto.NewUserRequest;
import ru.yandex.practicum.filmorate.dto.UserDto;
import ru.yandex.practicum.filmorate.model.User;

import java.util.Collections;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class UserMapper {

    public static UserDto mapToUserDto(User user) {

        UserDto dto = new UserDto();
        dto.setId(user.getId());
        dto.setLogin(user.getLogin());
        dto.setName(user.getName());
        dto.setEmail(user.getEmail());
        dto.setBirthday(user.getBirthday());

        if (user.getFriends() != null) {
            dto.setFriends(
                    user.getFriends().stream()
                            .sorted().toList()
            );
        } else {
            dto.setFriends(Collections.emptyList());
        }
        return dto;
    }

    public static User mapToUser(NewUserRequest userRequest) {
        User user = new User();
        user.setLogin(userRequest.getLogin());
        user.setName(userRequest.getName());
        user.setEmail(userRequest.getEmail());
        user.setBirthday(userRequest.getBirthday());
        return user;
    }
}