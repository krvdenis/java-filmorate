package ru.yandex.practicum.filmorate.dal.storage;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.DuplicateDataException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Component("inMemoryUserStorage")
@Slf4j
@Getter
public class InMemoryUserStorage implements UserStorage {
    private final HashMap<Long, User> users = new HashMap<>();

    @Override
    public Collection<User> findAllUsers() {
        return users.values();
    }

    @Override
    public User createUser(User user) {
        boolean isLoginAlreadyExists = users.values().stream()
                .anyMatch(value -> value.getLogin().equals(user.getLogin()));
        boolean isEmailAlreadyExists = users.values().stream()
                .anyMatch(value -> value.getEmail().equals(user.getEmail()));

        if (isLoginAlreadyExists) {
            log.warn("Попытка регистрации с уже используемым логином: {}. Пользователь: {}", user.getLogin(), user);
            throw new DuplicateDataException("Этот логин уже используется");
        }

        if (isEmailAlreadyExists) {
            log.warn("Попытка регистрации с уже используемым email: {}. Пользователь: {}", user.getEmail(), user);
            throw new DuplicateDataException("Эта электронная почта уже используется");
        }

        if (user.getName() == null || user.getName().isBlank()) {
            log.debug("Передано пустое поле с именем пользователя, вместо имени будет использован логин {}",
                    user.getLogin());
            user.setName(user.getLogin());
        }

        user.setId(getNextId());
        users.put(user.getId(), user);
        return user;
    }


    @Override
    public User updateUser(User newUser) {
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
                    throw new DuplicateDataException("Эта электронная почта уже используется");
                }
                oldUser.setEmail(newUser.getEmail());
                log.info("Пользователь под ID {} сменил email на: {}", newUser.getId(), newUser.getEmail());
            }

            if (newUser.getLogin() != null && !oldUser.getLogin().equals(newUser.getLogin())) {
                if (isLoginAlreadyExists) {
                    log.warn("Попытка изменить логин на уже используемое другим пользователем значение. Login: {}." +
                            " Пользователь: {}", newUser.getLogin(), newUser);
                    throw new DuplicateDataException("Этот логин уже используется");
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

    @Override
    public Optional<User> findUserById(Long userId) {

        if (!users.containsKey(userId)) {
            log.warn("Запрашиваемый пользователь с ID {} не найден", userId);
            throw new NotFoundException("Пользователь с ID " + userId + " не найден!");
        }
        User result = users.get(userId);
        return Optional.ofNullable(result);
    }

    @Override
    public User addFriend(Long userId, Long newFriendId) {
        if (!users.containsKey(newFriendId)) {
            log.warn("Пользователь, которого необходимо добавить в друзья, с ID {} не найден", newFriendId);
            throw new NotFoundException("Пользователь с ID " + newFriendId + " не найден!");
        }
        if (!users.containsKey(userId)) {
            log.warn("Пользователь с ID {} не найден", userId);
            throw new NotFoundException("Пользователь с ID " + userId + " не найден!");
        }

        User user = users.get(userId);
        Set<Long> userFriends = user.getFriends();
        userFriends.add(newFriendId);
        User newFriend = users.get(newFriendId);
        newFriend.getFriends().add(userId);
        return user;
    }

    @Override
    public void deleteFriend(Long userId, Long friendId) {
        if (!users.containsKey(userId)) {
            log.warn("Пользователь с ID {} не найден", userId);
            throw new NotFoundException("Пользователь с ID " + userId + " не найден!");
        }

        if (!users.get(userId).getFriends().contains(friendId)) {
            log.warn("Пользователя, которого необходимо удалить из друзей, с ID {} нет в списке друзей", friendId);
            throw new NotFoundException("Пользователь с ID " + friendId + " не найден в списке друзей!");
        }

        User user = users.get(userId);
        Set<Long> friends = user.getFriends();
        friends.remove(friendId);
        User exFriend = users.get(friendId);
        exFriend.getFriends().remove(userId);
    }

    @Override
    public Collection<User> getUserFriends(Long userId) {
        if (!users.containsKey(userId)) {
            log.warn("Пользователь с ID {} не найден", userId);
            throw new NotFoundException("Пользователь с ID " + userId + " не найден!");
        }

        User user = users.get(userId);
        return user.getFriends().stream()
                .map(users::get)
                .toList();
    }

    @Override
    public Collection<User> getCommonFriends(Long userId, Long otherId) {
        if (!users.containsKey(userId)) {
            log.warn("Пользователь с ID {} не найден", userId);
            throw new NotFoundException("Пользователь с ID " + userId + " не найден!");
        }
        if (!users.containsKey(otherId)) {
            log.warn("Пользователь, с которым нужно найти общих друзей, с ID {} не найден", otherId);
            throw new NotFoundException("Пользователь с ID " + otherId + " не найден!");
        }

        Set<Long> userFriends = users.get(userId).getFriends();
        Set<Long> otherFriends = users.get(otherId).getFriends();
        List<User> commonFriends = new ArrayList<>();

        for (Long userFriendId : userFriends) {
            if (otherFriends.contains(userFriendId)) {
                commonFriends.add(users.get(userFriendId));
            }
        }
        return commonFriends;
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
