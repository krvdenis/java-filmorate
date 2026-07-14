package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dal.FilmStorage;
import ru.yandex.practicum.filmorate.dto.FilmDto;
import ru.yandex.practicum.filmorate.dto.NewFilmRequest;
import ru.yandex.practicum.filmorate.dto.UserDto;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.mapper.FilmMapper;
import ru.yandex.practicum.filmorate.mapper.GenreMapper;
import ru.yandex.practicum.filmorate.mapper.MpaRatingMapper;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;

import java.util.Collection;
import java.util.HashSet;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Slf4j
public class FilmService {
    private final FilmStorage filmStorage;
    private final UserService userService;
    private final GenreService genreService;
    private final MpaService mpaService;

    public FilmService(@Qualifier("filmDbStorage") FilmStorage filmStorage,
                       UserService userService,
                       GenreService genreService,
                       MpaService mpaService) {
        this.filmStorage = filmStorage;
        this.userService = userService;
        this.genreService = genreService;
        this.mpaService = mpaService;
    }

    public FilmDto createFilm(NewFilmRequest newFilmRequest) {
        log.debug("Попытка добавить новый фильм: {}", newFilmRequest);
        if (newFilmRequest == null) {
            log.warn("Попытка добавить фильм без данных");
            throw new ValidationException("Невозможно добавить фильм без данных");
        }
        Film film = FilmMapper.mapToFilm(newFilmRequest);
        validateGenreAndMpa(film);

        Film createdfilm = filmStorage.createFilm(film);
        createdfilm.setMpa(MpaRatingMapper.mapToMpa(mpaService.findMpaRatingByFilmId(createdfilm.getId())));
        createdfilm.setGenres(new HashSet<>(genreService.findGenresByFilmId(createdfilm.getId()).stream()
                .map(GenreMapper::mapToGenre)
                .collect(Collectors.toList()))
        );
        log.info("Пользователь добавил фильм с названием {} с ID {}", createdfilm.getName(), createdfilm.getId());
        return FilmMapper.mapToFilmDto(createdfilm);
    }

    public FilmDto updateFilm(Film newFilm) {
        if (newFilm.getId() == null) {
            log.warn("Отсутствует ID у объекта. Данные: {}", newFilm);
            throw new ValidationException("Id должен быть указан");
        }
        log.debug("Попытка внести изменения в данные фильма с ID: {}", newFilm.getId());
        validateGenreAndMpa(newFilm);

        FilmDto currentFilmDto = findFilmWithGenresAndMpaById(newFilm.getId());
        Film currentFilm = FilmMapper.mapToFilmFromDto(currentFilmDto);
        boolean isNameChanged = !currentFilm.getName().equals(newFilm.getName());
        boolean isMpaChanged = !currentFilm.getMpa().getId().equals(newFilm.getMpa().getId());
        boolean isDescriptionChanged = !currentFilm.getDescription().equals(newFilm.getDescription());
        boolean isReleaseDateChanged = !currentFilm.getReleaseDate().equals(newFilm.getReleaseDate());
        boolean isDurationChanged = newFilm.getDuration() != currentFilm.getDuration();
        boolean isGenresChanged = !currentFilm.getGenres().equals(newFilm.getGenres());

        if (newFilm.getName() != null && isNameChanged) { //стоит ограничения на null в таблице
            currentFilm.setName(newFilm.getName());
            log.info("Пользователь хочет изменить имя фильма с ID {} на {}", newFilm.getId(), newFilm.getName());

        }

        if (newFilm.getMpa() != null && currentFilm.getMpa() != null && isMpaChanged) {
            currentFilm.getMpa().setId(newFilm.getMpa().getId());
            log.info("Пользователь хочет изменить MPA рейтинг фильма с ID {} на {}", newFilm.getId(),
                    newFilm.getMpa());
        }

        if (newFilm.getDescription() != null && isDescriptionChanged) {
            currentFilm.setDescription(newFilm.getDescription());
            log.info("Пользователь хочет изменить описание фильма с ID {} на {}", newFilm.getId(),
                    newFilm.getDescription());
        }

        if (newFilm.getReleaseDate() != null && isReleaseDateChanged) {
            currentFilm.setReleaseDate(newFilm.getReleaseDate());
            log.info("Пользователь хочет изменить дату выхода фильма с ID {} на {}", newFilm.getId(),
                    newFilm.getReleaseDate());
        }

        if (isDurationChanged) {
            currentFilm.setDuration(newFilm.getDuration());
            log.info("Пользователь хочет изменить продолжительность фильма с ID {} на {}", newFilm.getId(),
                    newFilm.getDuration());
        }

        if (newFilm.getGenres() != null && currentFilm.getGenres() != null && isGenresChanged) {
            log.info("Пользователь хочет изменить список жанров фильма с ID {} на {}", newFilm.getId(),
                    newFilm.getGenres());
            currentFilm.getGenres().addAll(newFilm.getGenres());
        }

        log.debug("Попытка сохранить изменения в данные фильма: {}", newFilm);
        FilmDto filmDto = FilmMapper.mapToFilmDto(filmStorage.updateFilm(currentFilm));
        log.info("Данные фильма с ID {} успешно обновлены", filmDto.getId());
        return filmDto;
    }

    public Collection<FilmDto> findAllFilms() {
        log.debug("Попытка получить список всех фильмов");

        Collection<FilmDto> filmDtos = filmStorage.findAllFilms().stream()
                .map(FilmMapper::mapToFilmDto)
                .collect(Collectors.toList());

        log.info("Возвращается список из {} фильмов", filmDtos.size());
        return filmDtos;
    }

    public FilmDto findFilmWithGenresAndMpaById(Long filmId) {
        log.debug("Попытка найти фильм: ID = {}", filmId);
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
        log.info("Отправлена информация о фильме: {}", filmOptional.get());
        return FilmMapper.mapToFilmDto(film);
    }

    public FilmDto addLike(Long userId, Long filmId) {
        log.debug("Попытка добавить like: userId={}, filmId={}", userId, filmId);
        if (userId == null) {
            log.warn("Попытка добавить like без указания ID пользователя");
            throw new ValidationException("ID пользователя должен быть указан!");
        }

        if (filmId == null) {
            log.warn("Попытка добавить like без указания ID фильма");
            throw new ValidationException("ID фильма должен быть указан!");
        }

        UserDto userdto = userService.findUserById(userId);
        FilmDto filmDto = findFilmWithGenresAndMpaById(filmId);
        Film film = filmStorage.addLike(FilmMapper.mapToFilmFromDto(filmDto), userdto.getId());
        log.info("Лайк добавлен: пользователь {} поставил like фильму с ID {}", userId, filmId);
        return FilmMapper.mapToFilmDto(film);
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

        UserDto user = userService.findUserById(userId);
        FilmDto filmDto = findFilmWithGenresAndMpaById(filmId);

        filmStorage.deleteLike(FilmMapper.mapToFilmFromDto(filmDto), user.getId());
    }

    public Collection<FilmDto> getCountMostPopularFilmsByLikes(int count) {
        log.debug("Попытка получить список популярных фильмов: count={}", count);

        Collection<FilmDto> popularFilms = filmStorage.getMostPopularFilmsByLikes(count).stream()
                .map(FilmMapper::mapToFilmDto)
                .collect(Collectors.toList());

        log.info("Найдено популярных фильмов: {}", popularFilms.size());
        return popularFilms;
    }

    private void validateGenreAndMpa(Film film) {
        Long totalMpa = mpaService.findTotalNumberMpa();
        if (film.getMpa() != null && (totalMpa == 0 || film.getMpa().getId() > totalMpa)) {
            log.warn("Попытка добавить MPA, которого нет в списке");
            throw new NotFoundException("MPA с id = " + film.getMpa().getId() + " нет в списке MPA.");
        }

        Long totalGenre = genreService.findTotalNumberGenres();
        for (Genre genre : film.getGenres()) {
            if (genre != null && (totalGenre == 0 || genre.getId() > totalGenre)) {
                log.warn("Попытка добавить жанр, которого нет в списке");
                throw new NotFoundException("Жанра с id = " + genre.getId() + " нет в списке жанров.");
            }
        }
    }
}