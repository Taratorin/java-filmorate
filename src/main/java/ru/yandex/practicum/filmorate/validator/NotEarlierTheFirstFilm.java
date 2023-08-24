package ru.yandex.practicum.filmorate.validator;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Constraint(validatedBy = NotEarlierTheFirstFilmValidator.class)
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface NotEarlierTheFirstFilm {
    String message() default
            "The date must be after 1895-28-12";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}