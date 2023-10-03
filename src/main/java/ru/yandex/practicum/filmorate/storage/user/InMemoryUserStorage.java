package ru.yandex.practicum.filmorate.storage.user;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.User;

import java.util.*;
import java.util.stream.Collectors;

@Component("inMemoryUserStorage")
public class InMemoryUserStorage implements UserStorage {

    private int id;
    private final Map<Integer, User> users = new HashMap<>();

    @Override
    public List<User> getUsers() {
        return new ArrayList<>(users.values());
    }

    @Override
    public User createUser(User user) {
        newId();
        user.setId(id);
        users.put(user.getId(), user);
        return user;
    }

    @Override
    public User updateUser(User user) {
        users.put(user.getId(), user);
        return user;

    }

    @Override
    public Optional<User> getUserById(int id) {
        return Optional.ofNullable(users.get(id));
    }

    @Override
    public List<User> getFriends(int id) {
        User user = users.get(id);
        return user.getFriends().stream()
                .map(users::get)
                .collect(Collectors.toList());
    }

    @Override
    public Boolean isUserPresent(int id) {
        return users.containsKey(id);
    }

    @Override
    public void checkAndUpdateFriends(int id, int friendId) {
        users.get(id).addFriend(friendId);
    }

    @Override
    public void deleteFriend(int id, int friendId) {
        users.get(id).deleteFriend(friendId);
    }

    private void newId() {
        ++id;
    }
}