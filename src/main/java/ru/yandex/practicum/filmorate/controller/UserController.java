package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserService;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import javax.validation.constraints.Min;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/users")
public class UserController {

    UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public List<User> getUsers() {
        return userService.getUsers();
    }

    @GetMapping("/{id}")
    public User getUserById(@PathVariable @Min(0) int id) {
        return userService.getUserById(id);
    }

    @GetMapping("/{id}/friends")
    public List<User> getFriends(@PathVariable @Min(0) int id) {
        return userService.getFriends(id);
    }

    @GetMapping("/{id}/friends/common/{otherId}")
    public List<User> getCommonFriends(
            @PathVariable @Min(0) int id,
            @PathVariable @Min(0) int otherId) {
        return userService.getCommonFriends(id, otherId);
    }

    @PostMapping
    public User createUser(@Valid @RequestBody User user, HttpServletRequest request) {
        logRequest(request);
        return userService.createUser(user);
    }

    @PutMapping
    public User updateUser(@Valid @RequestBody User user, HttpServletRequest request) {
        logRequest(request);
        return userService.updateUser(user);
    }

    @PutMapping("/{id}/friends/{friendId}")
    public void addToFriends(@PathVariable @Min(0) int id, @PathVariable int friendId) {
        userService.addFriend(id, friendId);
    }

    @DeleteMapping("/{id}/friends/{friendId}")
    public void deleteFromFriends(@PathVariable @Min(0) int id, @PathVariable int friendId) {
        userService.deleteFriend(id, friendId);
    }

    private void logRequest(HttpServletRequest request) {
        log.debug("Получен запрос к эндпоинту: '{} {}', Строка параметров запроса: '{}'",
                request.getMethod(), request.getRequestURI(), request.getQueryString());
    }
}