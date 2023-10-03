package ru.yandex.practicum.filmorate.storage.dao;

import lombok.AllArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Component("filmDbStorage")
@AllArgsConstructor
public class FilmDbStorage implements FilmStorage {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public List<Optional<Film>> getFilms() {
        String sql = "SELECT * FROM PUBLIC.FILMS;";
        return jdbcTemplate.query(sql, (rs, rowNum) -> makeFilm(rs));
    }

    @Override
    public Film createFilm(Film film) {
        film.setId(getId());
        int mpaId = film.getMpa().getId();
        String sql = "INSERT INTO PUBLIC.FILMS\n" +
                "(FILM_ID, TITLE, DESCRIPTION, RELEASE_DATE, DURATION, RATING_ID)\n" +
                "VALUES(?, ?, ?, ?, ?, ?);";
        jdbcTemplate.update(sql, film.getId(), film.getName(), film.getDescription(), film.getReleaseDate(),
                film.getDuration(), mpaId);
        film.setMpa(makeMpa(mpaId));
        film.setLikes(getLikesSet(film.getId()));
        if (film.getGenres() != null) {
            updateFilmGenre(film);
        }
        return film;
    }

    @Override
    public Film updateFilm(Film film) {
        int mpaId = film.getMpa().getId();
        String sql = "UPDATE PUBLIC.FILMS SET " +
                "TITLE = ?, DESCRIPTION = ?, RELEASE_DATE = ?, DURATION = ?, RATING_ID = ?\n" +
                "WHERE FILM_ID = ?;";
        jdbcTemplate.update(sql, film.getName(), film.getDescription(), film.getReleaseDate(),
                film.getDuration(), mpaId, film.getId());
        film.setMpa(makeMpa(mpaId));
        film.setLikes(getLikesSet(film.getId()));
        if (film.getGenres() != null) {
            updateFilmGenre(film);
        }
        return film;
    }

    @Override
    public Optional<Film> getFilmById(int id) {
        String sql = "SELECT * FROM PUBLIC.FILMS WHERE FILM_ID = ?;";
        List<Optional<Film>> query = jdbcTemplate.query(sql, (rs, rowNum) -> makeFilm(rs), id);
        if (!query.isEmpty()) {
            return query.get(0);
        } else {
            return Optional.empty();
        }
    }

    @Override
    public boolean isFilmPresent(int id) {
        String sql = "SELECT FILM_ID FROM PUBLIC.FILMS WHERE FILM_ID = ?;";
        SqlRowSet set = jdbcTemplate.queryForRowSet(sql, id);
        return set.next();
    }

    @Override
    public void addLikeToFilm(int id, int userId) {
        String sql = "SELECT * FROM PUBLIC.LIKES WHERE FILM_ID = ? AND USER_ID = ?;";
        SqlRowSet set = jdbcTemplate.queryForRowSet(sql, id, userId);
        if (!set.next()) {
            sql = "INSERT INTO PUBLIC.LIKES (FILM_ID, USER_ID) VALUES(?, ?)";
            jdbcTemplate.update(sql, id, userId);
        }
    }

    @Override
    public void deleteLike(int id, int userId) {
        String sql = "SELECT * FROM PUBLIC.LIKES WHERE FILM_ID = ? AND USER_ID = ?;";
        SqlRowSet set = jdbcTemplate.queryForRowSet(sql, id, userId);
        if (set.next()) {
            sql = "DELETE FROM PUBLIC.LIKES WHERE FILM_ID = ? AND USER_ID = ?;";
            jdbcTemplate.update(sql, id, userId);
        }
    }

    private void updateFilmGenre(Film film) {
        String sqlDelete = "DELETE FROM PUBLIC.FILM_GENRE WHERE FILM_ID = ?;";
        jdbcTemplate.update(sqlDelete, film.getId());
        String sql = "INSERT INTO PUBLIC.FILM_GENRE (FILM_ID, GENRE_ID) VALUES(?, ?);";
        for (Genre genre : film.getGenres()) {
            jdbcTemplate.update(sql, film.getId(), genre.getId());
        }
    }

    private int getId() {
        String sqlGetId = "SELECT COUNT(FILM_ID) AS ID FROM PUBLIC.FILMS;";
        SqlRowSet set = jdbcTemplate.queryForRowSet(sqlGetId);
        set.next();
        return set.getInt(1) + 1;
    }

    private Optional<Film> makeFilm(ResultSet rs) {
        try {
            int id = rs.getInt("FILM_ID");
            LinkedHashSet<Genre> genres = getGenres(id);
            Film film = Film.builder()
                    .id(id)
                    .name(rs.getString("TITLE"))
                    .description(rs.getString("DESCRIPTION"))
                    .releaseDate(rs.getDate("RELEASE_DATE").toLocalDate())
                    .duration(rs.getInt("DURATION"))
                    .mpa(makeMpa(rs.getInt("RATING_ID")))
                    .genres(genres)
                    .build();
            film.setLikes(getLikesSet(id));
            return Optional.of(film);
        } catch (SQLException e) {
            return Optional.empty();
        }
    }

    private Mpa makeMpa(int id) {
        String sql = "SELECT RATING_NAME FROM PUBLIC.RATINGS WHERE RATING_ID = ?;";
        SqlRowSet set = jdbcTemplate.queryForRowSet(sql, id);
        set.next();
        String mpaName = set.getString("RATING_NAME");
        return new Mpa(id, mpaName);
    }

    private Genre makeGenre(ResultSet rs) throws SQLException {
        return new Genre(
                rs.getInt("GENRE_ID"),
                rs.getString("GENRE_TITLE")
        );
    }

    private LinkedHashSet<Genre> getGenres(int id) {
        String sql = "SELECT g.GENRE_ID, g.GENRE_TITLE " +
                "FROM FILM_GENRE fg JOIN GENRES g ON fg.GENRE_ID = g.GENRE_ID " +
                "WHERE fg.FILM_ID = " + id;
        List<Genre> query = jdbcTemplate.query(sql, (rs, rowNum) -> makeGenre(rs));
        return new LinkedHashSet<>(query);
    }

    private int getLikesById(ResultSet rs) throws SQLException {
        return rs.getInt("USER_ID");
    }

    private Set<Integer> getLikesSet(int id) {
        String sql = "SELECT USER_ID FROM PUBLIC.LIKES WHERE FILM_ID = ?;";
        List<Integer> query;
        query = jdbcTemplate.query(sql, (rs, rowNum) -> getLikesById(rs), id);
        return new LinkedHashSet<>(query);
    }
}