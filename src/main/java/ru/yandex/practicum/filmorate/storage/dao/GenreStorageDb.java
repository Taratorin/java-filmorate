package ru.yandex.practicum.filmorate.storage.dao;

import lombok.AllArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Genre;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

@Component
@AllArgsConstructor
public class GenreStorageDb {

    private final JdbcTemplate jdbcTemplate;

    public List<Genre> getGenreList() {
        String sql = "SELECT * FROM PUBLIC.GENRES;";
        return jdbcTemplate.query(sql, (rs, rowNum) -> makeGenre(rs));
    }

    public Optional<Genre> getGenreById(int id) {
        String sql = "SELECT GENRE_TITLE FROM PUBLIC.GENRES WHERE GENRE_ID = ?;";
        SqlRowSet set = jdbcTemplate.queryForRowSet(sql, id);
        if (set.next()) {
            Genre genre = new Genre(
                    id,
                    set.getString("GENRE_TITLE")
            );
            return Optional.of(genre);
        } else {
            return Optional.empty();
        }
    }

    private Genre makeGenre(ResultSet rs) throws SQLException {
        return new Genre(
                rs.getInt("GENRE_ID"),
                rs.getString("GENRE_TITLE")
        );
    }
}
