package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.film.InMemoryFilmStorage;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/films")
public class FilmController {

    private final FilmStorage filmStorage;

    @Autowired
    public FilmController(InMemoryFilmStorage filmStorage) {
        this.filmStorage = filmStorage;
    }

    @GetMapping
    public List<Film> getFilms() {
        return filmStorage.getFilms();
    }

    @PostMapping
    public Film createFilm(@Valid @RequestBody Film film, HttpServletRequest request) {
        logRequest(request);
        return filmStorage.createFilm(film);
    }

    @PutMapping
    public Film updateFilm(@Valid @RequestBody Film film, HttpServletRequest request) {
        logRequest(request);
        return filmStorage.updateFilm(film);
    }

    private void logRequest(HttpServletRequest request) {
        log.debug("Получен запрос к эндпоинту: '{} {}', Строка параметров запроса: '{}'",
                request.getMethod(), request.getRequestURI(), request.getQueryString());
    }
}