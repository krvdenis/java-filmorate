package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.FilmStorage;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

@Service
@Slf4j
@RequiredArgsConstructor
public class FilmService {
    private final FilmStorage inMemoryFilmStorage;
    private final UserStorage inMemoryUserStorage;
    private final Comparator<Film> filmComparator = Comparator.comparing(Film::getTotalLikes).reversed();

    public Film addLike(Long userId, Long filmId) {
        log.debug("Попытка добавить like: userId={}, filmId={}", userId, filmId);
        if (userId == null) {
            log.warn("Попытка добавить like без указания ID пользователя");
            throw new ValidationException("ID пользователя должен быть указан!");
        }
        if (filmId == null) {
            log.warn("Попытка добавить like без указания ID фильма");
            throw new ValidationException("ID фильма должен быть указан!");
        }
        if (!inMemoryUserStorage.getUsers().containsKey(userId)) {
            log.warn("Пользователь с ID {} не существует", userId);
            throw new NotFoundException("Пользователь c ID " + userId + " не существует!");
        }
        if (!inMemoryFilmStorage.getFilms().containsKey(filmId)) {
            log.warn("Фильм с ID {} не существует", filmId);
            throw new NotFoundException("Фильм c ID " + filmId + " не существует!");
        }

        Film film = getFilm(filmId);
        Set<Long> likes = film.getLikes();
        likes.add(userId);

        log.info("Лайк добавлен: пользователь {} поставил like фильму {}", userId, filmId);
        return film;
    }

    public void deleteLike(Long userId, Long filmId) {
        log.debug("Попытка удалить like: userId={}, filmId={}", userId, filmId);
        if (userId == null) {
            log.warn("Попытка удалить like без указания ID пользователя");
            throw new ValidationException("ID пользователя должен быть указан!");
        }
        if (filmId == null) {
            log.warn("Попытка удалить like без указания ID фильма");
            throw new ValidationException("ID фильма должен быть указан!");
        }
        if (!inMemoryUserStorage.getUsers().containsKey(userId)) {
            log.warn("Пользователь с ID {} не существует", userId);
            throw new NotFoundException("Пользователь c ID " + userId + " не существует!");
        }
        if (!inMemoryFilmStorage.getFilms().containsKey(filmId)) {
            log.warn("Фильм с ID {} не существует", filmId);
            throw new NotFoundException("Фильм c ID " + filmId + " не существует!");
        }

        Set<Long> likes = getFilm(filmId).getLikes();
        boolean removed = likes.remove(userId);

        if (removed) {
            log.info("Лайк удалён: пользователь {} убрал лайк у фильма {}", userId, filmId);
        } else {
            log.warn("Попытка удалить отсутствующий лайк: пользователь {} не ставил лайк фильму {}", userId, filmId);
        }
    }

    public List<Film> getCountMostPopularFilmsByLikes(int count) {
        log.debug("Запрос популярных фильмов: count={}", count);

        List<Film> popularFilms = inMemoryFilmStorage.getFilms().values()
                .stream()
                .sorted(filmComparator)
                .limit(count)
                .toList();

        log.info("Найдено популярных фильмов: {}", popularFilms.size());
        return popularFilms;
    }

    public Collection<Film> getAllFilms() {
        return inMemoryFilmStorage.findAll();
    }

    public Film createFilm(Film film) {
        return inMemoryFilmStorage.create(film);
    }

    public Film updateFilm(Film newFilm) {
        return inMemoryFilmStorage.update(newFilm);
    }

    public Film findFilmById(Long filmId) {
        log.debug("Попытка найти фильм: filmId={}", filmId);
        if (filmId == null) {
            log.warn("Попытка найти фильм без указания ID фильма");
            throw new ValidationException("ID фильма должен быть указан!");
        }
        if (!inMemoryFilmStorage.getFilms().containsKey(filmId)) {
            log.warn("Фильм с ID {} не существует", filmId);
            throw new NotFoundException("Фильм c ID " + filmId + " не существует!");
        }

        log.info("Пользователю отправлена информация о фильме: {}", getFilm(filmId));
        return getFilm(filmId);
    }

    private Film getFilm(Long id) {
        return inMemoryFilmStorage.getFilms().get(id);
    }
}
