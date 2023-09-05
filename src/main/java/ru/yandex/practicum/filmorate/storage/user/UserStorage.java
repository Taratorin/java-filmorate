package ru.yandex.practicum.filmorate.storage.user;

import ru.yandex.practicum.filmorate.model.User;

import java.util.HashMap;
import java.util.List;

public interface UserStorage {
    List<User> getUsers();

    HashMap<Integer, User> getUsersMap();

    User createUser(User user);

    User updateUser(User user);

    void checkIfUserNamePresent(User user);

    User getUserById(Integer id);

    List<User> getFriends(Integer id);
}
