package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dal.GenreDbStorage;
import ru.yandex.practicum.filmorate.dto.GenreDto;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.mapper.GenreMapper;
import ru.yandex.practicum.filmorate.model.Genre;

import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class GenreService {
    private final GenreDbStorage genreDbStorage;

    public Collection<GenreDto> findAllGenre() {
        log.debug("Попытка получить список всех жанров");

        Collection<GenreDto> genreDtos = genreDbStorage.findAll().stream()
                .map(GenreMapper::mapToGenreDto)
                .collect(Collectors.toList());

        log.info("Возвращается список из всех жанров в количестве: {}", genreDtos.size());
        return genreDtos;
    }

    public GenreDto findGenreById(Long id) {
        log.debug("Попытка найти жанр: ID = {}", id);
        if (id == null) {
            log.warn("Попытка найти жанр рейтинг без указания ID жанра");
            throw new ValidationException("ID MPA рейтинга должен быть указан!");
        }

        Optional<Genre> genreOptional = genreDbStorage.findById(id);
        if (genreOptional.isEmpty()) {
            log.info("");
            throw new NotFoundException("Жанр c ID " + id + " не найден!");
        }
        log.info("Отправлена информация о жанре: {}", genreOptional.get());
        return GenreMapper.mapToGenreDto(genreOptional.get());
    }

    public Collection<GenreDto> findGenresByFilmId(Long filmId) {
        log.debug("Попытка найти список жанров по ID фильма: ID = {}", filmId);

        Collection<Genre> genres = genreDbStorage.findGenresByFilmId(filmId);

        log.info("Возвращается список из {} жанров для фильма с ID {}", genres.size(), filmId);
        return genres.stream().map(GenreMapper::mapToGenreDto).collect(Collectors.toList());
    }

    public Long findTotalNumberGenres() {
        log.debug("Попытка найти общее количество жанров");
        Long totalGenres = genreDbStorage.getTotalNumberGenres();
        log.info("Отправлена информация о количестве жанров: {}", totalGenres);
        return totalGenres;
    }
}