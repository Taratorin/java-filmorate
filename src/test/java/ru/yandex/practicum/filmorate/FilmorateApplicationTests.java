package ru.yandex.practicum.filmorate;

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
@Transactional
class FilmorateApplicationTests {

    private UserDbStorage userStorage;
    private FilmDbStorage filmStorage;
    private MpaStorageDb mpaStorageDb;
    private GenreStorageDb genreStorageDb;

    @Autowired
    public FilmorateApplicationTests(UserDbStorage userStorage, FilmDbStorage filmStorage, MpaStorageDb mpaStorageDb, GenreStorageDb genreStorageDb) {
        this.userStorage = userStorage;
        this.filmStorage = filmStorage;
        this.mpaStorageDb = mpaStorageDb;
        this.genreStorageDb = genreStorageDb;
    }

    @Test
    @Transactional
    public void testGetUsers() {
        List<User> users = userStorage.getUsers();
        assertEquals(users.size(), 0);
    }

    @Test
    @Transactional
    public void testCreateUser() {
        User userToPut = getUser().get(0);
        userStorage.createUser(userToPut);
        User userInBd = userStorage.getUserById(20).get();
        assertEquals(userToPut, userInBd);
    }

    @Test
    @Transactional
    public void testGetUserById() {
        User user1 = getUser().get(0);
        userStorage.createUser(user1);
        Optional<User> userOptional = userStorage.getUserById(25);
        assertThat(userOptional)
                .isPresent()
                .hasValueSatisfying(user ->
                        assertThat(user).hasFieldOrPropertyWithValue("id", 25)
                );
    }

    @Test
    @Transactional
    public void testUpdateUser() {
        userStorage.createUser(getUser().get(0));
        User userToUpdate = userStorage.getUserById(4).get();
        userToUpdate.setName("newName");
        userStorage.updateUser(userToUpdate);
        Optional<User> userOptional = userStorage.getUserById(4);
        assertThat(userOptional)
                .isPresent()
                .hasValueSatisfying(user ->
                        assertThat(user).hasFieldOrPropertyWithValue("name", "newName")
                );
    }

    @Test
    @Transactional
    public void testGetFriends() {
        userStorage.createUser(getUser().get(0));
        userStorage.createUser(getUser().get(1));
        userStorage.createUser(getUser().get(2));
        userStorage.checkAndUpdateFriends(12, 13);
        userStorage.checkAndUpdateFriends(14, 13);
        List<User> friends1 = userStorage.getFriends(12);
        List<User> friends2 = userStorage.getFriends(13);
        List<User> friends3 = userStorage.getFriends(14);
        assertEquals(friends1.size(), 1);
        assertEquals(friends2.size(), 0);
        assertEquals(friends3.size(), 1);
        assertEquals(friends1, friends3);
    }

    @Test
    @Transactional
    public void testCheckAndUpdateFriendsWhenFriendApproved() {
        userStorage.createUser(getUser().get(0));
        userStorage.createUser(getUser().get(1));
        userStorage.createUser(getUser().get(2));
        userStorage.checkAndUpdateFriends(5, 6);
        userStorage.checkAndUpdateFriends(7, 6);
        userStorage.checkAndUpdateFriends(6, 5);
        userStorage.checkAndUpdateFriends(6, 7);
        List<User> friends1 = userStorage.getFriends(5);
        List<User> friends2 = userStorage.getFriends(6);
        List<User> friends3 = userStorage.getFriends(7);
        assertEquals(friends1.size(), 1);
        assertEquals(friends2.size(), 2);
        assertEquals(friends3.size(), 1);
        assertEquals(friends1, friends3);
        assertEquals(friends2.get(0), userStorage.getUserById(5).get());
        assertEquals(friends2.get(1), userStorage.getUserById(7).get());
    }

    @Test
    @Transactional
    public void testIsUserPresentShouldReturnTrue() {
        userStorage.createUser(getUser().get(0));
        boolean bool = userStorage.isUserPresent(1);
        assertTrue(bool);
    }

    @Test
    @Transactional
    public void testIsUserPresentShouldReturnFalse() {
        userStorage.createUser(getUser().get(0));
        boolean bool = userStorage.isUserPresent(100);
        assertFalse(bool);
    }

    @Test
    @Transactional
    public void testDeleteFriendNoAction() {
        userStorage.createUser(getUser().get(0));
        userStorage.createUser(getUser().get(1));
        userStorage.checkAndUpdateFriends(26, 27);
        List<User> friends1 = userStorage.getFriends(26);
        List<User> friends2 = userStorage.getFriends(27);
        assertEquals(friends1.get(0), userStorage.getUserById(27).get());
        assertTrue(friends2.isEmpty());

        userStorage.deleteFriend(27, 26);

        friends1 = userStorage.getFriends(26);
        friends2 = userStorage.getFriends(27);
        assertEquals(friends1.get(0), userStorage.getUserById(27).get());
        assertTrue(friends2.isEmpty());
    }

    @Test
    @Transactional
    public void testDeleteFriendWithoutApprove() {
        userStorage.createUser(getUser().get(0));
        userStorage.createUser(getUser().get(1));
        userStorage.checkAndUpdateFriends(8, 9);
        List<User> friends1 = userStorage.getFriends(8);
        List<User> friends2 = userStorage.getFriends(9);
        assertEquals(friends1.get(0), userStorage.getUserById(9).get());
        assertTrue(friends2.isEmpty());

        userStorage.deleteFriend(8, 9);

        friends1 = userStorage.getFriends(8);
        friends2 = userStorage.getFriends(9);
        assertTrue(friends1.isEmpty());
        assertTrue(friends2.isEmpty());
    }

    @Test
    @Transactional
    public void testDeleteFriendWhenApproved() {
        userStorage.createUser(getUser().get(0));
        userStorage.createUser(getUser().get(1));
        userStorage.checkAndUpdateFriends(10, 11);
        userStorage.checkAndUpdateFriends(11, 10);
        List<User> friends1 = userStorage.getFriends(10);
        List<User> friends2 = userStorage.getFriends(11);
        assertEquals(friends1.get(0), userStorage.getUserById(11).get());
        assertEquals(friends2.get(0), userStorage.getUserById(10).get());

        userStorage.deleteFriend(10, 11);

        friends1 = userStorage.getFriends(10);
        friends2 = userStorage.getFriends(11);
        assertTrue(friends1.isEmpty());
        assertEquals(friends2.get(0), userStorage.getUserById(10).get());
    }

    @Test
    @Transactional
    public void testGetFilms() {
        List<Film> films = filmStorage.getFilms();
        assertEquals(films.size(), 0);
    }

    @Test
    @Transactional
    public void testCreateFilm() {
        Film film = getFilms().get(0);
        filmStorage.createFilm(film);
        Film filmFromDb = filmStorage.getFilmById(1);
        assertEquals(filmFromDb.getId(), 1);
    }

    @Test
    @Transactional
    public void testUpdateFilm() {
        filmStorage.createFilm(getFilms().get(0));
        Film filmToUpdate = filmStorage.getFilmById(1);
        filmToUpdate.setName("New name for first film");
        filmStorage.updateFilm(filmToUpdate);
        Film filmFromDb = filmStorage.getFilmById(1);
        assertEquals(filmFromDb.getName(), "New name for first film");
    }

    @Test
    @Transactional
    public void testGetFilmById() {
        filmStorage.createFilm(getFilms().get(0));
        Film filmFromDb = filmStorage.getFilmById(1);
        assertEquals(filmFromDb.getId(), 1);
        assertEquals(filmFromDb.getName(), "First film");
        assertEquals(filmFromDb.getDescription(), "Description of first film");
        assertEquals(filmFromDb.getReleaseDate(), LocalDate.of(1960, 12, 5));
    }

    @Test
    @Transactional
    public void testIsFilmPresentTrue() {
        filmStorage.createFilm(getFilms().get(0));
        assertTrue(filmStorage.isFilmPresent(1));
    }

    @Test
    @Transactional
    public void testIsFilmPresentFalse() {
        assertFalse(filmStorage.isFilmPresent(1));
    }


    @Test
    @Transactional
    public void testAddLikeToFilm() {
        filmStorage.createFilm(getFilms().get(0));
        filmStorage.createFilm(getFilms().get(1));
        userStorage.createUser(getUser().get(0));
        userStorage.createUser(getUser().get(1));
        Film film = filmStorage.getFilmById(2);
        assertEquals(film.getLikesCount(), 0);
        filmStorage.addLikeToFilm(2, 22);
        assertEquals(film.getLikesCount(), 0);
    }

    @Test
    @Transactional
    public void testAddLikeToFilmTwice() {
        filmStorage.createFilm(getFilms().get(0));
        filmStorage.createFilm(getFilms().get(1));
        userStorage.createUser(getUser().get(0));
        userStorage.createUser(getUser().get(1));
        Film film = filmStorage.getFilmById(2);
        assertEquals(film.getLikesCount(), 0);
        filmStorage.addLikeToFilm(2, 3);
        assertEquals(film.getLikesCount(), 0);
        filmStorage.addLikeToFilm(2, 3);
        assertEquals(film.getLikesCount(), 0);
    }

    @Test
    @Transactional
    public void testDeleteLike() {
        filmStorage.createFilm(getFilms().get(0));
        filmStorage.createFilm(getFilms().get(1));
        userStorage.createUser(getUser().get(0));
        userStorage.createUser(getUser().get(1));
        filmStorage.addLikeToFilm(2, 24);
        Film film = filmStorage.getFilmById(2);
        assertEquals(film.getLikesCount(), 1);
        filmStorage.deleteLike(2, 24);
        Film filmDeletedLike = filmStorage.getFilmById(2);
        assertEquals(filmDeletedLike.getLikesCount(), 0);
    }

    @Test
    @Transactional
    public void testDeleteLikeNotCorrectUser() {
        filmStorage.createFilm(getFilms().get(0));
        filmStorage.createFilm(getFilms().get(1));
        userStorage.createUser(getUser().get(0));
        userStorage.createUser(getUser().get(1));
        filmStorage.addLikeToFilm(2, 18);
        Film film = filmStorage.getFilmById(2);
        assertEquals(film.getLikesCount(), 1);
        filmStorage.deleteLike(2, 19);
        Film filmDeletedLike = filmStorage.getFilmById(2);
        assertEquals(filmDeletedLike.getLikesCount(), 1);
    }

    @Test
    @Transactional
    public void testDeleteLikeNotCorrectFilm() {
        filmStorage.createFilm(getFilms().get(0));
        filmStorage.createFilm(getFilms().get(1));
        userStorage.createUser(getUser().get(0));
        userStorage.createUser(getUser().get(1));
        filmStorage.addLikeToFilm(2, 16);
        Film film = filmStorage.getFilmById(2);
        assertEquals(film.getLikesCount(), 1);
        filmStorage.deleteLike(3, 16);
        Film filmDeletedLike = filmStorage.getFilmById(2);
        assertEquals(filmDeletedLike.getLikesCount(), 1);
    }

    @Test
    @Transactional
    public void testGetMpaList() {
        List<Mpa> mpaList = mpaStorageDb.getMpaList();
        assertEquals(5, mpaList.size());
    }

    @Test
    @Transactional
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
    @Transactional
    public void testGetGenreList() {
        List<Genre> genreList = genreStorageDb.getGenreList();
        assertEquals(6, genreList.size());
    }

    @Test
    @Transactional
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