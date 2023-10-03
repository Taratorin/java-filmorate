package ru.yandex.practicum.filmorate.storage.dao;

import lombok.AllArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

@Component
@AllArgsConstructor
public class MpaStorageDb {

    private final JdbcTemplate jdbcTemplate;

    public Optional<Mpa> getMpaById(int id) {
        String sql = "SELECT RATING_NAME FROM PUBLIC.RATINGS WHERE RATING_ID = ?;";
        SqlRowSet set = jdbcTemplate.queryForRowSet(sql, id);
        if (set.next()) {
            String mpaName = set.getString("RATING_NAME");
            Mpa mpa = new Mpa(id, mpaName);
            return Optional.of(mpa);
        } else {
            return Optional.empty();
        }
    }

    public List<Mpa> getMpaList() {
        String sql = "SELECT * FROM PUBLIC.RATINGS;";
        return jdbcTemplate.query(sql, (rs, rowNum) -> makeMpa(rs));
    }

    private Mpa makeMpa(ResultSet rs) throws SQLException {
        int id = rs.getInt("RATING_ID");
        String name = rs.getString("RATING_NAME");
        return new Mpa(id, name);
    }
}