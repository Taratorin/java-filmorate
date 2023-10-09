package ru.yandex.practicum.filmorate.dao.impl;

import lombok.AllArgsConstructor;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.dao.FilmStorage;
import ru.yandex.practicum.filmorate.dao.GenreStorage;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

@Component
@AllArgsConstructor
public class FilmDbStorage implements FilmStorage {

    private final JdbcTemplate jdbcTemplate;
    private final GenreStorage genreStorage;

    @Override
    public List<Film> getFilms() {
        String sql = "SELECT * FROM PUBLIC.FILMS f JOIN RATINGS r ON f.RATING_ID = r.RATING_ID";
        List<Film> films = jdbcTemplate.query(sql, (rs, rowNum) -> makeFilm(rs));
        genreStorage.attachGenreToFilm(films);
        return films;
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
        if (film.getGenres() != null) {
            updateFilmGenre(film);
            Set<Genre> genres = new HashSet<>(film.getGenres());
            film.setGenres(new LinkedHashSet<>());
            for (Genre genre : genres) {
                Optional<Genre> genreById = genreStorage.getGenreById(genre.getId());
                genreById.ifPresent(film::addGenre);
            }
        } else {
            film.setGenres(new LinkedHashSet<>());
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
        updateFilmGenre(film);
        genreStorage.attachGenreToFilm(List.of(film));

        return film;
    }

    @Override
    public Film getFilmById(int id) {
        String sql = "SELECT * FROM PUBLIC.FILMS f JOIN RATINGS r ON f.RATING_ID = r.RATING_ID WHERE FILM_ID = ?";
        List<Film> films = jdbcTemplate.query(sql, (rs, rowNum) -> makeFilm(rs), id);
        if (!films.isEmpty()) {
            Film film = films.get(0);
            genreStorage.attachGenreToFilm(List.of(film));
            return film;
        } else {
            throw new NotFoundException("Фильм с id = " + id + " не найден.");
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

    @Override
    public List<Film> getPopularFilms(int count) {
        String sql = "SELECT * FROM PUBLIC.FILMS f\n" +
                "JOIN RATINGS r ON f.RATING_ID = r.RATING_ID\n" +
                "WHERE FILM_ID IN (SELECT FILM_ID FROM PUBLIC.LIKES GROUP BY FILM_ID ORDER BY COUNT(USER_ID) DESC);";
        List<Film> films = jdbcTemplate.query(sql, (rs, rowNum) -> makeFilm(rs));
        if (!films.isEmpty()) {
            genreStorage.attachGenreToFilm(films);
            return films;
        } else {
            return getFilms();
        }
    }

    private void updateFilmGenre(Film film) {
        if (film.getGenres() != null) {
            String sqlDelete = "DELETE FROM PUBLIC.FILM_GENRE WHERE FILM_ID = ?;";
            jdbcTemplate.update(sqlDelete, film.getId());
            Set<Genre> genreSet = new HashSet<>(film.getGenres());
            ArrayList<Genre> genres = new ArrayList<>(genreSet);
            jdbcTemplate.batchUpdate(
                    "INSERT INTO PUBLIC.FILM_GENRE (FILM_ID, GENRE_ID) VALUES(?, ?)",
                    new BatchPreparedStatementSetter() {
                        public void setValues(PreparedStatement ps, int i)
                                throws SQLException {
                            ps.setInt(1, film.getId());
                            ps.setInt(2, genres.get(i).getId());
                        }

                        public int getBatchSize() {
                            return genreSet.size();
                        }
                    });
        }
    }

    private int getId() {
        String sqlGetId = "SELECT COUNT(FILM_ID) AS ID FROM PUBLIC.FILMS;";
        SqlRowSet set = jdbcTemplate.queryForRowSet(sqlGetId);
        set.next();
        return set.getInt(1) + 1;
    }

    private Film makeFilm(ResultSet rs) {
        try {
            int id = rs.getInt("FILM_ID");
            Film film = Film.builder()
                    .id(id)
                    .name(rs.getString("TITLE"))
                    .description(rs.getString("DESCRIPTION"))
                    .releaseDate(rs.getDate("RELEASE_DATE").toLocalDate())
                    .duration(rs.getInt("DURATION"))
                    .mpa(new Mpa(
                            rs.getInt("RATING_ID"),
                            rs.getString("RATING_NAME")
                    ))
                    .build();
            return film;
        } catch (SQLException e) {
            throw new NotFoundException("Не удалось найти фильм.");
        }
    }

    private Mpa makeMpa(int id) {
        String sql = "SELECT RATING_NAME FROM PUBLIC.RATINGS WHERE RATING_ID = ?;";
        SqlRowSet set = jdbcTemplate.queryForRowSet(sql, id);
        set.next();
        String mpaName = set.getString("RATING_NAME");
        return new Mpa(id, mpaName);
    }
}