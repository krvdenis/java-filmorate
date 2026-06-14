package ru.yandex.practicum.filmorate.annotation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.time.LocalDate;

public class ReleaseDateValidator implements ConstraintValidator<NotBefore1895Dec28, LocalDate> {
    @Override
    public void initialize(NotBefore1895Dec28 constraintAnnotation) {

    }

    @Override
    public boolean isValid(LocalDate date, ConstraintValidatorContext constraintValidatorContext) {
        if (date == null) {
            return true;
        }
        LocalDate birthdayOfCinema = LocalDate.of(1895, 12, 28);
        return date.isAfter(birthdayOfCinema) || date.isEqual(birthdayOfCinema);
    }
}
