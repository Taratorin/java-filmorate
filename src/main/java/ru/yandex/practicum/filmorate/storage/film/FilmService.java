package ru.yandex.practicum.filmorate.storage.film;

import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class FilmService {

    private final FilmStorage filmStorage;
    private final UserStorage userStorage;

    public FilmService(FilmStorage filmStorage, UserStorage userStorage) {
        this.filmStorage = filmStorage;
        this.userStorage = userStorage;
    }

    public List<Film> getFilms() {
        return filmStorage.getFilms().stream()
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }

    public Film getFilmById(int id) {
        return filmStorage.getFilmById(id)
                .orElseThrow(() -> new NotFoundException("Фильм " + id + " не найден."));
    }

    public Film createFilm(Film film) {
        return filmStorage.createFilm(film);
    }

    public Film updateFilm(Film film) {
        int id = film.getId();
        checkFilmId(id);
        return filmStorage.updateFilm(film);
    }

    public Film likeFilm(int id, int userId) {
        checkFilmId(id);
        checkUserId(userId);
        filmStorage.addLikeToFilm(id, userId);
        return getFilmById(id);
    }

    public Film unlikeFilm(int id, int userId) {
        checkFilmId(id);
        checkUserId(userId);
        filmStorage.deleteLike(id, userId);
        return getFilmById(id);
    }

    public List<Film> getPopularFilms(int count) {
        Comparator<Film> comparator =
                Comparator.comparing(film -> -1 * film.getLikesCount());
        return filmStorage.getFilms().stream()
                .filter(Optional::isPresent)
                .map(Optional::get)
                .sorted(comparator)
                .limit(count)
                .collect(Collectors.toList());
    }

    private void checkFilmId(int id) {
        if (!filmStorage.isFilmPresent(id)) {
            throw new NotFoundException("Фильм " + id + " не найден.");
        }
    }

    private void checkUserId(Integer userId) {
        if (!userStorage.isUserPresent(userId)) {
            throw new NotFoundException("Пользователь " + userId + " не найден.");
        }
    }
}