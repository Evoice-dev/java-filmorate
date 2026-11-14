package ru.yandex.practicum.filmorate.storage;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserDbStorage;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@JdbcTest
@AutoConfigureTestDatabase
@Import(UserDbStorage.class)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class UserDbStorageTest {
    private final UserDbStorage userStorage;

    private User newUser(String login) {
        return new User(login + "@mail.ru", "Имя " + login, login, "1990-05-15");
    }

    @Test
    void testAddAndFindUserById() {
        User created = userStorage.add(newUser("first"));

        Optional<User> userOptional = userStorage.getById(created.getId());

        assertThat(userOptional)
                .isPresent()
                .hasValueSatisfying(user -> {
                    assertThat(user).hasFieldOrPropertyWithValue("id", created.getId());
                    assertThat(user).hasFieldOrPropertyWithValue("login", "first");
                    assertThat(user).hasFieldOrPropertyWithValue("email", "first@mail.ru");
                    assertThat(user.getBirthday()).hasToString("1990-05-15");
                });
    }

    @Test
    void testFindUnknownUser() {
        assertThat(userStorage.getById(9999)).isEmpty();
    }

    @Test
    void testUpdateUser() {
        User created = userStorage.add(newUser("old"));
        User changed = new User("new@mail.ru", "Новое имя", "new", "2000-01-01");
        changed.setId(created.getId());

        User updated = userStorage.update(changed);

        assertThat(updated)
                .hasFieldOrPropertyWithValue("id", created.getId())
                .hasFieldOrPropertyWithValue("login", "new")
                .hasFieldOrPropertyWithValue("name", "Новое имя");
    }

    @Test
    void testUpdateUnknownUser() {
        User user = newUser("ghost");
        user.setId(9999);

        assertThatThrownBy(() -> userStorage.update(user)).isInstanceOf(NotFoundException.class);
    }

    @Test
    void testGetAll() {
        User a = userStorage.add(newUser("a"));
        User b = userStorage.add(newUser("b"));

        List<User> all = userStorage.getAll();

        assertThat(all).extracting(User::getId).contains(a.getId(), b.getId());
    }

    @Test
    void testDelete() {
        User user = userStorage.add(newUser("del"));

        userStorage.delete(user.getId());

        assertThat(userStorage.getById(user.getId())).isEmpty();
    }

    @Test
    void testFriendshipIsOneSided() {
        User a = userStorage.add(newUser("a"));
        User b = userStorage.add(newUser("b"));

        userStorage.addFriend(a.getId(), b.getId());

        assertThat(userStorage.getFriends(a.getId())).extracting(User::getId).containsExactly(b.getId());
        assertThat(userStorage.getFriends(b.getId())).isEmpty();
        assertThat(userStorage.getById(a.getId()).orElseThrow().getFriends()).containsExactly(b.getId());
    }

    @Test
    void testRemoveFriend() {
        User a = userStorage.add(newUser("a"));
        User b = userStorage.add(newUser("b"));
        userStorage.addFriend(a.getId(), b.getId());
        userStorage.addFriend(b.getId(), a.getId());

        userStorage.removeFriend(a.getId(), b.getId());

        assertThat(userStorage.getFriends(a.getId())).isEmpty();
        assertThat(userStorage.getFriends(b.getId())).extracting(User::getId).containsExactly(a.getId());
    }

    @Test
    void testCommonFriends() {
        User a = userStorage.add(newUser("a"));
        User b = userStorage.add(newUser("b"));
        User common = userStorage.add(newUser("c"));
        userStorage.addFriend(a.getId(), common.getId());
        userStorage.addFriend(a.getId(), b.getId());
        userStorage.addFriend(b.getId(), common.getId());

        List<User> result = userStorage.getCommonFriends(a.getId(), b.getId());

        assertThat(result).extracting(User::getId).containsExactly(common.getId());
    }
}
