package ru.yandex.practicum.filmorate;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import ru.yandex.practicum.filmorate.dal.UserDbStorage;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class UserDbStorageTests {
    private final UserDbStorage userDbStorage;

    @BeforeEach
    public void clear() {
        userDbStorage.clear();
    }

    @Test
    public void testFindUserById_ShouldReturnUserId() {
        User testUser = new User();
        testUser.setLogin("testlogin");
        testUser.setName("TestUser");
        testUser.setEmail("test@example.com");
        testUser.setBirthday(LocalDate.of(1990, 1, 1));

        User createdUser = userDbStorage.createUser(testUser);
        Optional<User> userOptional = userDbStorage.findUserById(createdUser.getId());

        assertThat(userOptional)
                .isPresent()
                .hasValueSatisfying(user ->
                        assertThat(user).hasFieldOrPropertyWithValue("id", createdUser.getId())
                );
    }

    @Test
    public void testFindAllUsers_ShouldReturnExpectedTotalUsers() {
        User user1 = new User();
        user1.setLogin("user1");
        user1.setName("User One");
        user1.setEmail("user1@example.com");
        user1.setBirthday(LocalDate.of(1990, 1, 1));

        User user2 = new User();
        user2.setLogin("user2");
        user2.setName("User Two");
        user2.setEmail("user2@example.com");
        user2.setBirthday(LocalDate.of(1995, 5, 15));

        List<User> testUsers = List.of(user1, user2);
        testUsers.forEach(userDbStorage::createUser);

        Collection<User> users = userDbStorage.findAllUsers();
        assertThat(users)
                .isNotNull()
                .hasSize(2);

        List<User> userList = new ArrayList<>(users);
        assertThat(userList)
                .extracting("login")
                .containsExactlyInAnyOrder("user1", "user2");
    }

    @Test
    public void testUpdateUser_ShouldReturnExpectedName() {
        User user = new User();
        user.setLogin("user111");
        user.setName("User One");
        user.setEmail("user3@example.com");
        user.setBirthday(LocalDate.of(1990, 1, 1));
        Long userId = userDbStorage.createUser(user).getId();

        User newUser = new User();
        newUser.setId(userId);
        newUser.setLogin("user111");
        newUser.setName("User Two");
        newUser.setEmail("user3@example.com");
        newUser.setBirthday(LocalDate.of(1990, 1, 1));

        User updatedUser = userDbStorage.updateUser(newUser);
        assertThat(updatedUser)
                .hasFieldOrPropertyWithValue("name", "User Two");
    }

    @Test
    public void testAddAndDeleteFriend_ShouldReturnExpectedTotalFriends() {
        User user1 = new User();
        user1.setLogin("user111");
        user1.setName("User One");
        user1.setEmail("user3@example.com");
        user1.setBirthday(LocalDate.of(1990, 1, 1));
        Long user1Id = userDbStorage.createUser(user1).getId();

        User user2 = new User();
        user2.setLogin("user112");
        user2.setName("User Two");
        user2.setEmail("user5@example.com");
        user2.setBirthday(LocalDate.of(1990, 1, 1));
        Long user2Id = userDbStorage.createUser(user2).getId();

        userDbStorage.addFriend(user1Id, user2Id);

        List<User> user1Friends = new ArrayList<>(userDbStorage.getUserFriends(user1Id));
        List<User> user2Friends = new ArrayList<>(userDbStorage.getUserFriends(user2Id));

        assertThat(user1Friends).hasSize(1);
        assertThat(user2Friends).hasSize(0);

        userDbStorage.deleteFriend(user1Id, user2Id);
        user1Friends = new ArrayList<>(userDbStorage.getUserFriends(user1Id));
        assertThat(user1Friends).hasSize(0);
    }

    @Test
    public void testCommonFriend_ShouldReturnExpectedTotalCommonFriend() {
        User user1 = new User();
        user1.setLogin("user111");
        user1.setName("User One");
        user1.setEmail("user3@example.com");
        user1.setBirthday(LocalDate.of(1990, 1, 1));

        User user2 = new User();
        user2.setLogin("user112");
        user2.setName("User Two");
        user2.setEmail("user5@example.com");
        user2.setBirthday(LocalDate.of(1990, 1, 1));

        User user3 = new User();
        user3.setLogin("user113");
        user3.setName("User Three");
        user3.setEmail("user6@example.com");
        user3.setBirthday(LocalDate.of(1990, 1, 1));

        Long user1Id = userDbStorage.createUser(user1).getId();
        Long user2Id = userDbStorage.createUser(user2).getId();
        Long user3Id = userDbStorage.createUser(user3).getId();

        userDbStorage.addFriend(user1Id, user3Id);
        userDbStorage.addFriend(user2Id, user3Id);

        List<User> commonFriends = new ArrayList<>(userDbStorage.getCommonFriends(user1Id, user2Id));

        assertThat(commonFriends).hasSize(1);
        assertThat(commonFriends).extracting("login").containsExactlyInAnyOrder("user113");
    }
}
