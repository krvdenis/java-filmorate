package ru.yandex.practicum.filmorate.dal.storage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.DuplicateDataException;
import ru.yandex.practicum.filmorate.exception.InternalServerException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;

import java.sql.Date;
import java.util.Collection;
import java.util.Optional;
import java.util.Set;

@Component("userDbStorage")
@Slf4j
public class UserDbStorage extends BaseDbStorage<User> implements UserStorage {
    private static final String INSERT_CREATE_USER_QUERY = "INSERT INTO \"user\" (login, name, email, birthday)" +
            "VALUES (?, ?, ?, ?)";
    private static final String FIND_ALL_QUERY = "SELECT * FROM \"user\"";
    private static final String FIND_BY_ID_QUERY = "SELECT * FROM \"user\" WHERE id = ?";
    private static final String UPDATE_LOGIN_BY_ID_QUERY = "UPDATE \"user\" SET login = ? WHERE id = ?";
    private static final String UPDATE_NAME_BY_ID_QUERY = "UPDATE \"user\" SET name = ? WHERE id = ?";
    private static final String UPDATE_EMAIL_BY_ID_QUERY = "UPDATE \"user\" SET email = ? WHERE id = ?";
    private static final String UPDATE_BIRTHDAY_BY_ID_QUERY = "UPDATE \"user\" SET birthday = ? WHERE id = ?";
    private static final String INSERT_CREATE_FRIEND_QUERY = "INSERT INTO user_friend (user_id, friend_user_id) "
            + "VALUES (?, ?)";
    private static final String DELETE_FRIEND_QUERY = "DELETE FROM user_friend WHERE user_id = ? " +
            "AND friend_user_id = ?";
    private static final String FIND_ALL_FRIENDS_QUERY =
            "SELECT u.* " +
                    "FROM \"user\" AS u " +
                    "JOIN user_friend AS uf ON u.id = uf.friend_user_id " +
                    "WHERE uf.user_id = ?";
    private static final String FIND_COMMON_FRIENDS_QUERY =
            "SELECT u.* " +
                    "FROM \"user\" u " +
                    "WHERE u.id IN (" +
                    "    SELECT uf1.friend_user_id" +
                    "    FROM user_friend uf1" +
                    "    WHERE uf1.user_id = ?" +
                    ")" +
                    "AND u.id IN (" +
                    "    SELECT uf2.friend_user_id" +
                    "    FROM user_friend uf2" +
                    "    WHERE uf2.user_id = ?" +
                    ")";

    public UserDbStorage(JdbcTemplate jdbc, RowMapper<User> mapper) {
        super(jdbc, mapper);
    }

    @Override
    public User createUser(User user) {
        try {
            if (user.getName() == null || user.getName().isBlank()) {
                log.debug("Передано пустое поле с именем пользователя, вместо имени будет использован логин {}",
                        user.getLogin());
                user.setName(user.getLogin());
            }

            long id = insert(
                    INSERT_CREATE_USER_QUERY,
                    user.getLogin(),
                    user.getName(),
                    user.getEmail(),
                    Date.valueOf(user.getBirthday())
            );
            user.setId(id);
            return user;
        } catch (DuplicateKeyException e) {
            String errorMessage = e.getMessage().toLowerCase();
            if (errorMessage.contains("login")) {
                log.warn("Попытка регистрации с уже используемым логином: {}. Пользователь: {}", user.getLogin(), user);
                throw new DuplicateDataException("Этот логин уже используется");
            } else if (errorMessage.contains("email")) {
                log.warn("Попытка регистрации с уже используемым email: {}. Пользователь: {}", user.getEmail(), user);
                throw new DuplicateDataException("Эта электронная почта уже используется");
            }
            throw new InternalServerException("Неизвестная ошибка при создании пользователя");
        }
    }

    @Override
    public User updateUser(User newUser) {
        Optional<User> updateUserOptional = findOne(FIND_BY_ID_QUERY, newUser.getId());
        try {
            if (updateUserOptional.isEmpty()) {
                log.error("Пользователь под {} ID не найден.", newUser.getId()); // вынести бы в отдельный метод и засунуть в orElseThrow
            }
            User updateUser = updateUserOptional.orElseThrow(() ->
                    new NotFoundException("Пользователь с id = " + newUser.getId() + " не найден"));


            if (newUser.getName() != null) {
                update(UPDATE_NAME_BY_ID_QUERY, newUser.getName(), newUser.getId());
                log.info("Пользователь под ID {} сменил имя на: {}", newUser.getId(), newUser.getName());
            }

            if (newUser.getEmail() != null && !updateUser.getEmail().equals(newUser.getEmail())) {
                update(UPDATE_EMAIL_BY_ID_QUERY, newUser.getEmail(), newUser.getId());
                log.info("Пользователь под ID {} сменил email на: {}", newUser.getId(), newUser.getEmail());
            }

            if (newUser.getLogin() != null && !updateUser.getLogin().equals(newUser.getLogin())) {
                update(UPDATE_LOGIN_BY_ID_QUERY, newUser.getLogin(), newUser.getId());
                log.info("Пользователь под ID {} сменил логин на: {}", newUser.getId(), newUser.getLogin());
            }

            if (newUser.getBirthday() != null) {
                update(UPDATE_BIRTHDAY_BY_ID_QUERY, newUser.getBirthday(), newUser.getId());
                log.info("Пользователь под ID {} сменил дату рождения на: {}", newUser.getId(), newUser.getBirthday());
            }
            return findUserById(newUser.getId()).get();
        } catch (DuplicateKeyException e) {
            String errorMessage = e.getMessage().toLowerCase();
            if (errorMessage.contains("login")) {
                log.warn("Попытка изменить логин на уже используемое другим пользователем значение. Login: {}." +
                        " Пользователь: {}", newUser.getLogin(), newUser);
                throw new DuplicateDataException("Этот логин уже используется");
            } else if (errorMessage.contains("email")) {
                log.warn("Попытка изменить email на уже используемое другим пользователем значение. Email: {}." +
                        " Пользователь: {}", newUser.getEmail(), newUser);
                throw new DuplicateDataException("Эта электронная почта уже используется");
            }
            throw new InternalServerException("Неизвестная ошибка при обновлении данных пользователя");
        }
    }

    @Override
    public Collection<User> findAllUsers() {
        return findMany(FIND_ALL_QUERY);
    }

    @Override
    public Optional<User> findUserById(Long userId) {
        return findOne(FIND_BY_ID_QUERY, userId);
    }

    @Override
    public User addFriend(Long userId, Long newFriendId) {
        Optional<User> userOptional = findUserById(userId);

        if (userOptional.isEmpty()) {
            log.warn("Пользователь с ID {} не найден", userId);
            throw new NotFoundException("Пользователь с ID " + userId + " не найден!");
        }
        if (findUserById(newFriendId).isEmpty()) {
            log.warn("Пользователь, которого необходимо добавить в друзья, с ID {} не найден", newFriendId);
            throw new NotFoundException("Пользователь с ID " + newFriendId + " не найден!");
        }

        try {
            User user = userOptional.get();
            Set<Long> friends = user.getFriends(); //как будто бы нет смысла в этом, user локальная переменная
            insertWithoutGeneratedKey(INSERT_CREATE_FRIEND_QUERY, userId, newFriendId);
            friends.add(newFriendId);
            return user;
        } catch (DuplicateKeyException e) {
            log.warn("Пользователь с ID {} уже добавлен в список друзей", newFriendId);
            throw new DuplicateDataException("Этот друг уже добавлен");
        }
    }

    @Override
    public void deleteFriend(Long userId, Long friendId) {
        Optional<User> userOptional = findUserById(userId);

        if (userOptional.isEmpty()) {
            log.warn("Пользователь с ID {} не найден", userId);
            throw new NotFoundException("Пользователь с ID " + userId + " не найден!");
        }

        if (findUserById(friendId).isEmpty()) {
            log.warn("Пользователь с ID {} не найден", friendId);
            throw new NotFoundException("Пользователь с ID " + friendId + " не найден!");
        }

        delete(DELETE_FRIEND_QUERY, userId, friendId);
    }

    @Override
    public Collection<User> getUserFriends(Long userId) {
        Optional<User> userOptional = findUserById(userId);

        if (userOptional.isEmpty()) {
            log.warn("Пользователь с ID {} не найден", userId);
            throw new NotFoundException("Пользователь с ID " + userId + " не найден!");
        }
        return findMany(FIND_ALL_FRIENDS_QUERY, userId);
    }

    @Override
    public Collection<User> getCommonFriends(Long userId, Long otherId) {
        return findMany(FIND_COMMON_FRIENDS_QUERY, userId, otherId);
    }

}
