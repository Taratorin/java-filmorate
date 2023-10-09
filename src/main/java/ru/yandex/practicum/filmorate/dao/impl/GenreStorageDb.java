package ru.yandex.practicum.filmorate.dao.impl;

import lombok.AllArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.dao.GenreStorage;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

import static java.util.function.UnaryOperator.identity;

@Component
@AllArgsConstructor
public class GenreStorageDb implements GenreStorage {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public List<Genre> getGenreList() {
        String sql = "SELECT * FROM PUBLIC.GENRES;";
        return jdbcTemplate.query(sql, (rs, rowNum) -> makeGenre(rs));
    }

    @Override
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

    @Override
    public void attachGenreToFilm(List<Film> films) {
        for (Film film : films) {
            film.setGenres(new LinkedHashSet<>());
        }
        final Map<Integer, Film> filmById = films.stream().collect(Collectors.toMap(Film::getId, identity()));
        String inSql = String.join(",", Collections.nCopies(films.size(), "?"));
        final String sqlQuery = "select * from GENRES g, film_genre fg" +
                " where fg.GENRE_ID = g.GENRE_ID AND fg.FILM_ID IN (" + inSql + ")";
        jdbcTemplate.query(
                sqlQuery,
                (rs, rowNum) -> {
                    final Film film = filmById.get(rs.getInt("FILM_ID"));
                    Genre genre = makeGenre(rs);
                    film.addGenre(genre);
                    return film;
                },
                films.stream().map(Film::getId).toArray());
    }

    private Genre makeGenre(ResultSet rs) throws SQLException {
        return new Genre(
                rs.getInt("GENRE_ID"),
                rs.getString("GENRE_TITLE")
        );
    }
}