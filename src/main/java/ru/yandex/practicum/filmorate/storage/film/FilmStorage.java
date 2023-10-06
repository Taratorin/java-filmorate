package ru.yandex.practicum.filmorate.storage.film;

import ru.yandex.practicum.filmorate.model.Film;

import java.util.List;

public interface FilmStorage {
    List<Film> getFilms();

    Film createFilm(Film film);

    Film updateFilm(Film film);

    Film getFilmById(int id);

    boolean isFilmPresent(int id);

    void addLikeToFilm(int id, int userId);

    void deleteLike(int id, int userId);

    List<Film> getPopularFilms(int count);
}
