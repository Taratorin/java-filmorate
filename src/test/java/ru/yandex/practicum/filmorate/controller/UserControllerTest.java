package ru.yandex.practicum.filmorate.controller;

import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;

class UserControllerTest {

    HttpServletRequest request = mock(HttpServletRequest.class);
    UserController controller = new UserController();

    @Test
    void createCorrectUserTest() {
        User user = getUser();
        User userInResponse = controller.createUser(user, request);
        assertEquals(user, userInResponse);
    }

    @Test
    void createUserBadLoginTest() {
        User user = getUser();
        user.setLogin(" ");
        assertThrows(ValidationException.class, () -> controller.createUser(user, request));
    }

    @Test
    void createUserBadEmailTest() {
        User user = getUser();
        user.setEmail("mail@.ru");
        assertThrows(ValidationException.class, () -> controller.createUser(user, request));
    }

    @Test
    void createUserBadBirthdayTest() {
        User user = getUser();
        user.setBirthday(LocalDate.of(2200,1,1));
        assertThrows(ValidationException.class, () -> controller.createUser(user, request));
    }

    @Test
    void updateCorrectUserTest() {
        User user = getUser();
        controller.createUser(user, request);
        user.setName("");
        user.setEmail("mail@yandex.ru");
        user.setBirthday(LocalDate.of(1976,12,2));
        user.setLogin("doloreUpdate");
        User userInResponse = controller.updateUser(user, request);
        assertEquals(user, userInResponse);
    }

    @Test
    void updateUserBadLoginTest() {
        User user = getUser();
        controller.createUser(user, request);
        user.setLogin(" ");
        assertThrows(ValidationException.class, () -> controller.updateUser(user, request));
    }

    @Test
    void updateUserBadEmailTest() {
        User user = getUser();
        controller.createUser(user, request);
        user.setEmail("mail@.ru");
        assertThrows(ValidationException.class, () -> controller.updateUser(user, request));
    }

    @Test
    void updateUserBadBirthdayTest() {
        User user = getUser();
        controller.createUser(user, request);
        user.setBirthday(LocalDate.of(2200,1,1));
        assertThrows(ValidationException.class, () -> controller.updateUser(user, request));
    }

    private User getUser() {
        return User.builder()
                .email("mail@mail.ru")
                .login("dolore")
                .name("Nick Name")
                .birthday(LocalDate.of(1946,8,20))
                .build();
    }
}