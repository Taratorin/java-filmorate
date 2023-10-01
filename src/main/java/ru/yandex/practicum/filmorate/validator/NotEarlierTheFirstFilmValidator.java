package ru.yandex.practicum.filmorate.validator;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.time.LocalDate;
import java.util.Date;

public class NotEarlierTheFirstFilmValidator implements
        ConstraintValidator<NotEarlierTheFirstFilm, LocalDate> {

    private LocalDate firstFilmDate;

    @Override
    public void initialize(NotEarlierTheFirstFilm constraintAnnotation) {
        firstFilmDate = LocalDate.of(1895, 12, 28);
    }

    @Override
    public boolean isValid(LocalDate date, ConstraintValidatorContext constraintValidatorContext) {
        return date != null && !date.isBefore(firstFilmDate);
    }
}