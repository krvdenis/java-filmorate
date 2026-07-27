package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dal.MpaRatingDbStorage;
import ru.yandex.practicum.filmorate.dto.MpaRatingDto;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.mapper.MpaRatingMapper;
import ru.yandex.practicum.filmorate.model.MpaRating;

import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class MpaService {
    private final MpaRatingDbStorage mpaStorage;

    public Collection<MpaRatingDto> findAllMpaRatings() {
        log.debug("Попытка получить список всех MPA");
        Collection<MpaRatingDto> mpaRatingsDto = mpaStorage.findAll().stream()
                .map(MpaRatingMapper::mapToMpaDto)
                .collect(Collectors.toList());
        log.info("Возвращается список из {} MPA", mpaRatingsDto.size());
        return mpaRatingsDto;
    }

    public MpaRatingDto findMpaRatingById(Long id) {
        log.debug("Попытка найти MPA: ID = {}", id);
        if (id == null) {
            log.warn("Попытка найти MPA рейтинг без указания ID MPA рейтинга");
            throw new ValidationException("ID MPA рейтинга должен быть указан!");
        }

        Optional<MpaRating> mpaRatingOptional = mpaStorage.findById(id);
        if (mpaRatingOptional.isEmpty()) {
            throw new NotFoundException("MPA c ID " + id + " не найден!");
        }
        log.info("Отправлена информация о MPA: {}", mpaRatingOptional.get());
        return MpaRatingMapper.mapToMpaDto(mpaRatingOptional.get());

    }

    public MpaRatingDto findMpaRatingByFilmId(Long filmId) {
        log.debug("Попытка найти MPA по ID фильма: ID = {}", filmId);
        MpaRating mpaRating = mpaStorage.findMpaByFilmId(filmId).orElse(null);
        log.info("Отправлена информация о MPA: {} для фильма с ID: {}", mpaRating, filmId);
        return MpaRatingMapper.mapToMpaDto(mpaRating);
    }

    public Long findTotalNumberMpa() {
        log.debug("Попытка найти общее количество MPA");
        Long totalMpa = mpaStorage.getTotalNumberMpa();
        log.info("Отправлена информация о количестве жанров: {}", totalMpa);
        return totalMpa;
    }
}