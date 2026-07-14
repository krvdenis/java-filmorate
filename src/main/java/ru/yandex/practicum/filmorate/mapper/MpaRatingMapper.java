package ru.yandex.practicum.filmorate.mapper;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import ru.yandex.practicum.filmorate.dto.MpaRatingDto;
import ru.yandex.practicum.filmorate.model.MpaRating;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class MpaRatingMapper {

    public static MpaRatingDto mapToMpaDto(MpaRating mpaRating) {
        MpaRatingDto dto = null;

        if (mpaRating != null) {
            dto = new MpaRatingDto();
            dto.setId(mpaRating.getId());
            dto.setName(mpaRating.getName());
        }
        return dto;
    }

    public static MpaRating mapToMpa(MpaRatingDto mpaRatingDto) {
        MpaRating mpaRating = null;

        if (mpaRatingDto != null) {
            mpaRating = new MpaRating();
            mpaRating.setId(mpaRatingDto.getId());
            mpaRating.setName(mpaRatingDto.getName());
        }
        return mpaRating;
    }
}