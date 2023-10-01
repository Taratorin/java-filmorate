//package ru.yandex.practicum.filmorate.model;
//
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//
//import javax.validation.ConstraintViolation;
//import javax.validation.Validation;
//import javax.validation.Validator;
//import javax.validation.ValidatorFactory;
//import java.time.LocalDate;
//import java.util.Set;
//
//import static org.junit.jupiter.api.Assertions.assertFalse;
//import static org.junit.jupiter.api.Assertions.assertTrue;
//
//class UserTest {
//
//    private Validator validator;
//
//    @BeforeEach
//    public void setUp() {
//        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
//        validator = factory.getValidator();
//    }
//
//    @Test
//    public void correctUserShouldPassValidation() {
//        User user = getUser();
//        Set<ConstraintViolation<User>> violations = validator.validate(user);
//        assertTrue(violations.isEmpty());
//    }
//
//    @Test
//    public void invalidUserLoginShouldFailValidation() {
//        User user = getUser();
//        user.setLogin("Qdf ioj");
//        Set<ConstraintViolation<User>> violations = validator.validate(user);
//        assertFalse(violations.isEmpty());
//    }
//
//    @Test
//    public void invalidUserEmailShouldFailValidation() {
//        User user = getUser();
//        user.setEmail("mail@.ru");
//        Set<ConstraintViolation<User>> violations = validator.validate(user);
//        assertFalse(violations.isEmpty());
//    }
//
//    @Test
//    public void invalidUserBirthdayShouldFailValidation() {
//        User user = getUser();
//        user.setBirthday(LocalDate.of(2200, 1, 1));
//        Set<ConstraintViolation<User>> violations = validator.validate(user);
//        assertFalse(violations.isEmpty());
//    }
//
//    private User getUser() {
//        return User.builder()
//                .email("mail@mail.ru")
//                .login("dolore")
//                .name("Nick Name")
//                .birthday(LocalDate.of(1946, 8, 20))
//                .build();
//    }
//}