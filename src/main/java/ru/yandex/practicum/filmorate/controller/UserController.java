package ru.yandex.practicum.filmorate.controller;

import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.User;

import java.util.ArrayList;

import static ru.yandex.practicum.filmorate.FilmorateApplication.log;


@RestController
@RequestMapping("/users")
public class UserController {
    private final ArrayList<User> allUsers = new ArrayList<>();

    @PostMapping()
    public User add(@RequestBody User user) {
        log.info("Попытка добавить пользователя");
        user.id = allUsers.size() + 1;
        allUsers.add(user);
        log.info("Пользователь успешно добавлен");
        return user;
    }

    @PutMapping()
    public User update(@RequestBody User user) {
        log.info("Попытка редактирования пользователя");
        if (allUsers.contains(user)) {
            allUsers.set(allUsers.indexOf(user), user);
        }
        log.info("Пользователь успешно отредактирован");
        return user;
    }

    @GetMapping()
    public ArrayList<User> getAll() {
        log.info("Произошел запрос всех пользователей");
        return allUsers;
    }
}
