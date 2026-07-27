package ru.yandex.practicum.filmorate.dal;

import ru.yandex.practicum.filmorate.model.User;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public interface UserStorage {

    User createUser(User user);

    User updateUser(User newUser);

    Collection<User> findAllUsers();

    Optional<User> findUserById(Long userId);

    User addFriend(Long userId, Long newFriendId);

    void deleteFriend(Long userId, Long friendId);

    Collection<User> getUserFriends(Long userId);

    Collection<User> getCommonFriends(Long userId, Long otherId);

    Map<Long, Collection<Long>> getUserFriendsMap(Set<Long> allUserIds);
}