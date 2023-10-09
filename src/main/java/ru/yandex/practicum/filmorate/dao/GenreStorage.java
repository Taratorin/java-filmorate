package ru.yandex.practicum.filmorate.dao;

import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;

import java.util.List;
import java.util.Optional;

public interface GenreStorage {
    List<Genre> getGenreList();

    Optional<Genre> getGenreById(int id);

    void attachGenreToFilm(List<Film> films);
}
