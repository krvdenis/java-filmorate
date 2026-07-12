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
    private final UserStorage userStorage; // работать через сервис
    private final GenreDbStorage genreStorage; // нужно будет убрать и работать через сервис
    private final GenreService genreService; // нужно будет убрать и работать через сервис
    private final MpaRatingDbStorage mpaRatingStorage;
    private final MpaService mpaService;// работать через сервис

    public FilmService(@Qualifier("filmDbStorage") FilmStorage filmStorage,
                       @Qualifier("userDbStorage") UserStorage userStorage,
                       GenreDbStorage genreStorage,
                       GenreService genreService,
                       MpaRatingDbStorage mpaRatingStorage,
                       MpaService mpaService) {
        this.filmStorage = filmStorage;
        this.userStorage = userStorage;
        this.genreStorage = genreStorage;
        this.genreService =genreService;
        this.mpaRatingStorage = mpaRatingStorage;
        this.mpaService = mpaService;
    }

    public Film createFilm(Film film) {
        if (film == null) {
            log.warn("Попытка добавить фильм без данных");
            throw new ValidationException("Невозможно добавить фильм без данных");
        }

        Long totalMpa = mpaRatingStorage.getTotalMpa();
        if (totalMpa == 0 || film.getMpa().getId() > totalMpa) {
            throw new NotFoundException("Общее количество mpa_rating_id = " + totalMpa);

        }

//        for (Genre genre : film.getGenres()) {
//            if (genre.getId() > genreStorage.totalGenres()) {
//                throw new NotFoundException("Общее количество жанров = " + genreStorage.totalGenres());
//            }
//        } // адаптировать как mpaRating а вроде и не надо

        log.debug("Попытка добавить новый фильм: {}", film);
        log.info("Пользователь добавил фильм с названием {} с ID {}", film.getName(), film.getId());
        film = filmStorage.createFilm(film);
        return film;
    }

    public Film updateFilm(Film newFilm) {
        if (newFilm.getId() == null) {
            log.warn("Отсутствует ID у объекта. Данные: {}", newFilm);
            throw new ValidationException("Id должен быть указан");
        }

        Long totalMpa = mpaRatingStorage.getTotalMpa();
        if (totalMpa == 0 || newFilm.getMpa().getId() > totalMpa) {
            throw new NotFoundException("Общее количество mpa_rating_id = " + totalMpa);
        } // ещё добавить проверку на жанры

        Film currentFilm = findFilmWithGenresAndMpaById(newFilm.getId());

        if (newFilm.getName() != null && !currentFilm.getName().equals(newFilm.getName())) { //стоит ограничения на null в таблице
            currentFilm.setName(newFilm.getName());
            log.info("Пользователь хочет изменить имя фильма с ID {} на {}", newFilm.getId(), newFilm.getName());

        }
        if (newFilm.getMpa() != null && currentFilm.getMpa() != null
                && !currentFilm.getMpa().getId().equals(newFilm.getMpa().getId())) {
                currentFilm.getMpa().setId(newFilm.getMpa().getId());
            log.info("Пользователь хочет изменить MPA рейтинг фильма с ID {} на {}", newFilm.getId(),
                    newFilm.getMpa());
        }

        if (newFilm.getDescription() != null && !currentFilm.getDescription().equals(newFilm.getDescription())) {
            currentFilm.setDescription(newFilm.getDescription());
            log.info("Пользователь хочет изменить описание фильма с ID {} на {}", newFilm.getId(),
                    newFilm.getDescription());
        }
        if (newFilm.getReleaseDate() != null && !currentFilm.getReleaseDate().equals(newFilm.getReleaseDate())) {
            currentFilm.setReleaseDate(newFilm.getReleaseDate());
            log.info("Пользователь хочет изменить дату выхода фильма с ID {} на {}", newFilm.getId(),
                    newFilm.getReleaseDate());
        }
        if (newFilm.getDuration() != currentFilm.getDuration()) {
            currentFilm.setDuration(newFilm.getDuration());
            log.info("Пользователь хочет изменить продолжительность фильма с ID {} на {}", newFilm.getId(),
                    newFilm.getDuration());
        }
    // ещё нужно будет учесть жанры
        log.debug("Попытка сохранить изменения в данные фильма: {}", newFilm);
        return filmStorage.updateFilm(currentFilm);
    }

    public Collection<Film> getAllFilms() {
        log.debug("Попытка получить список всех фильмов");
        Collection<Film> films = filmStorage.findAllFilms();
        log.info("Возвращается список из {} фильмов", films.size());
        return films;
    }

    public Film findFilmWithGenresAndMpaById(Long filmId) {
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
        Film film = filmOptional.get();
        film.setMpa(mpaService.findMpaRatingByFilmId(filmId));
        Collection<Genre> genres = genreService.findGenresByFilmId(film.getId());
        film.setGenres(new HashSet<>(genres));
        log.info("Отправлена информация о фильме: {}", filmOptional.get());
        return film;
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
        Optional<Film> filmOptional = filmStorage.findFilmById(filmId);
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
        Optional<Film> filmOptional = filmStorage.findFilmById(filmId);
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

//    public Genre createGenre(String name) {
//        return genreStorage.create(name);
//    } // а оно надо? Кажется нет

    //Если тесты проходят - удалить
//    public Collection<Genre> findAllGenre() {
//        return genreStorage.findAll();
//    }

    //если тесты проходят - удалить
//    public Genre findGenreById(Long genreId) {
//        if (genreId == null) {
//            log.warn("Попытка найти жанр без указания ID жанра");
//            throw new ValidationException("ID жанра должен быть указан!");
//        }
//
//        Optional<Genre> genreOptional = genreStorage.findById(genreId);
//        if (genreOptional.isEmpty()) {
//            throw new NotFoundException("Жанр c ID " + genreId + " не найден!");
//        }
//        return genreOptional.get();
//    }

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
