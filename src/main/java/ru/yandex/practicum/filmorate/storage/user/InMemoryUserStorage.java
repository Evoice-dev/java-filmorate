package ru.yandex.practicum.filmorate.storage.user;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Component("inMemoryUserStorage")
public class InMemoryUserStorage implements UserStorage {
    private final Map<Integer, User> users = new LinkedHashMap<>();
    private int nextId = 1;

    @Override
    public User add(User user) {
        user.setId(nextId++);
        users.put(user.getId(), user);
        return user;
    }

    @Override
    public User update(User user) {
        User existing = users.get(user.getId());
        if (existing == null) {
            throw new NotFoundException("Такого пользователя не существует");
        }
        user.setFriends(existing.getFriends());
        users.put(user.getId(), user);
        return user;
    }

    @Override
    public Optional<User> getById(int id) {
        return Optional.ofNullable(users.get(id));
    }

    @Override
    public List<User> getAll() {
        return new ArrayList<>(users.values());
    }

    @Override
    public void delete(int id) {
        users.remove(id);
        users.values().forEach(u -> u.getFriends().remove(id));
    }

    @Override
    public void addFriend(int userId, int friendId) {
        users.get(userId).getFriends().add(friendId);
    }

    @Override
    public void removeFriend(int userId, int friendId) {
        users.get(userId).getFriends().remove(friendId);
    }

    @Override
    public List<User> getFriends(int userId) {
        return users.get(userId).getFriends().stream()
                .sorted()
                .map(users::get)
                .collect(Collectors.toList());
    }

    @Override
    public List<User> getCommonFriends(int userId, int otherId) {
        User other = users.get(otherId);
        return users.get(userId).getFriends().stream()
                .filter(other.getFriends()::contains)
                .sorted()
                .map(users::get)
                .collect(Collectors.toList());
    }
}
