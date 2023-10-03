package ru.yandex.practicum.filmorate.storage.user;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;

import java.util.List;
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

    public void deleteFriend(int id, int friendId) {
        User u = getUserById(id);
        User friend = getUserById(friendId);
        List<User> friends = getFriends(id);
        if (friends.contains(friend)) {
            userStorage.deleteFriend(id, friendId);
        }
    }

    public List<User> getCommonFriends(int id1, int id2) {
        List<User> list1 = getFriends(id1);
        List<User> list2 = getFriends(id2);
        return list1.stream()
                .filter(list2::contains)
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