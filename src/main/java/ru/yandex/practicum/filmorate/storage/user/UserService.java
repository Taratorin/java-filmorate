package ru.yandex.practicum.filmorate.storage.user;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class UserService {

    private final UserStorage userStorage;

    @Autowired
    public UserService(@Qualifier("userDbStorage") UserStorage userStorage) {
        this.userStorage = userStorage;
    }

    public void addFriend(int id, int friendId) {
        if (!isUserExists(id) && !isUserExists(friendId)) {
            throw new NotFoundException("Пользователь не существует.");
        }
        userStorage.checkAndUpdateFriends(id, friendId);
    }

    public void deleteFriend(int id1, int id2) {
        if (!isUserExists(id1) || !isUserExists(id2)) {
            throw new NotFoundException("Пользователь не существует.");
        }
        User u1 = getUserById(id1);
        User u2 = getUserById(id2);
        if (u1.getFriends().contains(u2.getId())) {
            getUserById(id1).deleteFriend(u2.getId());
            u2.deleteFriend(u1.getId());
        }
    }

    public List<User> getCommonFriends(int id1, int id2) {
        Set<Integer> set1 = getUserById(id1).getFriends();
        Set<Integer> set2 = getUserById(id2).getFriends();
        return set1.stream()
                .filter(set2::contains)
                .map(userStorage::getUserById)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }

    public User createUser(User user) {
        checkIfUserNamePresent(user);
        return userStorage.createUser(user);
    }

    public List<User> getUsers() {
        return userStorage.getUsers();
    }

    public User updateUser(User user) {
        getUserById(user.getId());
        checkIfUserNamePresent(user);
        return userStorage.updateUser(user);
    }

    public User getUserById(Integer id) {
        return userStorage.getUserById(id)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден."));
    }

    public List<User> getFriends(Integer id) {
        return userStorage.getFriends(id);
    }

    private Boolean isUserExists(Integer id) {
        return userStorage.getUserById(id).isPresent();
    }

    private void checkIfUserNamePresent(User user) {
        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }
    }
}