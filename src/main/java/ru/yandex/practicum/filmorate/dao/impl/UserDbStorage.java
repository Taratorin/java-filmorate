package ru.yandex.practicum.filmorate.dao.impl;

import lombok.AllArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.dao.UserStorage;
import ru.yandex.practicum.filmorate.model.User;

import java.sql.*;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Component
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
        KeyHolder keyHolder = new GeneratedKeyHolder();
        String sql = "INSERT INTO PUBLIC.USERS\n" +
                "(EMAIL, LOGIN, NAME, BIRTHDAY)\n" +
                "VALUES(?, ?, ?, ?);";
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, user.getEmail());
            ps.setString(2, user.getLogin());
            ps.setString(3, user.getName());
            ps.setDate(4, Date.valueOf(user.getBirthday()));
            return ps;
        }, keyHolder);
        int key = (int) keyHolder.getKey();
        user.setId(key);
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

    @Override
    public List<User> getCommonFriends(int id1, int id2) {
        String sql = "SELECT * FROM USERS u \n" +
                "WHERE USER_ID IN \n" +
                "(\n" +
                "SELECT *\n" +
                "FROM\n" +
                "(SELECT f.FRIEND_ID AS FRIENDS\n" +
                "FROM FRIENDS f\n" +
                "WHERE f.USER_ID = ?\n" +
                "UNION\n" +
                "SELECT f.USER_ID\n" +
                "FROM FRIENDS f \n" +
                "WHERE (f.FRIEND_ID = ? AND f.IF_APPROVED = true))\n" +
                "WHERE friends IN (\n" +
                "SELECT f.FRIEND_ID  AS FRIENDS\n" +
                "FROM FRIENDS f\n" +
                "WHERE f.USER_ID = ?\n" +
                "UNION\n" +
                "SELECT f.USER_ID\n" +
                "FROM FRIENDS f \n" +
                "WHERE (f.FRIEND_ID = ? AND f.IF_APPROVED = true))\n" +
                ");";
        return jdbcTemplate.query(sql, (rs, rowNum) -> makeUser(rs), id1, id1, id2, id2);
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
}