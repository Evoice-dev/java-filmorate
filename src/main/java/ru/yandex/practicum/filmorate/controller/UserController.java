package ru.yandex.practicum.filmorate.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.UserService;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @PostMapping
    public User addUser(@RequestBody User user) {
        log.info("POST /users — запрос на добавление пользователя: {}", user);
        User created = userService.addUser(user);
        log.info("POST /users — пользователь добавлен с id={}: {}", created.getId(), created);
        return created;
    }

    @PutMapping
    public User updateUser(@RequestBody User user) {
        log.info("PUT /users — запрос на обновление пользователя: {}", user);
        User updated = userService.updateUser(user);
        log.info("PUT /users — пользователь обновлён: {}", updated);
        return updated;
    }

    @GetMapping
    public List<User> getAllUsers() {
        log.info("GET /users — запрос списка всех пользователей");
        List<User> users = userService.getAllUsers();
        log.info("GET /users — возвращено пользователей: {}", users.size());
        return users;
    }

    @GetMapping("/{id}")
    public User getUser(@PathVariable int id) {
        log.info("GET /users/{} — запрос пользователя по id", id);
        User user = userService.getUserById(id);
        log.info("GET /users/{} — пользователь найден: {}", id, user);
        return user;
    }

    @PutMapping("/{id}/friends/{friendId}")
    public void addFriend(@PathVariable int id, @PathVariable int friendId) {
        log.info("PUT /users/{}/friends/{} — добавление в друзья", id, friendId);
        userService.addFriend(id, friendId);
        log.info("PUT /users/{}/friends/{} — пользователи теперь друзья", id, friendId);
    }

    @DeleteMapping("/{id}/friends/{friendId}")
    public void deleteFriend(@PathVariable int id, @PathVariable int friendId) {
        log.info("DELETE /users/{}/friends/{} — удаление из друзей", id, friendId);
        userService.deleteFriend(id, friendId);
        log.info("DELETE /users/{}/friends/{} — пользователи больше не друзья", id, friendId);
    }

    @GetMapping("/{id}/friends")
    public List<User> getFriends(@PathVariable int id) {
        log.info("GET /users/{}/friends — запрос друзей", id);
        List<User> friends = userService.getFriends(id);
        log.info("GET /users/{}/friends — возвращено друзей: {}", id, friends.size());
        return friends;
    }

    @GetMapping("/{id}/friends/common/{otherId}")
    public List<User> getCommonFriends(@PathVariable int id, @PathVariable int otherId) {
        log.info("GET /users/{}/friends/common/{} — запрос общих друзей", id, otherId);
        List<User> common = userService.getCommonFriends(id, otherId);
        log.info("GET /users/{}/friends/common/{} — возвращено общих друзей: {}", id, otherId, common.size());
        return common;
    }
}