package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserService {
    private final UserStorage inMemoryUserStorage;

    public User addFriend(Long userId, Long newFriendId) {
        log.debug("Попытка добавления в друзья: userId={}, friendId={}", userId, newFriendId);
        if (newFriendId == null || userId == null) {
            log.warn("Попытка добавление в друзья без указания ID одного или обоих пользователей");
            throw new ValidationException("ID пользователей должен быть указан!");
        }
        if (!inMemoryUserStorage.getUsers().containsKey(newFriendId)) {
            log.warn("Пользователь с ID {} не существует", newFriendId);
            throw new NotFoundException("Пользователь с ID " + newFriendId + " не существует!");
        }
        if (!inMemoryUserStorage.getUsers().containsKey(userId)) {
            log.warn("Пользователь с ID {} не существует", userId);
            throw new NotFoundException("Пользователь с ID " + userId + " не существует!");
        }
        if (userId.equals(newFriendId)) {
            log.warn("Попытка добавление самого себя в список своих друзей");
            throw new ValidationException("Нельзя добавить себя в друзья");
        }

        User user = getUser(userId);
        Set<Long> userFriends = user.getFriends();
        userFriends.add(newFriendId);
        User newFriend = getUser(newFriendId);
        newFriend.getFriends().add(userId);
        log.info("Пользователь с ID={} добавлен в список друзей пользователя с ID={}", newFriend, userId);
        return user;
    }

    public void deleteFriend(Long userId, Long friendId) {
        log.debug("Попытка удаления из друзей: userId={}, friendId={}", userId, friendId);
        if (friendId == null || userId == null) {
            log.warn("Попытка удаления из друзей без указания ID одного или обоих пользователей");
            throw new ValidationException("ID пользователей должен быть указан!");
        }
        if (!inMemoryUserStorage.getUsers().containsKey(userId)) {
            log.warn("Пользователь с ID {} не существует", userId);
            throw new NotFoundException("Пользователь с ID " + userId + " не существует!");
        }
        if (!inMemoryUserStorage.getUsers().containsKey(friendId)) {
            log.warn("Пользователь с ID {} не существует", friendId);
            throw new NotFoundException("Пользователь с ID " + friendId + " не существует!");
        }
        if (userId.equals(friendId)) {
            log.warn("Пользователь указал свой же ID в запросе на удаления из друзей");
            throw new ValidationException("Сам пользователь не может находиться в своём списке друзей");
        }

        User user = getUser(userId);
        Set<Long> friends = user.getFriends();
        friends.remove(friendId);
        User exFriend = getUser(friendId);
        log.info("Пользователь с ID={} удалён из списка друзей пользователя с ID={}", friendId, userId);
        exFriend.getFriends().remove(user.getId());

    }

    public List<User> getCommonFriends(Long userId, Long otherId) {
        log.debug("Попытка запроса списка общих друзей: userId={}, otherId={}", userId, otherId);
        if (otherId == null || userId == null) {
            log.warn("Попытка запроса списка общих друзей без указания ID одного или обоих пользователей");
            throw new ValidationException("ID пользователей должен быть указан!");
        }
        if (!inMemoryUserStorage.getUsers().containsKey(userId)) {
            log.warn("Пользователь с ID {} не существует", userId);
            throw new NotFoundException("Пользователь с ID " + userId + " не существует!");
        }
        if (!inMemoryUserStorage.getUsers().containsKey(otherId)) {
            log.warn("Пользователь с ID {} не существует", otherId);
            throw new NotFoundException("Пользователь с ID " + otherId + " не существует!");
        }

        Set<Long> userFriends = getUser(userId).getFriends();
        Set<Long> otherFriends = getUser(otherId).getFriends();
        List<User> generalFriends = new ArrayList<>();

        for (Long userFriendId : userFriends) {
            if (otherFriends.contains(userFriendId)) {
                generalFriends.add(getUser(userFriendId));
            }
        }
        log.info("Найдено общих друзей: {}", generalFriends.size());
        return generalFriends;
    }

    public List<User> getUserFriends(Long userId) {
        log.debug("Попытка запроса списка друзей: userId={}", userId);
        if (userId == null) {
            log.warn("Попытка запроса списка общих друзей без указания ID пользователя");
            throw new ValidationException("ID пользователя должен быть указан!");
        }
        if (!inMemoryUserStorage.getUsers().containsKey(userId)) {
            log.warn("Пользователь с ID {} не существует", userId);
            throw new NotFoundException("Пользователь с ID " + userId + " не существует!");
        }

        User user = getUser(userId);
        List<User> friends = user.getFriends().stream()
                .map(this::getUser)
                .toList();

        log.info("Найдено друзей: {}", friends.size());
        return friends;
    }

    public Collection<User> getAllUsers() {
        return inMemoryUserStorage.findAll();
    }

    public User createUser(User user) {
        return inMemoryUserStorage.create(user);
    }

    public User updateUser(User newUser) {
        return inMemoryUserStorage.update(newUser);
    }

    public User findUserById(Long userId) {
        log.debug("Попытка найти пользователя: userId={}", userId);
        if (userId == null) {
            log.warn("Попытка найти пользователя без указания ID пользователя");
            throw new ValidationException("ID пользователей должен быть указан!");
        }
        if (!inMemoryUserStorage.getUsers().containsKey(userId)) {
            log.warn("Пользователь с ID {} не существует", userId);
            throw new NotFoundException("Пользователь с ID " + userId + " не существует!");
        }
        log.info("Отправлена информация о пользователе: {}", getUser(userId));
        return getUser(userId);
    }

    private User getUser(Long id) {
        return inMemoryUserStorage.getUsers().get(id);
    }
}
