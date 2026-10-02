package ru.yandex.practicum.filmorate.storage.user;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static ru.yandex.practicum.filmorate.FilmorateApplication.log;

@Component
public class InMemoryUserStorage implements UserStorage {
    private final Map<Integer, User> users = new LinkedHashMap<>();
    private int nextId = 1;

    @Override
    public User add(User user) {
        log.info("Попытка добавить пользователя");
        user.setId(nextId++);
        users.put(user.getId(), user);
        log.info("Пользователь успешно добавлен");
        return user;
    }

    @Override
    public User update(User user) {
        log.info("Попытка редактирования пользователя");
        if (!users.containsKey(user.getId())) {
            throw new NotFoundException("Такого пользователя не существует");
        }
        users.put(user.getId(), user);
        log.info("Пользователь успешно отредактирован");
        return user;
    }

    @Override
    public User getById(int id) {
        return users.get(id);
    }

    @Override
    public List<User> getAll() {
        log.info("Произошел запрос всех пользователей");
        return new ArrayList<>(users.values());
    }

    @Override
    public void delete(int id) {
        users.remove(id);
    }
}