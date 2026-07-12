package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dal.storage.GenreDbStorage;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Genre;

import java.util.Collection;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class GenreService {
    private final GenreDbStorage genreDbStorage;

    public Collection<Genre> findAllGenre() {
        return genreDbStorage.findAll();
    }

    public Genre findGenreById(Long id) {
        if (id == null) {
            log.warn("Попытка найти MPA рейтинг без указания ID MPA рейтинга");
            throw new ValidationException("ID MPA рейтинга должен быть указан!");
        }

        Optional<Genre> genreOptional = genreDbStorage.findById(id);
        if (genreOptional.isEmpty()) {
            throw new NotFoundException("Жанр c ID " + id + " не найден!");
        }
        return genreOptional.get();
    }
}
