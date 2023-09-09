package ru.yandex.practicum.filmorate.storage.film;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FilmService {

    private final FilmStorage filmStorage;
    private final UserStorage userStorage;

    public List<Film> getFilms() {
        return filmStorage.getFilms();
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
        if (!filmStorage.isFilmPresent(id)) {
            throw new NotFoundException("Фильм " + id + " не найден.");
        }
        return filmStorage.updateFilm(film);
    }

    public Film likeFilm(int id, int userId) {
        checkUserId(userId);
        Film film = getFilmById(id);
        film.addLike(userId);
        return film;
    }

    public Film unlikeFilm(int id, int userId) {
        checkUserId(userId);
        Film film = getFilmById(id);
        film.deleteLike(userId);
        return film;
    }

    public List<Film> getPopularFilms(int count) {
        Comparator<Film> comparator =
                Comparator.comparing(film -> -1 * film.getLikesCount());
        return filmStorage.getFilms().stream()
                .sorted(comparator)
                .limit(count)
                .collect(Collectors.toList());
    }

    private void checkUserId(Integer userId) {
        if (!userStorage.isUserPresent(userId)) {
            throw new NotFoundException("Пользователь " + userId + " не найден.");
        }
    }

}