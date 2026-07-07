package ru.yandex.practicum.filmorate.storage;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.Collection;
import java.util.HashMap;

@Component("inMemoryFilmStorage")
@Slf4j
@Getter
public class InMemoryFilmStorage implements FilmStorage {
    private final HashMap<Long, Film> films = new HashMap<>();

    @Override
    public Collection<Film> findAll() {
        log.debug("Попытка получить список всех фильмов");
        log.info("Возвращается список из {} фильмов", films.size());
        return films.values();
    }

    @Override
    public Film create(Film film) {
        log.debug("Попытка добавить новый фильм: {}", film);
        if (film == null) {
            log.warn("Попытка добавить фильм без данных");
            throw new ValidationException("Невозможно добавить фильм без данных");
        }
        film.setId(getNextId());
        films.put(film.getId(), film);
        log.info("Пользователь добавил фильм с названием {} с ID {}", film.getTitle(), film.getId());
        return film;
    }

    @Override
    public Film update(Film newFilm) {
        log.debug("Попытка внести изменения в данные фильма: {}", newFilm);
        if (newFilm.getId() == null) {
            log.warn("Отсутствует ID у объекта. Данные: {}", newFilm);
            throw new ValidationException("Id должен быть указан");
        }
        if (films.containsKey(newFilm.getId())) {
            Film oldFilm = films.get(newFilm.getId());

            if (newFilm.getTitle() != null && !oldFilm.getTitle().equals(newFilm.getTitle())) {
                oldFilm.setTitle(newFilm.getTitle());
                log.info("Пользователь изменил имя фильма с ID {} на {}", newFilm.getId(), newFilm.getTitle());

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
