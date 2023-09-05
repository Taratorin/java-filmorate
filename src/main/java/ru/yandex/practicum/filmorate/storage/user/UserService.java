package ru.yandex.practicum.filmorate.storage.user;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class UserService {

    private final UserStorage userStorage;

    @Autowired
    public UserService(InMemoryUserStorage userStorage) {
        this.userStorage = userStorage;
    }

    public void addFriend(Integer id1, Integer id2) {
        if (!isUserExists(id1) && !isUserExists(id2)) {
            throw new NotFoundException("Пользователь не существует.");
        }
        getUserById(id1).addFriend(getUserById(id2).getId());
        getUserById(id2).addFriend(getUserById(id1).getId());
    }

    public void deleteFriend(Integer id1, Integer id2) {
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

    public List<User> getCommonFriends(Integer id1, Integer id2) {
        Set<Integer> set1 = getUserById(id1).getFriends();
        Set<Integer> set2 = getUserById(id2).getFriends();
        return set1.stream()
                .filter(set2::contains)
                .map(userStorage.getUsersMap()::get)
                .collect(Collectors.toList());
    }

    public User createUser(User user) {
        return userStorage.createUser(user);
    }

    public List<User> getUsers() {
        return userStorage.getUsers();
    }

    public User updateUser(User user) {
        return userStorage.updateUser(user);
    }

    public User getUserById(Integer id) {
        return userStorage.getUserById(id);
    }

    public List<User> getFriends(Integer id) {
        return userStorage.getFriends(id);
    }

    private Boolean isUserExists(Integer id) {
        return userStorage.getUsersMap().containsKey(id);
    }
}