package ru.yandex.practicum.filmorate.dal;

import ru.yandex.practicum.filmorate.model.Film;

import java.util.Collection;
import java.util.Optional;

public interface FilmStorage {

    Film createFilm(Film film);

    Film updateFilm(Film newFilm);

    Collection<Film> findAllFilms();

    Optional<Film> findFilmById(Long filmId);

    Film addLike(Film film, Long userId);

    void deleteLike(Film film, Long userId);

    Collection<Film> getMostPopularFilmsByLikes(int count);
}