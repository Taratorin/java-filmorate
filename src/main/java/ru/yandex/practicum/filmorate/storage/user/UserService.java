package ru.yandex.practicum.filmorate.storage.user;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.User;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class UserService {

    UserStorage userStorage;

    @Autowired
    public UserService(InMemoryUserStorage userStorage) {
        this.userStorage = userStorage;
    }

    //todo : use id's
    public void addFriend(Integer id1, Integer id2) {
        if (isUserExists(id1) && isUserExists(id2)) {
            getUserById(id1).addFriend(getUserById(id2).getId());
            getUserById(id2).addFriend(getUserById(id1).getId());
        }
    }

    public void deleteFriend(Integer id1, Integer id2) {
        if (isUserExists(id1) && isUserExists(id2)) {
            User u1 = getUserById(id1);
            User u2 = getUserById(id2);
            if (u1.getFriends().contains(u2.getId())) {
                getUserById(id1).deleteFriend(u2.getId());
                u2.deleteFriend(u1.getId());
            }
        }
    }

    public List<Integer> getCommonFriends(Integer id1, Integer id2) {
        Set<Integer> set1 = getUserById(id1).getFriends();
        Set<Integer> set2 = getUserById(id2).getFriends();
        return set1.stream()
                .filter(set2::contains)
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

    private Boolean isUserExists(Integer id) {
        return userStorage.getUsers().contains(id);
    }

    private User getUserById(Integer id) {
        return userStorage.getUsers().get(id);
    }
}