package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.dto.FilmDto;
import ru.yandex.practicum.filmorate.dto.NewFilmRequest;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.FilmService;

import java.util.Collection;

@RestController
@Slf4j
@RequestMapping("/films")
@RequiredArgsConstructor
public class FilmController {
    private final FilmService filmService;

    @GetMapping
    public Collection<FilmDto> findAll() {
        log.info("Поступил запрос на список всех фильмов");
        return filmService.findAllFilms();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public FilmDto create(@Valid @RequestBody NewFilmRequest newFilmRequest) {
        log.info("Поступил запрос на создание фильма: {}", newFilmRequest);
        return filmService.createFilm(newFilmRequest);
    }

    @PutMapping
    public FilmDto update(@Valid @RequestBody Film newFilm) {
        log.info("Поступил запрос на обновление фильма: {}", newFilm);
        return filmService.updateFilm(newFilm);
    }

    @GetMapping("/{id}")
    public FilmDto findFilmById(@PathVariable Long id) {
        log.info("Поступил запрос на поиск фильма с ID: {}", id);
        return filmService.findFilmWithGenresAndMpaById(id);
    }

    @PutMapping("/{id}/like/{userId}")
    public FilmDto addLike(@PathVariable Long id, @PathVariable Long userId) {
        log.info("Поступил запрос на добавления like к фильму с ID: {}", id);
        return filmService.addLike(userId, id);
    }

    @DeleteMapping("/{id}/like/{userId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteLike(@PathVariable Long id, @PathVariable Long userId) {
        log.info("Поступил запрос на удаление like у фильма с ID: {}", id);
        filmService.deleteLike(userId, id);
    }

    @GetMapping("/popular")
    public Collection<FilmDto> findCountPopularFilms(@RequestParam Integer count) {
        log.info("Поступил запрос на список из {} самых популярных фильмов", count);
        if (count == null) {
            return filmService.getCountMostPopularFilmsByLikes(10);
        } else {
            return filmService.getCountMostPopularFilmsByLikes(count);
        }
    }
}