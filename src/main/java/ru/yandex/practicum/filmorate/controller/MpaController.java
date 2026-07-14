package ru.yandex.practicum.filmorate.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.filmorate.dto.MpaRatingDto;
import ru.yandex.practicum.filmorate.service.MpaService;

import java.util.Collection;

@RestController
@Slf4j
@RequestMapping("/mpa")
@RequiredArgsConstructor
public class MpaController {
    private final MpaService mpaService;

    @GetMapping()
    public Collection<MpaRatingDto> findMpaRatings() {
        log.info("Поступил запрос на список всех MPA ");
        return mpaService.findAllMpaRatings();
    }

    @GetMapping("/{id}")
    public MpaRatingDto findMpaRatingById(@PathVariable Long id) {
        log.info("Поступил запрос на поиск MPA с ID: {}", id);
        return mpaService.findMpaRatingById(id);
    }
}