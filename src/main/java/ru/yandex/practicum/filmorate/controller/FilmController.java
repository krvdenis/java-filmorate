package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;
import java.util.Collection;
import java.util.HashMap;

@RestController
@RequestMapping("/films")
public class FilmController {
    private static final LocalDate MIN_RELEASE_DATE = LocalDate.of(1895, 12, 28);
    private static final Logger log = LoggerFactory.getLogger(FilmController.class);
    private final HashMap<Long, Film> films = new HashMap<>();

    @GetMapping
    public Collection<Film> findAll() {
        log.info("Пользователю отправлен список всех фильмов");
        return films.values();
    }

    @PostMapping
    public Film create(@Valid @RequestBody Film film) {
        boolean isReleaseDateBeforeMinAllowed = film.getReleaseDate() != null &&
                film.getReleaseDate().isBefore(MIN_RELEASE_DATE);

        if (isReleaseDateBeforeMinAllowed) {
            log.warn("Дата релиза фильма некорректна: {}. Фильм: {}, минимальная допустимая дата: {}",
                    film.getReleaseDate(), film, MIN_RELEASE_DATE);
            throw new ValidationException("Дата релиза — не раньше 28 декабря 1895 года");
        }

        film.setId(getNextId());
        films.put(film.getId(), film);
        log.info("Пользователь добавил фильм с названием {} с ID {}", film.getName(), film.getId());
        return film;
    }

    @PutMapping
    public Film update(@Valid @RequestBody Film newFilm) {
        if (newFilm.getId() == null) {
            log.warn("Отсутствует ID у объекта. Данные: {}", newFilm);
            throw new ValidationException("Id должен быть указан");
        }

        boolean isReleaseDateBeforeMinAllowed = newFilm.getReleaseDate() != null &&
                newFilm.getReleaseDate().isBefore(MIN_RELEASE_DATE);

        if (films.containsKey(newFilm.getId())) {
            Film oldFilm = films.get(newFilm.getId());

            if (isReleaseDateBeforeMinAllowed) {
                log.warn("Попытка изменить дату релиза фильма на некорректную: {}. Фильм: {}," +
                                " минимальная допустимая дата: {}", newFilm.getReleaseDate(),
                        newFilm, MIN_RELEASE_DATE);
                throw new ValidationException("Дата релиза — не раньше 28 декабря 1895 года");
            }

            if (newFilm.getName() != null && !oldFilm.getName().equals(newFilm.getName())) {
                oldFilm.setName(newFilm.getName());
                log.info("Пользователь изменил имя фильма с ID {} на {}", newFilm.getId(), newFilm.getName());

            }

            if (newFilm.getDescription() != null && !oldFilm.getDescription().equals(newFilm.getDescription())) {
                oldFilm.setDescription(newFilm.getDescription());
                log.info("Пользователь изменил описание фильма с ID {} на {}", newFilm.getId(),
                        newFilm.getDescription());
            }

            if (newFilm.getReleaseDate() != null && !oldFilm.getReleaseDate().equals(newFilm.getReleaseDate())) {
                oldFilm.setReleaseDate(newFilm.getReleaseDate());
                log.info("Пользователь изменил дату выхода фильма с ID {} на {}", newFilm.getId(),
                        newFilm.getReleaseDate());
            }

            if (oldFilm.getDuration() != newFilm.getDuration()) {
                oldFilm.setDuration(newFilm.getDuration());
                log.info("Пользователь изменил продолжительность фильма с ID {} на {}", newFilm.getId(),
                        newFilm.getDuration());
            }
            return oldFilm;
        }
        log.error("Фильм не найден. ID: {}", newFilm.getId());
        throw new NotFoundException("Фильм с id = " + newFilm.getId() + " не найден");
    }

    private long getNextId() {
        long currentMaxId = films.keySet()
                .stream()
                .mapToLong(id -> id)
                .max()
                .orElse(0);

        return ++currentMaxId;
    }

}
