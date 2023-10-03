package ru.yandex.practicum.filmorate.storage.film;

import ru.yandex.practicum.filmorate.model.Film;

import java.util.List;
import java.util.Optional;

public interface FilmStorage {
    List<Optional<Film>> getFilms();

    Film createFilm(Film film);

    Film updateFilm(Film film);

    Optional<Film> getFilmById(int id);

    boolean isFilmPresent(int id);

    void addLikeToFilm(int id, int userId);

    void deleteLike(int id, int userId);
}
