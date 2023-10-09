package ru.yandex.practicum.filmorate;

import lombok.AllArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.filmorate.dao.impl.FilmDbStorage;
import ru.yandex.practicum.filmorate.dao.impl.GenreStorageDb;
import ru.yandex.practicum.filmorate.dao.impl.MpaStorageDb;
import ru.yandex.practicum.filmorate.dao.impl.UserDbStorage;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@AutoConfigureTestDatabase
@AllArgsConstructor(onConstructor = @__(@Autowired))
@Transactional
class FilmorateApplicationTests {

    private UserDbStorage userStorage;
    private FilmDbStorage filmStorage;
    private MpaStorageDb mpaStorageDb;
    private GenreStorageDb genreStorageDb;

    @Test
    public void testGetUsers() {
        List<User> users = userStorage.getUsers();
        assertEquals(users.size(), 0);
    }

    @Test
    public void testCreateUser() {
        User userToPut = getUser().get(0);
        User user1 = userStorage.createUser(userToPut);
        User userInBd = userStorage.getUserById(user1.getId()).get();
        assertEquals(userToPut, userInBd);
    }

    @Test
    public void testGetUserById() {
        User user1 = getUser().get(0);
        User user2 = userStorage.createUser(user1);
        Optional<User> userOptional = userStorage.getUserById(user2.getId());
        assertThat(userOptional)
                .isPresent()
                .hasValueSatisfying(user ->
                        assertThat(user).hasFieldOrPropertyWithValue("id", user2.getId())
                );
    }

    @Test
    public void testUpdateUser() {
        User user1 = userStorage.createUser(getUser().get(0));
        User userToUpdate = userStorage.getUserById(user1.getId()).get();
        userToUpdate.setName("newName");
        userStorage.updateUser(userToUpdate);
        Optional<User> userOptional = userStorage.getUserById(user1.getId());
        assertThat(userOptional)
                .isPresent()
                .hasValueSatisfying(user ->
                        assertThat(user).hasFieldOrPropertyWithValue("name", "newName")
                );
    }

    @Test
    public void testGetFriends() {
        User user1 = userStorage.createUser(getUser().get(0));
        User user2 = userStorage.createUser(getUser().get(1));
        User user3 = userStorage.createUser(getUser().get(2));
        userStorage.checkAndUpdateFriends(user1.getId(), user2.getId());
        userStorage.checkAndUpdateFriends(user3.getId(), user2.getId());
        List<User> friends1 = userStorage.getFriends(user1.getId());
        List<User> friends2 = userStorage.getFriends(user2.getId());
        List<User> friends3 = userStorage.getFriends(user3.getId());
        assertEquals(friends1.size(), 1);
        assertEquals(friends2.size(), 0);
        assertEquals(friends3.size(), 1);
        assertEquals(friends1, friends3);
    }

    @Test
    public void testCheckAndUpdateFriendsWhenFriendApproved() {
        User user1 = userStorage.createUser(getUser().get(0));
        User user2 = userStorage.createUser(getUser().get(1));
        User user3 = userStorage.createUser(getUser().get(2));
        userStorage.checkAndUpdateFriends(user1.getId(), user2.getId());
        userStorage.checkAndUpdateFriends(user3.getId(), user2.getId());
        userStorage.checkAndUpdateFriends(user2.getId(), user1.getId());
        userStorage.checkAndUpdateFriends(user2.getId(), user3.getId());
        List<User> friends1 = userStorage.getFriends(user1.getId());
        List<User> friends2 = userStorage.getFriends(user2.getId());
        List<User> friends3 = userStorage.getFriends(user3.getId());
        assertEquals(friends1.size(), 1);
        assertEquals(friends2.size(), 2);
        assertEquals(friends3.size(), 1);
        assertEquals(friends1, friends3);
        assertEquals(friends2.get(0), userStorage.getUserById(user1.getId()).get());
        assertEquals(friends2.get(1), userStorage.getUserById(user3.getId()).get());
    }

    @Test
    public void testIsUserPresentShouldReturnTrue() {
        userStorage.createUser(getUser().get(0));
        boolean bool = userStorage.isUserPresent(1);
        assertTrue(bool);
    }

    @Test
    public void testIsUserPresentShouldReturnFalse() {
        userStorage.createUser(getUser().get(0));
        boolean bool = userStorage.isUserPresent(100);
        assertFalse(bool);
    }

    @Test
    public void testDeleteFriendNoAction() {
        User user1 = userStorage.createUser(getUser().get(0));
        User user2 = userStorage.createUser(getUser().get(1));
        userStorage.checkAndUpdateFriends(user1.getId(), user2.getId());
        List<User> friends1 = userStorage.getFriends(user1.getId());
        List<User> friends2 = userStorage.getFriends(user2.getId());
        assertEquals(friends1.get(0), userStorage.getUserById(user2.getId()).get());
        assertTrue(friends2.isEmpty());

        userStorage.deleteFriend(user2.getId(), user1.getId());

        friends1 = userStorage.getFriends(user1.getId());
        friends2 = userStorage.getFriends(user2.getId());
        assertEquals(friends1.get(0), userStorage.getUserById(user2.getId()).get());
        assertTrue(friends2.isEmpty());
    }

    @Test
    public void testDeleteFriendWithoutApprove() {
        User user1 = userStorage.createUser(getUser().get(0));
        User user2 = userStorage.createUser(getUser().get(1));
        userStorage.checkAndUpdateFriends(user1.getId(), user2.getId());
        List<User> friends1 = userStorage.getFriends(user1.getId());
        List<User> friends2 = userStorage.getFriends(user2.getId());
        assertEquals(friends1.get(0), userStorage.getUserById(user2.getId()).get());
        assertTrue(friends2.isEmpty());

        userStorage.deleteFriend(8, 9);

        friends1 = userStorage.getFriends(8);
        friends2 = userStorage.getFriends(9);
        assertTrue(friends1.isEmpty());
        assertTrue(friends2.isEmpty());
    }

    @Test
    public void testDeleteFriendWhenApproved() {
        User user1 = userStorage.createUser(getUser().get(0));
        User user2 = userStorage.createUser(getUser().get(1));
        userStorage.checkAndUpdateFriends(user1.getId(), user2.getId());
        userStorage.checkAndUpdateFriends(user2.getId(), user1.getId());
        List<User> friends1 = userStorage.getFriends(user1.getId());
        List<User> friends2 = userStorage.getFriends(user2.getId());
        assertEquals(friends1.get(0), userStorage.getUserById(user2.getId()).get());
        assertEquals(friends2.get(0), userStorage.getUserById(user1.getId()).get());

        userStorage.deleteFriend(user1.getId(), user2.getId());

        friends1 = userStorage.getFriends(user1.getId());
        friends2 = userStorage.getFriends(user2.getId());
        assertTrue(friends1.isEmpty());
        assertEquals(friends2.get(0), userStorage.getUserById(user1.getId()).get());
    }

    @Test
    public void testGetFilms() {
        List<Film> films = filmStorage.getFilms();
        assertEquals(films.size(), 0);
    }

    @Test
    public void testCreateFilm() {
        Film film = getFilms().get(0);
        filmStorage.createFilm(film);
        Film filmFromDb = filmStorage.getFilmById(1);
        assertEquals(filmFromDb.getId(), 1);
    }

    @Test
    public void testUpdateFilm() {
        filmStorage.createFilm(getFilms().get(0));
        Film filmToUpdate = filmStorage.getFilmById(1);
        filmToUpdate.setName("New name for first film");
        filmStorage.updateFilm(filmToUpdate);
        Film filmFromDb = filmStorage.getFilmById(1);
        assertEquals(filmFromDb.getName(), "New name for first film");
    }

    @Test
    public void testGetFilmById() {
        filmStorage.createFilm(getFilms().get(0));
        Film filmFromDb = filmStorage.getFilmById(1);
        assertEquals(filmFromDb.getId(), 1);
        assertEquals(filmFromDb.getName(), "First film");
        assertEquals(filmFromDb.getDescription(), "Description of first film");
        assertEquals(filmFromDb.getReleaseDate(), LocalDate.of(1960, 12, 5));
    }

    @Test
    public void testIsFilmPresentTrue() {
        filmStorage.createFilm(getFilms().get(0));
        assertTrue(filmStorage.isFilmPresent(1));
    }

    @Test
    public void testIsFilmPresentFalse() {
        assertFalse(filmStorage.isFilmPresent(1));
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