package ru.yandex.practicum.filmorate.storage.user;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class InMemoryUserStorage implements UserStorage {

    private int id;
    private final Map<Integer, User> users = new HashMap<>();

    @Override
    public List<User> getUsers() {
        return new ArrayList<>(users.values());
    }

    @Override
    public HashMap<Integer, User> getUsersMap() {
        return new HashMap<>(users);
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

    @Override
    public User getUserById(Integer id) {
        User user = users.get(id);
        if (user == null) {
            throw new NotFoundException("Пользователь не найден.");
        }
        return user;
    }

    @Override
    public List<User> getFriends(Integer id) {
        User user = users.get(id);
        return user.getFriends().stream()
                .map(users::get)
                .collect(Collectors.toList());
    }

    private void newId() {
        ++id;
    }
}
