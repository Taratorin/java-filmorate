package ru.yandex.practicum.filmorate.storage.dao;

import lombok.AllArgsConstructor;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

@Component
@AllArgsConstructor
public class FilmDbStorage implements FilmStorage {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public List<Film> getFilms() {
        String sql = "SELECT * FROM PUBLIC.FILMS f JOIN RATINGS r ON f.RATING_ID = r.RATING_ID";
        List<Film> films = jdbcTemplate.query(sql, (rs, rowNum) -> makeFilm(rs));
        Map<Integer, List<Genre>> genres = getGenres(films);
        for (Film film : films) {
            int id = film.getId();
            List<Genre> genreList = genres.get(id);
            film.setGenres(Objects.requireNonNullElseGet(genreList, ArrayList::new));
        }
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
        film.setLikesCount(getLikesSet(film.getId()).size());
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
        film.setLikesCount(getLikesSet(film.getId()).size());
        if (film.getGenres() != null) {
            updateFilmGenre(film);
        }
        Map<Integer, List<Genre>> genres = getGenres(List.of(film));
        int filmId = film.getId();
        List<Genre> genreList = genres.get(filmId);
        film.setGenres(Objects.requireNonNullElseGet(genreList, ArrayList::new));
        return film;
    }

    @Override
    public Film getFilmById(int id) {
        String sql = "SELECT * FROM PUBLIC.FILMS f JOIN RATINGS r ON f.RATING_ID = r.RATING_ID WHERE FILM_ID = ?";
        List<Film> query = jdbcTemplate.query(sql, (rs, rowNum) -> makeFilm(rs), id);
        if (!query.isEmpty()) {
            Map<Integer, List<Genre>> genres = getGenres(query);
            for (Film film : query) {
                int filmId = film.getId();
                List<Genre> genreList = genres.get(filmId);
                film.setGenres(Objects.requireNonNullElseGet(genreList, ArrayList::new));
            }
            return query.get(0);
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
        List<Film> query = jdbcTemplate.query(sql, (rs, rowNum) -> makeFilm(rs));
        if (!query.isEmpty()) {
            Map<Integer, List<Genre>> genres = getGenres(query);
            for (Film film : query) {
                int filmId = film.getId();
                List<Genre> genreList = genres.get(filmId);
                film.setGenres(Objects.requireNonNullElseGet(genreList, ArrayList::new));
            }
            return query;
        } else {
            return getFilms();
        }
    }

    private void updateFilmGenre(Film film) {
        String sqlDelete = "DELETE FROM PUBLIC.FILM_GENRE WHERE FILM_ID = ?;";
        jdbcTemplate.update(sqlDelete, film.getId());
        Set<Genre> genreSet = new HashSet<>(film.getGenres());
        System.out.println(genreSet);
        ArrayList<Genre> genres = new ArrayList<>(genreSet);
        System.out.println(genres);
        jdbcTemplate.batchUpdate(
                "INSERT INTO PUBLIC.FILM_GENRE (FILM_ID, GENRE_ID) VALUES(?, ?)",
                new BatchPreparedStatementSetter() {
                    public void setValues(PreparedStatement ps, int i)
                            throws SQLException {
                        ps.setInt(1, film.getId());
                        ps.setInt(2, genres.get(i).getId());
                    }

                    public int getBatchSize() {
                        return genres.size();
                    }
                });
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
                    .likesCount(getLikesSet(id).size())
                    .build();
            film.setLikesCount(getLikesSet(film.getId()).size());
            return film;
        } catch (SQLException e) {
            throw new NotFoundException("Не удалось найти фильм.");
        }
    }

    private Map<Integer, List<Genre>> makeGenre(ResultSet rs) throws SQLException {
        Map<Integer, List<Genre>> map = new HashMap<>();
        Genre genre = new Genre(
                rs.getInt("GENRE_ID"),
                rs.getString("GENRE_TITLE"));
        List<Genre> genres = new ArrayList<>(List.of(genre));
        map.put(rs.getInt("FILM_ID"), genres);
        return map;
    }

    private Mpa makeMpa(int id) {
        String sql = "SELECT RATING_NAME FROM PUBLIC.RATINGS WHERE RATING_ID = ?;";
        SqlRowSet set = jdbcTemplate.queryForRowSet(sql, id);
        set.next();
        String mpaName = set.getString("RATING_NAME");
        return new Mpa(id, mpaName);
    }

    private Map<Integer, List<Genre>> getGenres(List<Film> films) {
        List<Integer> filmsId = films.stream()
                .map(Film::getId)
                .collect(Collectors.toList());
        String inSql = String.join(",", Collections.nCopies(filmsId.size(), "?"));
        String sql = "SELECT * " +
                "FROM FILM_GENRE fg JOIN GENRES g ON fg.GENRE_ID = g.GENRE_ID " +
                "WHERE fg.FILM_ID IN (%s)";
        List<Map<Integer, List<Genre>>> maps = jdbcTemplate.query(
                String.format(sql, inSql),
                filmsId.toArray(),
                (rs, rowNum) -> makeGenre(rs)
        );
        Map<Integer, List<Genre>> map = new HashMap<>();
        for (Map<Integer, List<Genre>> currentMap : maps) {
            for (Integer id : currentMap.keySet()) {
                List<Genre> list = map.get(id);
                if (list != null) {
                    list.add(currentMap.get(id).get(0));
                } else {
                    list = currentMap.get(id);
                }
                map.put(id, list);
            }
        }
        return map;
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