package ru.yandex.practicum.filmorate.storage;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.DuplicatedDataException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.util.Collection;
import java.util.HashMap;

@Component("inMemoryUserStorage")
@Slf4j
@Getter
public class InMemoryUserStorage implements UserStorage {
    private final HashMap<Long, User> users = new HashMap<>();

    @Override
    public Collection<User> findAll() {
        log.debug("Попытка получить список всех пользователей");
        log.info("Пользователю отправлен список из {} пользователей", users.size());
        return users.values();
    }

    @Override
    public User create(User user) {
        log.debug("Попытка зарегистрировать нового пользователя: {}", user);
        if (user == null) {
            log.warn("Попытка добавить пользователя с пустыми данными");
            throw new ValidationException("Невозможно добавить пользователя с пустыми данными");
        }
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
            log.debug("Передано пустое поле с именем пользователя, вместо имени будет использован логин {}",
                    user.getLogin());
            user.setName(user.getLogin());
        }

        user.setId(getNextId());
        users.put(user.getId(), user);
        log.info("Пользователь {} успешно зарегистрирован под ID {}", user.getLogin(), user.getId());
        return user;

    }

    @Override
    public User update(User newUser) {
        log.debug("Попытка внести изменения в данные пользователя {}", newUser);
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
