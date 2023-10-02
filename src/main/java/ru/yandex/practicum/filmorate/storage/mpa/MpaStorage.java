package ru.yandex.practicum.filmorate.storage.mpa;

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
public class MpaStorage {

    private final JdbcTemplate jdbcTemplate;

    //todo вынести в статическую переменную?

    public Optional<Mpa> getMpaById(int id) {
        String sql = "SELECT RATING_NAME FROM PUBLIC.RATINGS WHERE RATING_ID = ?;";
        SqlRowSet set = jdbcTemplate.queryForRowSet(sql, id);
        if (set.next()) {
            Mpa mpa = new Mpa();
            String mpaName = set.getString("RATING_NAME");
            mpa.setId(id);
            mpa.setName(mpaName);
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
        Mpa mpa = new Mpa();
        mpa.setId(rs.getInt("RATING_ID"));
        mpa.setName(rs.getString("RATING_NAME"));
        return mpa;
    }
}
