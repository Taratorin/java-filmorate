package ru.yandex.practicum.filmorate.storage.film;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class InMemoryFilmStorage implements FilmStorage {

    private int id;
    private final Map<Integer, Film> films = new HashMap<>();

    @Override
    public List<Film> getFilms() {
        return new ArrayList<>(films.values());
    }

    @Override
    public Map<Integer, Film> getFilmsMap() {
        return new HashMap<>(films);
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
        if (!films.containsKey(film.getId())) {
            throw new NotFoundException("Некорректный id для обновления фильма.");
        } else {
            films.put(film.getId(), film);
            return film;
        }
    }

    private void newId() {
        ++id;
    }
}
