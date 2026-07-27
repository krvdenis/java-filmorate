package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dto.NewUserRequest;
import ru.yandex.practicum.filmorate.dto.UserDto;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.mapper.UserMapper;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.dal.UserStorage;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Slf4j
public class UserService {
    private final UserStorage userStorage;

    public UserService(@Qualifier("userDbStorage") UserStorage userStorage) {
        this.userStorage = userStorage;
    }

    public UserDto createUser(NewUserRequest newUserRequest) {
        if (newUserRequest == null) {
            log.warn("Попытка добавить пользователя с пустыми данными");
            throw new ValidationException("Невозможно добавить пользователя с пустыми данными");
        }
        log.debug("Попытка зарегистрировать нового пользователя: {}", newUserRequest);

        User user = UserMapper.mapToUser(newUserRequest);
        UserDto userDto = UserMapper.mapToUserDto(userStorage.createUser(user));
        log.info("Пользователь {} успешно зарегистрирован под ID {}", userDto.getLogin(), userDto.getId());
        return userDto;
    }

    public UserDto updateUser(User newUser) {
        log.debug("Попытка внести изменения в данные пользователя {}", newUser);
        if (newUser.getId() == null) {
            log.warn("Отсутствует ID у объекта. Данные: {}", newUser);
            throw new ValidationException("Id должен быть указан");
        }

        User updatedUser = userStorage.updateUser(newUser);
        updatedUser.setFriends(new HashSet<>(getFriendsId(updatedUser.getId())));
        log.info("Данные пользователя с ID {} успешно обновлены", updatedUser.getId());
        return UserMapper.mapToUserDto(updatedUser);
    }

    public Collection<UserDto> findAllUsers() {
        log.debug("Попытка получить список всех пользователей");

        Collection<User> users = userStorage.findAllUsers();

        if (users.isEmpty()) {
            log.info("Список пользователей пуст");
            return Collections.emptyList();
        }

        Set<Long> allUserIds = users.stream()
                .map(User::getId)
                .collect(Collectors.toSet());

        log.debug("Собрано {} уникальных ID пользователей для запроса друзей", allUserIds.size());

        Map<Long, Collection<Long>> userToFriendsMap = userStorage.getUserFriendsMap(allUserIds);
        Collection<UserDto> usersWithFriends = users.stream()
                .map(user -> {
                    Collection<Long> friendsId = userToFriendsMap.getOrDefault(user.getId(), Collections.emptyList());
                    user.setFriends(new HashSet<>(friendsId));
                    return UserMapper.mapToUserDto(user);
                })
                .collect(Collectors.toList());

        log.info("Отправлен список из {} пользователей с друзьями", usersWithFriends.size());
        return usersWithFriends;
    }

    public UserDto findUserById(Long userId) {
        if (userId == null) {
            log.warn("Попытка найти пользователя без указания ID пользователя");
            throw new ValidationException("ID пользователей должен быть указан!");
        }
        log.debug("Попытка найти пользователя: userId={}", userId);

        Optional<User> userOptional = userStorage.findUserById(userId);
        if (userOptional.isEmpty()) {
            log.warn("Запрашиваемый пользователь с ID {} не найден", userId);
        }

        UserDto userDto = userOptional
                .map(UserMapper::mapToUserDto)
                .orElseThrow(() -> new NotFoundException("Пользователь с ID " + userId + " не найден!"));
        userDto.setFriends(new ArrayList<>(getFriendsId(userDto.getId())));
        log.info("Отправлена информация о пользователе c ID: {}", userDto.getId());
        return userDto;
    }

    public UserDto addFriend(Long userId, Long newFriendId) {
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
        user.setFriends(new HashSet<>(getFriendsId(user.getId())));
        log.info("Пользователь с ID={} добавлен в список друзей пользователя с ID={}", newFriendId, userId);
        return UserMapper.mapToUserDto(user);
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

    public Collection<UserDto> getUserFriends(Long userId) {
        log.debug("Попытка запроса списка друзей: userId={}", userId);
        if (userId == null) {
            log.warn("Попытка запроса списка общих друзей без указания ID пользователя");
            throw new ValidationException("ID пользователя должен быть указан!");
        }

        Collection<UserDto> friends = userStorage.getUserFriends(userId).stream()
                .peek(user -> user.setFriends(new HashSet<>(getFriendsId(user.getId()))))
                .map(UserMapper::mapToUserDto)
                .collect(Collectors.toList());

        log.info("Отправлен список друзей размером: {}", friends.size());
        return friends;
    }

    public Collection<UserDto> getCommonFriends(Long userId, Long otherId) {
        log.debug("Попытка запроса списка общих друзей: userId={}, otherId={}", userId, otherId);

        if (otherId == null || userId == null) {
            log.warn("Попытка запроса списка общих друзей без указания ID одного или обоих пользователей");
            throw new ValidationException("ID пользователей должен быть указан!");
        }

        Collection<User> commonFriends = userStorage.getCommonFriends(userId, otherId);

        if (commonFriends.isEmpty()) {
            return Collections.emptyList();
        }

        Set<Long> commonFriendIds = commonFriends.stream()
                .map(User::getId)
                .collect(Collectors.toSet());

        log.debug("Найдено {} общих друзей с ID: {}", commonFriendIds.size(), commonFriendIds);

        Map<Long, Collection<Long>> userToFriendsMap = userStorage.getUserFriendsMap(commonFriendIds);
        Collection<UserDto> commonFriendsWithFriends = commonFriends.stream()
                .map(user -> {
                    Collection<Long> friendsId = userToFriendsMap.getOrDefault(user.getId(), Collections.emptyList());
                    user.setFriends(new HashSet<>(friendsId));
                    return UserMapper.mapToUserDto(user);
                })
                .collect(Collectors.toList());

        log.info("Отправлен список общих друзей размером: {}", commonFriendsWithFriends.size());
        return commonFriendsWithFriends;
    }

    private Collection<Long> getFriendsId(Long userId) {
        log.debug("Попытка запроса ID друзей пользователя с userId={}", userId);

        Collection<Long> friendsId = userStorage.getUserFriends(userId).stream()
                .map(User::getId)
                .collect(Collectors.toList());

        log.info("Отправлен список ID друзей размером: {}", friendsId.size());
        return friendsId;
    }
}