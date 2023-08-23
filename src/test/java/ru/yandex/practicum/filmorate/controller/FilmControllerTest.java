package ru.yandex.practicum.filmorate.controller;

import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;

class FilmControllerTest {

    HttpServletRequest request = mock(HttpServletRequest.class);
    FilmController controller = new FilmController();

    @Test
    public void createFilmCorrectTest() {
        Film film = getFilm();
        Film filmInResponse = controller.createFilm(film, request);
        assertEquals(film, filmInResponse);
    }

    @Test
    public void createFilmBadNameTest() {
        Film film = getFilm();
        film.setName(" ");
        assertThrows((ValidationException.class), () -> controller.createFilm(film, request));
    }

    @Test
    public void createFilmBadDescriptionTest() {
        Film film = getFilm();
        film.setDescription("Пятеро друзей ( комик-группа «Шарло»), приезжают в город Бризуль." +
                " Здесь они хотят разыскать господина Огюста Куглова, который задолжал им деньги," +
                " а именно 20 миллионов. о Куглов, который за время «своего отсутствия», стал кандидатом Коломбани.");
        assertThrows((ValidationException.class), () -> controller.createFilm(film, request));
    }

    @Test
    public void createFilmBadDateTest() {
        Film film = getFilm();
        film.setReleaseDate(LocalDate.of(967, 3, 25));
        assertThrows((ValidationException.class), () -> controller.createFilm(film, request));
    }

    @Test
    public void createFilmBadDurationTest() {
        Film film = getFilm();
        film.setDuration(-200);
        assertThrows((ValidationException.class), () -> controller.createFilm(film, request));
        film.setDuration(100);
    }

    @Test
    public void updateFilmCorrectTest() {
        Film film = getFilm();
        controller.createFilm(film, request);
        film.setName("Film Updated");
        film.setDescription("New film update decription");
        film.setDuration(150);
        Film filmInResponse = controller.updateFilm(film, request);
        assertEquals(film, filmInResponse);
    }

    @Test
    public void updateFilmBadNameTest() {
        Film film = getFilm();
        controller.createFilm(film, request);
        film.setName(" ");
        assertThrows((ValidationException.class), () -> controller.updateFilm(film, request));
    }

    @Test
    public void updateFilmBadDescriptionTest() {
        Film film = getFilm();
        controller.createFilm(film, request);
        film.setDescription("Пятеро друзей ( комик-группа «Шарло»), приезжают в город Бризуль." +
                " Здесь они хотят разыскать господина Огюста Куглова, который задолжал им деньги," +
                " а именно 20 миллионов. о Куглов, который за время «своего отсутствия», стал кандидатом Коломбани.");
        assertThrows((ValidationException.class), () -> controller.updateFilm(film, request));
    }

    @Test
    public void updateFilmBadReleaseDateTest() {
        Film film = getFilm();
        controller.createFilm(film, request);
        film.setReleaseDate(LocalDate.of(967, 3, 25));
        assertThrows((ValidationException.class), () -> controller.updateFilm(film, request));
    }

    @Test
    public void updateFilmBadDurationTest() {
        Film film = getFilm();
        controller.createFilm(film, request);
        film.setDuration(-200);
        assertThrows((ValidationException.class), () -> controller.updateFilm(film, request));
    }

    @Test
    public void updateFilmBadIdTest() {
        Film film = getFilm();
        controller.createFilm(film, request);
        film.setId(200);
        assertThrows((ValidationException.class), () -> controller.updateFilm(film, request));
        film.setId(1);
    }

    private Film getFilm() {
        return Film.builder()
                .name("nisi eiusmod")
                .description("adipisicing")
                .releaseDate(LocalDate.of(1967, 3, 25))
                .duration(100)
                .build();
    }
}