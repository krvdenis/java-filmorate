package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dal.storage.GenreDbStorage;
import ru.yandex.practicum.filmorate.dal.storage.MpaRatingDbStorage;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.dal.storage.FilmStorage;
import ru.yandex.practicum.filmorate.dal.storage.UserStorage;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.MpaRating;
import ru.yandex.practicum.filmorate.model.User;

import java.util.*;

@Service
@Slf4j
public class FilmService {
    private final FilmStorage filmStorage;
    private final UserStorage userStorage;
    private final GenreDbStorage genreStorage;
    private final MpaRatingDbStorage mpaRatingStorage;

    public FilmService(@Qualifier("filmDbStorage") FilmStorage filmStorage,
                       @Qualifier("userDbStorage") UserStorage userStorage,
                       GenreDbStorage genreStorage,
                       MpaRatingDbStorage mpaRatingStorage) {
        this.filmStorage = filmStorage;
        this.userStorage = userStorage;
        this.genreStorage = genreStorage;
        this.mpaRatingStorage = mpaRatingStorage;
    }

    public Film createFilm(Film film) {
        if (film == null) {
            log.warn("Попытка добавить фильм без данных");
            throw new ValidationException("Невозможно добавить фильм без данных");
        }
        log.debug("Попытка добавить новый фильм: {}", film);
        log.info("Пользователь добавил фильм с названием {} с ID {}", film.getName(), film.getId());
        return filmStorage.createFilm(film);
    }

    public Film updateFilm(Film newFilm) {
        if (newFilm.getId() == null) {
            log.warn("Отсутствует ID у объекта. Данные: {}", newFilm);
            throw new ValidationException("Id должен быть указан");
        }
        log.debug("Попытка внести изменения в данные фильма: {}", newFilm);
        return filmStorage.updateFilm(newFilm);
    }

    public Collection<Film> getAllFilms() {
        log.debug("Попытка получить список всех фильмов");
        Collection<Film> films = filmStorage.findAllFilms();
        log.info("Возвращается список из {} фильмов", films.size());
        return films;
    }

    public Film findFilmById(Long filmId) {
        log.debug("Попытка найти фильм: filmId={}", filmId);
        if (filmId == null) {
            log.warn("Попытка найти фильм без указания ID фильма");
            throw new ValidationException("ID фильма должен быть указан!");
        }

        Optional<Film> filmOptional = filmStorage.findFilmById(filmId);
        if (filmOptional.isEmpty()) {
            log.warn("Фильм с ID {} не найден", filmId);
            throw new NotFoundException("Фильм c ID " + filmId + " не найден!");
        }
        log.info("Пользователю отправлена информация о фильме: {}", filmOptional.get());
        return filmOptional.get();
    }

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
        Optional<User> userOptional = userStorage.findUserById(userId);
        if (userOptional.isEmpty()) {
            log.warn("Пользователь с ID {} не найден", userId);
            throw new NotFoundException("Пользователь c ID " + userId + " не найден!");
        }
        Optional<Film> filmOptional = filmStorage.findFilmById(userId);
        if (filmOptional.isEmpty()) {
            log.warn("Фильм с ID {} не найден", filmId);
            throw new NotFoundException("Фильм c ID " + filmId + " не найден!");
        }
        Film film = filmStorage.addLike(filmOptional.get(), userId);
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
        Optional<User> userOptional = userStorage.findUserById(userId);
        if (userOptional.isEmpty()) {
            log.warn("Пользователь с ID {} не найден", userId);
            throw new NotFoundException("Пользователь c ID " + userId + " не найден!");
        }
        Optional<Film> filmOptional = filmStorage.findFilmById(userId);
        if (filmOptional.isEmpty()) {
            log.warn("Фильм с ID {} не найден", filmId);
            throw new NotFoundException("Фильм c ID " + filmId + " не найден!");
        }

        filmStorage.deleteLike(filmOptional.get(), userId);
    }

    public Collection<Film> getCountMostPopularFilmsByLikes(int count) {
        log.debug("Запрос популярных фильмов: count={}", count);

        Collection<Film> popularFilms = filmStorage.findMostPopularFilmsByLikes(count);

        log.info("Найдено популярных фильмов: {}", popularFilms.size());
        return popularFilms;
    }

    public Genre createGenre(String name) {
        return genreStorage.create(name);
    }
    
    public Collection<Genre> findAllGenre() {
        return genreStorage.findAll();
    }
    
    public Genre findGenreById(Long genreId) {
        if (genreId == null) {
            log.warn("Попытка найти жанр без указания ID жанра");
            throw new ValidationException("ID жанра должен быть указан!");
        }
        
        Optional<Genre> genreOptional = genreStorage.findById(genreId);
        if (genreOptional.isEmpty()) {
            throw new NotFoundException("Жанр c ID " + genreId + " не найден!");
        }
        return genreOptional.get();
    }

    public Collection<MpaRating> findAllMpaRatings() {
        return mpaRatingStorage.findAll();
    }

    public MpaRating findMpaRatingById(Long mpaRatingId) {
        if (mpaRatingId == null) {
            log.warn("Попытка найти MPA рейтинг без указания ID MPA рейтинга");
            throw new ValidationException("ID MPA рейтинга должен быть указан!");
        }

        Optional<MpaRating> mpaRatingOptional = mpaRatingStorage.findById(mpaRatingId);
        if (mpaRatingOptional.isEmpty()) {
            throw new NotFoundException("Жанр c ID " + mpaRatingId + " не найден!");
        }
        return mpaRatingOptional.get();
    }
    
}
