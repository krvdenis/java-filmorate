package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.Collection;
import java.util.HashMap;

@RestController
@RequestMapping("/films")
public class FilmController {
    private static final Logger log = LoggerFactory.getLogger(FilmController.class);
    private final HashMap<Long, Film> films = new HashMap<>();

    @GetMapping
    public Collection<Film> findAll() {
        log.info("Пользователю отправлен список всех фильмов");
        return films.values();
    }

    @PostMapping
    public Film create(@Valid @RequestBody Film film) {
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

        if (films.containsKey(newFilm.getId())) {
            Film oldFilm = films.get(newFilm.getId());

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
