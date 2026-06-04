package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.DuplicatedDataException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.util.Collection;
import java.util.HashMap;

@RestController
@RequestMapping("/users")
public class UserController {
    private static final Logger log = LoggerFactory.getLogger(UserController.class);
    private final HashMap<Long, User> users = new HashMap<>();

    @GetMapping
    public Collection<User> findAll() {
        log.info("Пользователю отправлен список всех пользователей");
        return users.values();
    }

    @PostMapping
    public User create(@Valid @RequestBody User user) {
        boolean isLoginAlreadyExists = users.values().stream()
                .anyMatch(value -> value.getLogin().equals(user.getLogin()));
        boolean isEmailAlreadyExists = users.values().stream()
                .anyMatch(value -> value.getEmail().equals(user.getEmail()));

        if (isLoginAlreadyExists) {
            log.warn("Попытка регистрации с уже используемым логином: {}. Пользователь: {}", user.getLogin(), user);
            throw new DuplicatedDataException("Этот логин уже используется");
        }

        if (isEmailAlreadyExists) {
            log.warn("Попытка регистрации с уже используемым email: {}. Пользователь: {}", user.getEmail(), user);
            throw new DuplicatedDataException("Эта электронная почта уже используется");
        }

        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }

        user.setId(getNextId());
        users.put(user.getId(), user);
        log.info("Пользователь {} успешно зарегистрирован под ID {}", user.getLogin(), user.getId());
        return user;

    }

    @PutMapping
    public User update(@Valid @RequestBody User newUser) {
        if (newUser.getId() == null) {
            log.warn("Отсутствует ID у объекта. Данные: {}", newUser);
            throw new ValidationException("Id должен быть указан");
        }

        boolean isLoginAlreadyExists = users.values().stream()
                .anyMatch(value -> value.getLogin().equals(newUser.getLogin()));
        boolean isEmailAlreadyExists = users.values().stream()
                .anyMatch(value -> value.getEmail().equals(newUser.getEmail()));

        if (users.containsKey(newUser.getId())) {
            User oldUser = users.get(newUser.getId());

            if (newUser.getName() != null) {
                oldUser.setName(newUser.getName());
                log.info("Пользователь под ID {} сменил имя на: {}", newUser.getId(), newUser.getName());
            }

            if (newUser.getEmail() != null && !oldUser.getEmail().equals(newUser.getEmail())) {
                if (isEmailAlreadyExists) {
                    log.warn("Попытка изменить email на уже используемое другим пользователем значение. Email: {}." +
                            " Пользователь: {}", newUser.getEmail(), newUser);
                    throw new DuplicatedDataException("Эта электронная почта уже используется");
                }
                oldUser.setEmail(newUser.getEmail());
                log.info("Пользователь под ID {} сменил email на: {}", newUser.getId(), newUser.getEmail());
            }

            if (newUser.getLogin() != null && !oldUser.getLogin().equals(newUser.getLogin())) {
                if (isLoginAlreadyExists) {
                    log.warn("Попытка изменить логин на уже используемое другим пользователем значение. Login: {}." +
                            " Пользователь: {}", newUser.getLogin(), newUser);
                    throw new DuplicatedDataException("Этот логин уже используется");
                }
                oldUser.setLogin(newUser.getLogin());
                log.info("Пользователь под ID {} сменил логин на: {}", newUser.getId(), newUser.getLogin());
            }

            if (newUser.getBirthday() != null) {
                oldUser.setBirthday(newUser.getBirthday());
                log.info("Пользователь под ID {} сменил дату рождения на: {}", newUser.getId(), newUser.getBirthday());
            }
            return oldUser;
        }
        log.error("Пользователь под {} ID не найден.", newUser.getId());
        throw new NotFoundException("Пользователь с id = " + newUser.getId() + " не найден");
    }

    private long getNextId() {
        long currentMaxId = users.keySet()
                .stream()
                .mapToLong(id -> id)
                .max()
                .orElse(0);

        return ++currentMaxId;
    }
}
