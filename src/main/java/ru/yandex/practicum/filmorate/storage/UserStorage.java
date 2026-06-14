package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.User;

import java.util.Collection;
import java.util.HashMap;

public interface UserStorage {

    Collection<User> findAll();

    User create(User user);

    User update(User newUser);

    HashMap<Long, User> getUsers();
}
