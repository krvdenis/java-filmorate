package ru.yandex.practicum.filmorate.dal.storage;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.*;

@Component("inMemoryFilmStorage")
@Slf4j
@Getter
public class InMemoryFilmStorage implements FilmStorage {
    private final HashMap<Long, Film> films = new HashMap<>();
    private final Comparator<Film> filmComparator = Comparator.comparing(Film::getTotalLikes).reversed();

    @Override
    public Film createFilm(Film film) {
        film.setId(getNextId());
        films.put(film.getId(), film);
        return film;
    }

    @Override
    public Film updateFilm(Film newFilm) {

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

    @Override
    public Collection<Film> findAllFilms() {
        return films.values();
    }

    @Override
    public Optional<Film> findFilmById(Long filmId) {
        return Optional.ofNullable(films.get(filmId));
    }

    @Override
    public Film addLike(Film film, Long userId) {
        Set<Long> likes = film.getLikes();
        likes.add(userId);
        return film;
    }


    @Override
    public void deleteLike(Film film, Long userId) {
        Set<Long> likes = film.getLikes();
        boolean removed = likes.remove(userId);
        if (removed) {
            log.info("Лайк удалён: пользователь {} убрал лайк у фильма {}", userId, film.getId());
        } else {
            log.warn("Попытка удалить отсутствующий лайк: пользователь {} не ставил лайк фильму {}", userId,
                    film.getId());
        }
    }

    @Override
    public Collection<Film> findMostPopularFilmsByLikes(int count) {
        return films.values()
                .stream()
                .sorted(filmComparator)
                .limit(count)
                .toList();
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
