package ru.yandex.practicum.filmorate.service;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Validated
@RequiredArgsConstructor
public class UserService {
    private final UserStorage userStorage;

    public User addUser(@Valid User user) {
        return userStorage.add(user);
    }

    public User updateUser(@Valid User user) {
        User existing = getUserById(user.getId());
        user.setFriends(existing.getFriends());
        return userStorage.update(user);
    }

    public List<User> getAllUsers() {
        return userStorage.getAll();
    }

    public User getUserById(int id) {
        return userStorage.getById(id)
                .orElseThrow(() -> new NotFoundException("Пользователь с id=" + id + " не найден"));
    }

    public void addFriend(int userId, int friendId) {
        User user = getUserById(userId);
        User friend = getUserById(friendId);

        user.getFriends().add((long) friendId);
        friend.getFriends().add((long) userId);
    }

    public void deleteFriend(int userId, int friendId) {
        User user = getUserById(userId);
        User friend = getUserById(friendId);

        user.getFriends().remove((long) friendId);
        friend.getFriends().remove((long) userId);
    }

    public List<User> getFriends(int userId) {
        User user = getUserById(userId);
        return user.getFriends().stream()
                .map(id -> userStorage.getById(id.intValue()))
                .flatMap(java.util.Optional::stream)
                .collect(Collectors.toList());
    }

    public List<User> getCommonFriends(int userId, int otherId) {
        User user = getUserById(userId);
        User other = getUserById(otherId);

        Set<Long> common = new HashSet<>(user.getFriends());
        common.retainAll(other.getFriends());

        return common.stream()
                .map(id -> userStorage.getById(id.intValue()))
                .flatMap(java.util.Optional::stream)
                .collect(Collectors.toList());
    }
}