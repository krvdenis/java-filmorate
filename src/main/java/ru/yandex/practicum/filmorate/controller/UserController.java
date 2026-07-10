package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.dto.UserDto;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.UserService;

import java.util.Collection;
import java.util.List;

@RestController
@Slf4j
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @GetMapping
    public Collection<User> findAll() {
        log.info("Поступил запрос на список всех пользователей");
        return userService.getAllUsers();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UserDto create(@Valid @RequestBody User user) {
        log.info("Поступил запрос на создание пользователя: {}", user);
        return userService.createUser(user);
    }

    @PutMapping
    public User update(@Valid @RequestBody User newUser) {
        log.debug("Поступил запрос на обновление пользователя: {}", newUser);
        return userService.updateUser(newUser);
    }

    @GetMapping("/{id}")
    public UserDto findUserById(@PathVariable Long id) {
        log.info("Поступил запрос на поиск пользователя с ID: {}", id);
        return userService.findUserById(id);
    }

    @PutMapping("/{id}/friends/{friendId}")
    public User addFriend(@PathVariable("id") Long userId, @PathVariable Long friendId) {
        log.info("Поступил запрос на добавление пользователя с ID {}  в список друзей пользователя с ID: {}", friendId,
                userId);
        return userService.addFriend(userId, friendId);
    }

    @DeleteMapping("/{id}/friends/{friendId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteFriend(@PathVariable("id") Long userId, @PathVariable Long friendId) {
        log.info("Поступил запрос на удаление из списка друзей пользователя с ID {} пользователя с ID: {}", userId,
                friendId);
        userService.deleteFriend(userId, friendId);
    }

    @GetMapping("/{id}/friends")
    public Collection<User> getFriends(@PathVariable("id") Long userId) {
        log.info("Поступил запрос на список друзей пользователя с ID: {}", userId);
        return userService.getUserFriends(userId);
    }

    @GetMapping("/{id}/friends/common/{otherId}")
    public Collection<User> getCommonFriends(@PathVariable Long id, @PathVariable Long otherId) {
        log.info("Поступил запрос на список общих друзей пользователя с ID {} и пользователя с ID: {}", id,
                otherId);
        return userService.getCommonFriends(id, otherId);
    }

}
