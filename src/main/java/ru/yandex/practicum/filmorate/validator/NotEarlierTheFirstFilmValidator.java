package ru.yandex.practicum.filmorate.validator;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.time.LocalDate;

public class NotEarlierTheFirstFilmValidator implements
        ConstraintValidator<NotEarlierTheFirstFilm, LocalDate> {

    private LocalDate firstFilmDate;

    @Override
    public void initialize(NotEarlierTheFirstFilm constraintAnnotation) {
        firstFilmDate = LocalDate.of(1895, 12, 27);
    }

    @Override
    public boolean isValid(LocalDate localDate, ConstraintValidatorContext constraintValidatorContext) {
        return localDate == null || localDate.isAfter(firstFilmDate);
    }

}
