package ru.yandex.practicum.filmorate;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.dao.FilmDbStorage;
import ru.yandex.practicum.filmorate.storage.dao.GenreStorageDb;
import ru.yandex.practicum.filmorate.storage.dao.MpaStorageDb;
import ru.yandex.practicum.filmorate.storage.dao.UserDbStorage;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Transactional
class FilmorateApplicationTests {

    private final UserDbStorage userStorage;
    private final FilmDbStorage filmStorage;
    private final MpaStorageDb mpaStorageDb;
    private final GenreStorageDb genreStorageDb;

    @Test
    public void testGetUsers() {
        List<User> users = userStorage.getUsers();
        assertEquals(users.size(), 3);
    }

    @Test
    public void testCreateUser() {
        User userToPut = getUser().get(0);
        userStorage.createUser(userToPut);
        User userInBd = userStorage.getUserById(4).get();
        assertEquals(userToPut, userInBd);
    }

    @Test
    public void testGetUserById() {
        Optional<User> userOptional = userStorage.getUserById(1);
        assertThat(userOptional)
                .isPresent()
                .hasValueSatisfying(user ->
                        assertThat(user).hasFieldOrPropertyWithValue("id", 1)
                );
    }

    @Test
    public void testUpdateUser() {
        User userToPut = userStorage.getUserById(1).get();
        userToPut.setName("newName");
        userStorage.updateUser(userToPut);
        Optional<User> userOptional = userStorage.getUserById(1);
        assertThat(userOptional)
                .isPresent()
                .hasValueSatisfying(user ->
                        assertThat(user).hasFieldOrPropertyWithValue("name", "newName")
                );
    }

    @Test
    public void testGetFriends() {
        userStorage.checkAndUpdateFriends(1, 2);
        userStorage.checkAndUpdateFriends(3, 2);
        List<User> friends1 = userStorage.getFriends(1);
        List<User> friends2 = userStorage.getFriends(2);
        List<User> friends3 = userStorage.getFriends(3);
        assertEquals(friends1.size(), 1);
        assertEquals(friends2.size(), 0);
        assertEquals(friends3.size(), 1);
        assertEquals(friends1, friends3);
    }

    @Test
    public void testCheckAndUpdateFriendsWhenFriendApproved() {
        userStorage.checkAndUpdateFriends(1, 2);
        userStorage.checkAndUpdateFriends(3, 2);
        userStorage.checkAndUpdateFriends(2, 1);
        userStorage.checkAndUpdateFriends(2, 3);
        List<User> friends1 = userStorage.getFriends(1);
        List<User> friends2 = userStorage.getFriends(2);
        List<User> friends3 = userStorage.getFriends(3);
        assertEquals(friends1.size(), 1);
        assertEquals(friends2.size(), 2);
        assertEquals(friends3.size(), 1);
        assertEquals(friends1, friends3);
        assertEquals(friends2.get(0), userStorage.getUserById(1).get());
        assertEquals(friends2.get(1), userStorage.getUserById(3).get());
    }

    @Test
    public void testIsUserPresentShouldReturnTrue() {
        boolean bool = userStorage.isUserPresent(1);
        assertTrue(bool);
    }

    @Test
    public void testIsUserPresentShouldReturnFalse() {
        boolean bool = userStorage.isUserPresent(10);
        assertFalse(bool);
    }

    @Test
    public void testDeleteFriendNoAction() {
        userStorage.checkAndUpdateFriends(1, 2);
        List<User> friends1 = userStorage.getFriends(1);
        List<User> friends2 = userStorage.getFriends(2);
        assertEquals(friends1.get(0), userStorage.getUserById(2).get());
        assertTrue(friends2.isEmpty());

        userStorage.deleteFriend(2, 1);

        friends1 = userStorage.getFriends(1);
        friends2 = userStorage.getFriends(2);
        assertEquals(friends1.get(0), userStorage.getUserById(2).get());
        assertTrue(friends2.isEmpty());
    }

    @Test
    public void testDeleteFriendWithoutApproved() {
        userStorage.checkAndUpdateFriends(1, 2);
        List<User> friends1 = userStorage.getFriends(1);
        List<User> friends2 = userStorage.getFriends(2);
        assertEquals(friends1.get(0), userStorage.getUserById(2).get());
        assertTrue(friends2.isEmpty());

        userStorage.deleteFriend(1, 2);

        friends1 = userStorage.getFriends(1);
        friends2 = userStorage.getFriends(2);
        assertTrue(friends1.isEmpty());
        assertTrue(friends2.isEmpty());
    }

    @Test
    public void testDeleteFriendWhenApproved() {
        userStorage.checkAndUpdateFriends(1, 2);
        userStorage.checkAndUpdateFriends(2, 1);
        List<User> friends1 = userStorage.getFriends(1);
        List<User> friends2 = userStorage.getFriends(2);
        assertEquals(friends1.get(0), userStorage.getUserById(2).get());
        assertEquals(friends2.get(0), userStorage.getUserById(1).get());

        userStorage.deleteFriend(1, 2);

        friends1 = userStorage.getFriends(1);
        friends2 = userStorage.getFriends(2);
        assertTrue(friends1.isEmpty());
        assertEquals(friends2.get(0), userStorage.getUserById(1).get());
    }

    @Test
    public void testGetFilms() {
        List<Optional<Film>> films = filmStorage.getFilms();
        assertEquals(films.size(), 3);
    }

    @Test
    public void testCreateFilm() {
        Film film = getFilms().get(0);
        filmStorage.createFilm(film);
        Optional<Film> optionalFilm = filmStorage.getFilmById(4);
        assertThat(optionalFilm)
                .isPresent()
                .hasValueSatisfying(f ->
                        assertThat(f).hasFieldOrPropertyWithValue("id", 4)
                );
    }

    @Test
    public void testUpdateFilm() {
        Film filmToUpdate = filmStorage.getFilmById(1).get();
        filmToUpdate.setName("New name for first film");
        filmStorage.updateFilm(filmToUpdate);
        Optional<Film> optionalFilm = filmStorage.getFilmById(1);
        assertThat(optionalFilm)
                .isPresent()
                .hasValueSatisfying(f ->
                        assertThat(f).hasFieldOrPropertyWithValue("name", "New name for first film")
                );
    }

    @Test
    public void testGetFilmById() {
        Optional<Film> optionalFilm = filmStorage.getFilmById(1);
        assertThat(optionalFilm)
                .isPresent()
                .hasValueSatisfying(f -> {
                    assertThat(f).hasFieldOrPropertyWithValue("id", 1);
                    assertThat(f).hasFieldOrPropertyWithValue("name", "First film");
                    assertThat(f).hasFieldOrPropertyWithValue("description", "Description of first film");
                    assertThat(f).hasFieldOrPropertyWithValue("releaseDate", LocalDate.of(1960, 12, 5));
                    assertThat(f).hasFieldOrPropertyWithValue("duration", 120);
                });
    }

    @Test
    public void testIsFilmPresentTrue() {
        assertTrue(filmStorage.isFilmPresent(1));
    }

    @Test
    public void testIsFilmPresentFalse() {
        assertFalse(filmStorage.isFilmPresent(100));
    }


    @Test
    public void testAddLikeToFilm() {
        Film film = filmStorage.getFilmById(2).get();
        assertEquals(film.getLikes().size(), 0);
        filmStorage.addLikeToFilm(2, 1);
        assertEquals(film.getLikes().size(), 0);
    }

    @Test
    public void testAddLikeToFilmTwice() {
        Film film = filmStorage.getFilmById(2).get();
        assertEquals(film.getLikes().size(), 0);
        filmStorage.addLikeToFilm(2, 1);
        assertEquals(film.getLikes().size(), 0);
        filmStorage.addLikeToFilm(2, 1);
        assertEquals(film.getLikes().size(), 0);
    }

    @Test
    public void testDeleteLike() {
        filmStorage.addLikeToFilm(2, 1);
        Film film = filmStorage.getFilmById(2).get();
        assertEquals(film.getLikes().size(), 1);
        filmStorage.deleteLike(2, 1);
        Film filmDeletedLike = filmStorage.getFilmById(2).get();
        assertEquals(filmDeletedLike.getLikes().size(), 0);
    }

    @Test
    public void testDeleteLikeNotCorrectUser() {
        filmStorage.addLikeToFilm(2, 1);
        Film film = filmStorage.getFilmById(2).get();
        assertEquals(film.getLikes().size(), 1);
        filmStorage.deleteLike(2, 2);
        Film filmDeletedLike = filmStorage.getFilmById(2).get();
        assertEquals(filmDeletedLike.getLikes().size(), 1);
    }

    @Test
    public void testDeleteLikeNotCorrectFilm() {
        filmStorage.addLikeToFilm(2, 1);
        Film film = filmStorage.getFilmById(2).get();
        assertEquals(film.getLikes().size(), 1);
        filmStorage.deleteLike(3, 1);
        Film filmDeletedLike = filmStorage.getFilmById(2).get();
        assertEquals(filmDeletedLike.getLikes().size(), 1);
    }

    @Test
    public void testGetMpaList() {
        List<Mpa> mpaList = mpaStorageDb.getMpaList();
        assertEquals(5, mpaList.size());
    }

    @Test
    public void testGetMpaById() {
        assertThat(mpaStorageDb.getMpaById(1))
                .isPresent()
                .hasValueSatisfying(user -> {
                    assertThat(user).hasFieldOrPropertyWithValue("id", 1);
                    assertThat(user).hasFieldOrPropertyWithValue("name", "G");
                });
        assertThat(mpaStorageDb.getMpaById(2))
                .isPresent()
                .hasValueSatisfying(user -> {
                    assertThat(user).hasFieldOrPropertyWithValue("id", 2);
                    assertThat(user).hasFieldOrPropertyWithValue("name", "PG");
                });
        assertThat(mpaStorageDb.getMpaById(3))
                .isPresent()
                .hasValueSatisfying(user -> {
                    assertThat(user).hasFieldOrPropertyWithValue("id", 3);
                    assertThat(user).hasFieldOrPropertyWithValue("name", "PG-13");
                });
        assertThat(mpaStorageDb.getMpaById(4))
                .isPresent()
                .hasValueSatisfying(user -> {
                    assertThat(user).hasFieldOrPropertyWithValue("id", 4);
                    assertThat(user).hasFieldOrPropertyWithValue("name", "R");
                });
        assertThat(mpaStorageDb.getMpaById(5))
                .isPresent()
                .hasValueSatisfying(user -> {
                    assertThat(user).hasFieldOrPropertyWithValue("id", 5);
                    assertThat(user).hasFieldOrPropertyWithValue("name", "NC-17");
                });
    }

    @Test
    public void testGetGenreList() {
        List<Genre> genreList = genreStorageDb.getGenreList();
        assertEquals(6, genreList.size());
    }

    @Test
    public void testGetGenreById() {
        assertThat(genreStorageDb.getGenreById(1))
                .isPresent()
                .hasValueSatisfying(user -> {
                    assertThat(user).hasFieldOrPropertyWithValue("id", 1);
                    assertThat(user).hasFieldOrPropertyWithValue("name", "Комедия");
                });
        assertThat(genreStorageDb.getGenreById(2))
                .isPresent()
                .hasValueSatisfying(user -> {
                    assertThat(user).hasFieldOrPropertyWithValue("id", 2);
                    assertThat(user).hasFieldOrPropertyWithValue("name", "Драма");
                });
        assertThat(genreStorageDb.getGenreById(3))
                .isPresent()
                .hasValueSatisfying(user -> {
                    assertThat(user).hasFieldOrPropertyWithValue("id", 3);
                    assertThat(user).hasFieldOrPropertyWithValue("name", "Мультфильм");
                });
        assertThat(genreStorageDb.getGenreById(4))
                .isPresent()
                .hasValueSatisfying(user -> {
                    assertThat(user).hasFieldOrPropertyWithValue("id", 4);
                    assertThat(user).hasFieldOrPropertyWithValue("name", "Триллер");
                });
        assertThat(genreStorageDb.getGenreById(5))
                .isPresent()
                .hasValueSatisfying(user -> {
                    assertThat(user).hasFieldOrPropertyWithValue("id", 5);
                    assertThat(user).hasFieldOrPropertyWithValue("name", "Документальный");
                });
        assertThat(genreStorageDb.getGenreById(6))
                .isPresent()
                .hasValueSatisfying(user -> {
                    assertThat(user).hasFieldOrPropertyWithValue("id", 6);
                    assertThat(user).hasFieldOrPropertyWithValue("name", "Боевик");
                });
    }

    private List<User> getUser() {
        return List.of(
                User.builder()
                        .id(1)
                        .email("one@mail.ru")
                        .login("dolore")
                        .name("Nick Name")
                        .birthday(LocalDate.of(1946, 8, 20))
                        .build(),
                User.builder()
                        .id(2)
                        .email("two@mail.ru")
                        .login("gowetitu")
                        .name("Name of Second User")
                        .birthday(LocalDate.of(1995, 1, 1))
                        .build(),
                User.builder()
                        .id(3)
                        .email("three@mail.ru")
                        .login("sogjgvl")
                        .name("Name of Third User")
                        .birthday(LocalDate.of(1984, 10, 21))
                        .build()
        );
    }

    private List<Film> getFilms() {
        return List.of(
                Film.builder()
                        .id(1)
                        .name("First film")
                        .description("Description of first film")
                        .releaseDate(LocalDate.of(1960, 12, 5))
                        .duration(120)
                        .mpa(new Mpa(1, "G"))
                        .build(),
                Film.builder()
                        .id(2)
                        .name("Second film")
                        .description("Description of second film")
                        .releaseDate(LocalDate.of(1990, 12, 5))
                        .duration(200)
                        .mpa(new Mpa(1, "G"))
                        .build(),
                Film.builder()
                        .id(3)
                        .name("Third film")
                        .description("Description of third film")
                        .releaseDate(LocalDate.of(2002, 12, 5))
                        .duration(20)
                        .mpa(new Mpa(1, "G"))
                        .build()
        );
    }
}