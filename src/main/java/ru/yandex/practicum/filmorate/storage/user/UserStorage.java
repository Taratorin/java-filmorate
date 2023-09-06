package ru.yandex.practicum.filmorate.storage.user;

import ru.yandex.practicum.filmorate.model.User;

import java.util.List;
import java.util.Optional;

public interface UserStorage {
    List<User> getUsers();

    User createUser(User user);

    User updateUser(User user);

    Optional<User> getUserById(Integer id);

    List<User> getFriends(Integer id);
}
