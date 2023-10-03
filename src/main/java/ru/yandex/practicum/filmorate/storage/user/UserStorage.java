package ru.yandex.practicum.filmorate.storage.user;

import ru.yandex.practicum.filmorate.model.User;

import java.util.List;
import java.util.Optional;

public interface UserStorage {
    List<User> getUsers();

    User createUser(User user);

    User updateUser(User user);

    Optional<User> getUserById(int id);

    List<User> getFriends(int id);

    Boolean isUserPresent(int id);

    void checkAndUpdateFriends(int id, int friendId);

    void deleteFriend(int id, int friendId);
}
