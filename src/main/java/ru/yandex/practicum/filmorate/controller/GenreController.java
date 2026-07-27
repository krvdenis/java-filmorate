package ru.yandex.practicum.filmorate.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.filmorate.dto.GenreDto;
import ru.yandex.practicum.filmorate.service.GenreService;

import java.util.Collection;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/genres")
public class GenreController {
    private final GenreService genreService;

    @GetMapping()
    public Collection<GenreDto> findGenres() {
        log.info("Поступил запрос на список всех жанров");
        return genreService.findAllGenre();
    }

    @GetMapping("/{id}")

    public GenreDto findGenreById(@PathVariable Long id) {
        log.info("Поступил запрос на поиск жанра с ID: {}", id);
        return genreService.findGenreById(id);
    }
}