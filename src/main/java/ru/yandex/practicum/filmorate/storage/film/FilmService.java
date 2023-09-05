package ru.yandex.practicum.filmorate.storage.film;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class FilmService {

    private final FilmStorage filmStorage;
    private final UserStorage userStorage;

    @Autowired
    public FilmService(FilmStorage filmStorage, UserStorage userStorage) {
        this.filmStorage = filmStorage;
        this.userStorage = userStorage;
    }

    public List<Film> getFilms() {
        return filmStorage.getFilms();
    }

    public Film getFilmById(int id) {
        Film film = filmStorage.getFilmsMap().get(id);
        if (film == null) {
            throw new NotFoundException("Фильм не найден.");
        }
        return film;
    }

    public Film createFilm(Film film) {
        return filmStorage.createFilm(film);
    }

    public Film updateFilm(Film film) {
        return filmStorage.updateFilm(film);
    }

    public Film likeFilm(int id, int userId) {
        Film film = filmStorage.getFilmsMap().get(id);
        User user = userStorage.getUserById(userId);
        if (film == null) {
            throw new NotFoundException("Фильм не найден.");
        }
        if (user == null) {
            throw new NotFoundException("Пользователь не найден.");
        }
        film.addLike(userId);
        return film;
    }


    public Film unlikeFilm(int id, int userId) {
        Film film = filmStorage.getFilmsMap().get(id);
        User user = userStorage.getUserById(userId);
        if (film == null) {
            throw new NotFoundException("Фильм не найден.");
        }
        if (user == null) {
            throw new NotFoundException("Пользователь не найден.");
        }
        film.deleteLike(userId);
        return film;
    }

    public List<Film> getPopularFilms(int count) {
        Comparator<Film> comparator =
                Comparator.comparing(film -> -1 * film.getLikes().size());
        return filmStorage.getFilms().stream()
                .sorted(comparator)
                .limit(count)
                .collect(Collectors.toList());
    }
}