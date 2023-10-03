package ru.yandex.practicum.filmorate.storage.dao;

import lombok.AllArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Component("userDbStorage")
@AllArgsConstructor
public class UserDbStorage implements UserStorage {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public List<User> getUsers() {
        String sql = "SELECT * FROM PUBLIC.USERS;";
        return jdbcTemplate.query(sql, (rs, rowNum) -> makeUser(rs));
    }

    @Override
    public User createUser(User user) {
        user.setId(getId());
        String sql = "INSERT INTO PUBLIC.USERS\n" +
                "(USER_ID, EMAIL, LOGIN, NAME, BIRTHDAY)\n" +
                "VALUES(?, ?, ?, ?, ?);";
        jdbcTemplate.update(sql, user.getId(), user.getEmail(), user.getLogin(), user.getName(), user.getBirthday());
        return user;
    }

    @Override
    public User updateUser(User user) {
        String sql = "UPDATE PUBLIC.USERS SET EMAIL = ?, LOGIN = ?, NAME = ?, BIRTHDAY = ? WHERE USER_ID = ?;";
        jdbcTemplate.update(sql, user.getEmail(), user.getLogin(), user.getName(), user.getBirthday(), user.getId());
        return user;
    }

    @Override
    public Optional<User> getUserById(int id) {
        String sql = "SELECT * FROM PUBLIC.USERS WHERE USER_ID = ?;";
        SqlRowSet set = jdbcTemplate.queryForRowSet(sql, id);
        if (!set.next()) {
            return Optional.empty();
        } else {
            return Optional.of(User.builder()
                    .id(set.getInt("USER_ID"))
                    .email(set.getString("EMAIL"))
                    .login(set.getString("LOGIN"))
                    .name(set.getString("NAME"))
                    .birthday(Objects.requireNonNull(set.getDate("BIRTHDAY")).toLocalDate())
                    .build());
        }
    }

    @Override
    public List<User> getFriends(int id) {
        String sql = "SELECT u.USER_ID, u.EMAIL, u.LOGIN, u.NAME, u.BIRTHDAY FROM FRIENDS f " +
                "JOIN USERS u ON f.FRIEND_ID = u.USER_ID WHERE f.USER_ID = ?\n" +
                "UNION\n" +
                "SELECT u.USER_ID, u.EMAIL, u.LOGIN, u.NAME, u.BIRTHDAY FROM FRIENDS f " +
                "JOIN USERS u ON f.USER_ID = u.USER_ID WHERE (f.FRIEND_ID = ? AND f.IF_APPROVED = true);";
        return jdbcTemplate.query(sql, (rs, rowNum) -> makeUser(rs), id, id);
    }

    @Override
    public Boolean isUserPresent(int id) {
        String sql = "SELECT USER_ID FROM PUBLIC.USERS WHERE USER_ID = ?;";
        SqlRowSet set = jdbcTemplate.queryForRowSet(sql, id);
        return set.next();
    }

    @Override
    public void checkAndUpdateFriends(int id, int friendId) {
        String sql = "SELECT * FROM PUBLIC.FRIENDS WHERE USER_ID = ? AND FRIEND_ID = ?;";
        SqlRowSet setDirect = jdbcTemplate.queryForRowSet(sql, id, friendId);
        SqlRowSet setReversed = jdbcTemplate.queryForRowSet(sql, friendId, id);
        boolean isDirectNotEmpty = setDirect.next();
        boolean isReversedNotEmpty = setReversed.next();
        if (!isDirectNotEmpty && !isReversedNotEmpty) {
            String sqlInsert = "INSERT INTO PUBLIC.FRIENDS (USER_ID, FRIEND_ID, IF_APPROVED) VALUES (?, ?, ?);";
            jdbcTemplate.update(sqlInsert, id, friendId, false);
        } else if (isReversedNotEmpty) {
            String sqlUpdate = "UPDATE PUBLIC.FRIENDS SET FRIEND_ID = ?, IF_APPROVED = true WHERE USER_ID = ?;";
            jdbcTemplate.update(sqlUpdate, id, friendId);
        }
    }

    @Override
    public void deleteFriend(int id, int friendId) {
        String sql = "SELECT * FROM PUBLIC.FRIENDS WHERE USER_ID = ? AND FRIEND_ID = ?;";
        SqlRowSet setDirect = jdbcTemplate.queryForRowSet(sql, id, friendId);
        boolean isDirectNotEmpty = setDirect.next();
        if (isDirectNotEmpty) {
            boolean ifApproved = setDirect.getBoolean("IF_APPROVED");
            String sqlDelete = "DELETE FROM PUBLIC.FRIENDS WHERE USER_ID = ? AND FRIEND_ID = ?;";
            if (!ifApproved) {
                jdbcTemplate.update(sqlDelete, id, friendId);
            } else {
                jdbcTemplate.update(sqlDelete, id, friendId);
                String sqlUpdate = "INSERT INTO PUBLIC.FRIENDS (USER_ID, FRIEND_ID, IF_APPROVED) VALUES (?, ?, ?);";
                jdbcTemplate.update(sqlUpdate, friendId, id, false);
            }
        } else {
            SqlRowSet setReversed = jdbcTemplate.queryForRowSet(sql, friendId, id);
            boolean isReversedNotEmpty = setReversed.next();
            if (isReversedNotEmpty) {
                boolean ifApproved = setReversed.getBoolean("IF_APPROVED");
                if (ifApproved) {
                    String sqlUpdate = "INSERT INTO PUBLIC.FRIENDS (USER_ID, FRIEND_ID, IF_APPROVED) VALUES (?, ?, ?);";
                    jdbcTemplate.update(sqlUpdate, friendId, id, false);
                }
            }
        }
    }

    private User makeUser(ResultSet rs) throws SQLException {
        return User.builder()
                .id(rs.getInt("USER_ID"))
                .email(rs.getString("EMAIL"))
                .login(rs.getString("LOGIN"))
                .name(rs.getString("NAME"))
                .birthday(rs.getDate("BIRTHDAY").toLocalDate())
                .build();
    }

    private int getId() {
        String sqlGetId = "SELECT COUNT(USER_ID) AS ID FROM PUBLIC.USERS;";
        SqlRowSet set = jdbcTemplate.queryForRowSet(sqlGetId);
        set.next();
        return set.getInt(1) + 1;
    }
}