package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dto.UserDto;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.mapper.UserMapper;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.dal.storage.UserStorage;

import java.util.*;

@Service
@Slf4j

public class UserService {

    private final UserStorage userStorage;

    public UserService(@Qualifier("userDbStorage") UserStorage userStorage) {
        this.userStorage = userStorage;
    }

    public UserDto createUser(User user) {
        log.debug("Попытка зарегистрировать нового пользователя: {}", user);
        if (user == null) {
            log.warn("Попытка добавить пользователя с пустыми данными");
            throw new ValidationException("Невозможно добавить пользователя с пустыми данными");
        }
        log.info("Пользователь {} успешно зарегистрирован под ID {}", user.getLogin(), user.getId());
        return UserMapper.mapToUserDto(userStorage.createUser(user));
    }

    public User updateUser(User newUser) {
        log.debug("Попытка внести изменения в данные пользователя {}", newUser);
        if (newUser.getId() == null) {
            log.warn("Отсутствует ID у объекта. Данные: {}", newUser);
            throw new ValidationException("Id должен быть указан");
        }
        User updatedUser = userStorage.updateUser(newUser);
        log.info("Данные пользователя с ID {} успешно обновлены", newUser.getId());
        return updatedUser;
    }

    public Collection<User> getAllUsers() {
        log.debug("Попытка получить список всех пользователей");
        Collection<User> users = userStorage.findAllUsers();
        log.info("Пользователю отправлен список из {} пользователей", users.size());
        return users;
    }

    public UserDto findUserById(Long userId) {
        log.debug("Попытка найти пользователя: userId={}", userId);
        if (userId == null) {
            log.warn("Попытка найти пользователя без указания ID пользователя");
            throw new ValidationException("ID пользователей должен быть указан!");
        }
        Optional<User> userOptional = userStorage.findUserById(userId);

        if (userOptional.isEmpty()) {
            log.warn("Запрашиваемый пользователь с ID {} не найден", userId);
        }
        UserDto userDto = userOptional
                .map(UserMapper::mapToUserDto)
                .orElseThrow(() -> new NotFoundException("Пользователь с ID " + userId + " не найден!"));
        log.info("Отправлена информация о пользователе c ID: {}", userId);
        return userDto;
    }

    public User addFriend(Long userId, Long newFriendId) {
        log.debug("Попытка добавления в друзья: userId={}, friendId={}", userId, newFriendId);
        if (newFriendId == null || userId == null) {
            log.warn("Попытка добавление в друзья без указания ID одного или обоих пользователей");
            throw new ValidationException("ID пользователей должен быть указан!");
        }
        if (userId.equals(newFriendId)) {
            log.warn("Попытка добавление самого себя в список своих друзей");
            throw new ValidationException("Нельзя добавить себя в друзья");
        }
        User user = userStorage.addFriend(userId, newFriendId);
        log.info("Пользователь с ID={} добавлен в список друзей пользователя с ID={}", newFriendId, userId);
        return user;
    }

    public void deleteFriend(Long userId, Long friendId) {
        log.debug("Попытка удаления из друзей: userId={}, friendId={}", userId, friendId);
        if (friendId == null || userId == null) {
            log.warn("Попытка удаления из друзей без указания ID одного или обоих пользователей");
            throw new ValidationException("ID пользователей должен быть указан!");
        }
        if (userId.equals(friendId)) {
            log.warn("Пользователь указал свой же ID в запросе на удаления из друзей");
            throw new ValidationException("Сам пользователь не может находиться в своём списке друзей");
        }
        userStorage.deleteFriend(userId, friendId);
        log.info("Пользователь с ID={} удалён из списка друзей пользователя с ID={}", friendId, userId);

    }

    public Collection<User> getUserFriends(Long userId) {
        log.debug("Попытка запроса списка друзей: userId={}", userId);
        if (userId == null) {
            log.warn("Попытка запроса списка общих друзей без указания ID пользователя");
            throw new ValidationException("ID пользователя должен быть указан!");
        }
        Collection<User> friends = userStorage.getUserFriends(userId);
        log.info("Найдено друзей: {}", friends.size());
        return friends;
    }

    public Collection<User> getCommonFriends(Long userId, Long otherId) {
        log.debug("Попытка запроса списка общих друзей: userId={}, otherId={}", userId, otherId);
        if (otherId == null || userId == null) {
            log.warn("Попытка запроса списка общих друзей без указания ID одного или обоих пользователей");
            throw new ValidationException("ID пользователей должен быть указан!");
        }
        Collection<User> commonFriends = userStorage.getCommonFriends(userId, otherId);
        log.info("Найдено общих друзей: {}", commonFriends.size());
        return commonFriends;
    }

}
