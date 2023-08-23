package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/users")
public class UserController {

    private int id;
    private final Map<Integer, User> users = new HashMap<>();

    @GetMapping
    public List<User> getUsers() {
        return new ArrayList<>(users.values());
    }

    @PostMapping
    public User createUser(@Valid @RequestBody User user, HttpServletRequest request) {
        logRequest(request);
        checkIfUserNamePresent(user);
        newId();
        user.setId(id);
        users.put(user.getId(), user);
        log.debug("Добавлен новый пользователь: " + user);
        return user;
    }

    @PutMapping
    public User updateUser(@Valid @RequestBody User user, HttpServletRequest request) {
        logRequest(request);
        checkIfUserNamePresent(user);
        if (!users.containsKey(user.getId())) {
            throw new ValidationException("Некорректный id для обновления пользователя.");
        } else {
            users.put(user.getId(), user);
            log.debug("Пользователь обновлен: " + user);
            return user;
        }
    }

    private void newId() {
        ++id;
    }

    private void logRequest(HttpServletRequest request) {
        log.debug("Получен запрос к эндпоинту: '{} {}', Строка параметров запроса: '{}'",
                request.getMethod(), request.getRequestURI(), request.getQueryString());
    }

    private void checkIfUserNamePresent(User user) {
        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }
    }
}