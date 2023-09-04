package ru.yandex.practicum.filmorate.storage.user;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class InMemoryUserStorage implements UserStorage {

    private int id;
    private final Map<Integer, User> users = new HashMap<>();

    @Override
    public List<User> getUsers() {
        return new ArrayList<>(users.values());
    }

    @Override
    public User createUser(User user) {
        checkIfUserNamePresent(user);
        newId();
        user.setId(id);
        users.put(user.getId(), user);
        return user;
    }

    @Override
    public User updateUser(User user) {
        checkIfUserNamePresent(user);
        if (!users.containsKey(user.getId())) {
            throw new ValidationException("Некорректный id для обновления пользователя.");
        } else {
            users.put(user.getId(), user);
            return user;
        }
    }

    @Override
    public void checkIfUserNamePresent(User user) {
        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }
    }

    private void newId() {
        ++id;
    }
}
