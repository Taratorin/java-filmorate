package ru.yandex.practicum.filmorate.storage.film;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Component("inMemoryFilmStorage")
public class InMemoryFilmStorage implements FilmStorage {

    private int id;
    private final Map<Integer, Film> films = new HashMap<>();

    @Override
    public List<Optional<Film>> getFilms() {
        return films.values().stream()
                .map(Optional::of)
                .collect(Collectors.toList());
    }

    @Override
    public Film createFilm(Film film) {
        newId();
        film.setId(id);
        films.put(film.getId(), film);
        return film;
    }

    @Override
    public Film updateFilm(Film film) {
        films.put(film.getId(), film);
        return film;
    }

    @Override
    public Optional<Film> getFilmById(int id) {
        return Optional.ofNullable(films.get(id));
    }

    @Override
    public boolean isFilmPresent(int id) {
        return films.containsKey(id);
    }

    @Override
    public void addLikeToFilm(int id, int userId) {
        films.get(id).addLike(userId);
    }

    @Override
    public void deleteLike(int id, int userId) {
        films.get(id).deleteLike(userId);
    }

    private void newId() {
        ++id;
    }
}