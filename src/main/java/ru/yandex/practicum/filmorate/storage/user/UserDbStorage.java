package ru.yandex.practicum.filmorate.storage.user;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Repository("userDbStorage")
@RequiredArgsConstructor
public class UserDbStorage implements UserStorage {
    private static final String SELECT_USERS = "SELECT u.id, u.email, u.login, u.name, u.birthday FROM users u ";

    private static final RowMapper<User> MAPPER = (rs, rowNum) -> {
        User user = new User();
        user.setId(rs.getInt("id"));
        user.setEmail(rs.getString("email"));
        user.setLogin(rs.getString("login"));
        user.setName(rs.getString("name"));
        user.setBirthday(rs.getDate("birthday").toLocalDate());
        return user;
    };

    private final JdbcTemplate jdbc;

    @Override
    public User add(User user) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbc.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(
                    "INSERT INTO users (email, login, name, birthday) VALUES (?, ?, ?, ?)",
                    new String[]{"id"});
            ps.setString(1, user.getEmail());
            ps.setString(2, user.getLogin());
            ps.setString(3, user.getName());
            ps.setDate(4, Date.valueOf(user.getBirthday()));
            return ps;
        }, keyHolder);
        user.setId(Objects.requireNonNull(keyHolder.getKey()).intValue());
        return getById(user.getId()).orElseThrow();
    }

    @Override
    public User update(User user) {
        int updated = jdbc.update(
                "UPDATE users SET email = ?, login = ?, name = ?, birthday = ? WHERE id = ?",
                user.getEmail(), user.getLogin(), user.getName(), Date.valueOf(user.getBirthday()), user.getId());
        if (updated == 0) {
            throw new NotFoundException("Пользователь с id=" + user.getId() + " не найден");
        }
        return getById(user.getId()).orElseThrow();
    }

    @Override
    public Optional<User> getById(int id) {
        List<User> users = jdbc.query(SELECT_USERS + "WHERE u.id = ?", MAPPER, id);
        return fillFriends(users).stream().findFirst();
    }

    @Override
    public List<User> getAll() {
        return fillFriends(jdbc.query(SELECT_USERS + "ORDER BY u.id", MAPPER));
    }

    @Override
    public void delete(int id) {
        jdbc.update("DELETE FROM users WHERE id = ?", id);
    }

    @Override
    public void addFriend(int userId, int friendId) {
        boolean reverseExists = isFriend(friendId, userId);
        jdbc.update("MERGE INTO friendships (user_id, friend_id, confirmed) KEY (user_id, friend_id) VALUES (?, ?, ?)",
                userId, friendId, reverseExists);
        if (reverseExists) {
            jdbc.update("UPDATE friendships SET confirmed = TRUE WHERE user_id = ? AND friend_id = ?",
                    friendId, userId);
        }
    }

    @Override
    public void removeFriend(int userId, int friendId) {
        jdbc.update("DELETE FROM friendships WHERE user_id = ? AND friend_id = ?", userId, friendId);
        jdbc.update("UPDATE friendships SET confirmed = FALSE WHERE user_id = ? AND friend_id = ?",
                friendId, userId);
    }

    @Override
    public List<User> getFriends(int userId) {
        return fillFriends(jdbc.query(SELECT_USERS
                + "JOIN friendships f ON f.friend_id = u.id "
                + "WHERE f.user_id = ? ORDER BY u.id", MAPPER, userId));
    }

    @Override
    public List<User> getCommonFriends(int userId, int otherId) {
        return fillFriends(jdbc.query(SELECT_USERS
                + "JOIN friendships f1 ON f1.friend_id = u.id AND f1.user_id = ? "
                + "JOIN friendships f2 ON f2.friend_id = u.id AND f2.user_id = ? "
                + "ORDER BY u.id", MAPPER, userId, otherId));
    }

    private boolean isFriend(int userId, int friendId) {
        Integer count = jdbc.queryForObject(
                "SELECT COUNT(*) FROM friendships WHERE user_id = ? AND friend_id = ?",
                Integer.class, userId, friendId);
        return count != null && count > 0;
    }

    private List<User> fillFriends(List<User> users) {
        if (users.isEmpty()) {
            return users;
        }
        Map<Integer, User> byId = users.stream()
                .collect(Collectors.toMap(User::getId, Function.identity(), (a, b) -> a, LinkedHashMap::new));
        String placeholders = String.join(",", Collections.nCopies(byId.size(), "?"));
        jdbc.query("SELECT user_id, friend_id FROM friendships WHERE user_id IN (" + placeholders + ")",
                rs -> {
                    byId.get(rs.getInt("user_id")).getFriends().add(rs.getInt("friend_id"));
                },
                byId.keySet().toArray());
        return users;
    }
}
