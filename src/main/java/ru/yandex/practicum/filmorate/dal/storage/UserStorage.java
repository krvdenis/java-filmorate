package ru.yandex.practicum.filmorate.dal.storage;

import ru.yandex.practicum.filmorate.model.User;

import java.util.Collection;
import java.util.Optional;

public interface UserStorage {

    User createUser(User user);

    User updateUser(User newUser);

    Collection<User> findAllUsers();

    Optional<User> findUserById(Long userId);

    User addFriend(Long userId, Long newFriendId);

    void deleteFriend(Long userId, Long friendId);

    Collection<User> getUserFriends(Long userId);

    Collection<User> getCommonFriends(Long userId, Long otherId);
}
