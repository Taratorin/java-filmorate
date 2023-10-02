package ru.yandex.practicum.filmorate.storage.film;

import lombok.AllArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

@Component("filmDbStorage")
@AllArgsConstructor
public class FilmDbStorage implements FilmStorage {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public List<Film> getFilms() {
        String sql = "SELECT * FROM PUBLIC.FILMS;";
        return jdbcTemplate.query(sql, (rs, rowNum) -> makeFilm(rs));
    }

    @Override
    public Film createFilm(Film film) {
        film.setId(getId());
        String sql = "INSERT INTO PUBLIC.FILMS\n" +
                "(FILM_ID, TITLE, DESCRIPTION, RELEASE_DATE, DURATION, RATING_ID)\n" +
                "VALUES(?, ?, ?, ?, ?, ?);";
        jdbcTemplate.update(sql, film.getId(), film.getName(), film.getDescription(), film.getReleaseDate(),
                film.getDuration(), film.getMpa().getId());
        if (film.getGenres() != null) {
            updateFilmGenre(film);
        }
        return film;
    }

    @Override
    public Film updateFilm(Film film) {
        String sql = "UPDATE PUBLIC.FILMS SET " +
                "TITLE = ?, DESCRIPTION = ?, RELEASE_DATE = ?, DURATION = ?, RATING_ID = ?\n" +
                "WHERE FILM_ID = ?;";
        jdbcTemplate.update(sql, film.getName(), film.getDescription(), film.getReleaseDate(),
                film.getDuration(), film.getMpa().getId(), film.getId());
        if (film.getGenres() != null) {
            updateFilmGenre(film);
        }
        return film;
    }

    private void updateFilmGenre(Film film) {
        String sqlDelete = "DELETE FROM PUBLIC.FILM_GENRE WHERE FILM_ID = ?;";
        jdbcTemplate.update(sqlDelete, film.getId());
        String sql = "INSERT INTO PUBLIC.FILM_GENRE (FILM_ID, GENRE_ID) VALUES(?, ?);";
        for (Genre genre : film.getGenres()) {
            jdbcTemplate.update(sql, film.getId(), genre.getId());
        }
    }

    @Override
    public Optional<Film> getFilmById(Integer id) {
        String sql = "SELECT * FROM PUBLIC.FILMS WHERE FILM_ID = ?;";
        LinkedHashSet<Genre> genres = getGenres(id);
        SqlRowSet set = jdbcTemplate.queryForRowSet(sql, id);
        if (!set.next()) {
            return Optional.empty();
        } else {
            return Optional.of(Film.builder()
                    .id(set.getInt("FILM_ID"))
                    .name(set.getString("TITLE"))
                    .description(set.getString("DESCRIPTION"))
                    .releaseDate(Objects.requireNonNull(set.getDate("RELEASE_DATE")).toLocalDate())
                    .duration(set.getInt("DURATION"))
                    .mpa(makeMpa(set.getInt("RATING_ID")))
                    .genres(genres)
                    .build());
        }
    }

    @Override
    public Boolean isFilmPresent(Integer id) {
        String sql = "SELECT COUNT(FILM_ID) AS ID FROM PUBLIC.FILMS WHERE FILM_ID = ?;";
        SqlRowSet set = jdbcTemplate.queryForRowSet(sql, id);
        return set.next();
    }

    private int getId() {
        String sqlGetId = "SELECT COUNT(FILM_ID) AS ID FROM PUBLIC.FILMS;";
        SqlRowSet set = jdbcTemplate.queryForRowSet(sqlGetId);
        set.next();
        return set.getInt(1) + 1;
    }

    private Film makeFilm(ResultSet rs) throws SQLException {
        int id = rs.getInt("FILM_ID");
        LinkedHashSet<Genre> genres = getGenres(id);
        return Film.builder()
                .id(id)
                .name(rs.getString("TITLE"))
                .description(rs.getString("DESCRIPTION"))
                .releaseDate(rs.getDate("RELEASE_DATE").toLocalDate())
                .duration(rs.getInt("DURATION"))
                .mpa(makeMpa(rs.getInt("RATING_ID")))
                .genres(genres)
                .build();
    }

    private Mpa makeMpa(int id) {
        Mpa mpa = new Mpa();
        String sql = "SELECT RATING_NAME FROM PUBLIC.RATINGS WHERE RATING_ID = ?;";
        SqlRowSet set = jdbcTemplate.queryForRowSet(sql, id);
        set.next();
        String mpaName = set.getString("RATING_NAME");
        mpa.setId(id);
        mpa.setName(mpaName);
        return mpa;
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
}