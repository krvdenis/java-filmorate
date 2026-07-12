package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dal.storage.MpaRatingDbStorage;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.MpaRating;

import java.util.Collection;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class MpaService {
    private final MpaRatingDbStorage mpaStorage;

    public Collection<MpaRating> findAllMpaRatings() {
        return mpaStorage.findAll();
    }

    public MpaRating findMpaRatingById(Long id) {
            if (id == null) {
                log.warn("Попытка найти MPA рейтинг без указания ID MPA рейтинга");
                throw new ValidationException("ID MPA рейтинга должен быть указан!");
            }

            Optional<MpaRating> mpaRatingOptional = mpaStorage.findById(id);
            if (mpaRatingOptional.isEmpty()) {
                throw new NotFoundException("MPA c ID " + id + " не найден!");
            }
            return mpaRatingOptional.get();

    }

    public MpaRating findMpaRatingByFilmId(Long filmId) {
       return mpaStorage.findMpaByFilmId(filmId).orElse(null);
    }
    //добавить сюда count mpa и заменить дальше на этот метод
}
