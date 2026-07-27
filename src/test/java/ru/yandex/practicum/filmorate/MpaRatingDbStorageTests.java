package ru.yandex.practicum.filmorate;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import ru.yandex.practicum.filmorate.dal.MpaRatingDbStorage;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.MpaRating;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class MpaRatingDbStorageTests {
    private final MpaRatingDbStorage mpaRatingDbStorage;

    @Test
    public void testFindMpaRatingById_ShouldReturnExpectedName() {
        MpaRating mpaRating = mpaRatingDbStorage.findById(1L).orElseThrow(() ->
                new NotFoundException("Рейтинг не найден"));
        assertThat(mpaRating).hasFieldOrPropertyWithValue("name", "G");
    }

    @Test
    public void testFindMpaRatings_ShouldReturnExpectedTotalMpa() {
        List<MpaRating> mpaRatings = new ArrayList<>(mpaRatingDbStorage.findAll());
        assertThat(mpaRatings).hasSize(5);
        assertThat(mpaRatings)
                .extracting("name")
                .containsExactlyInAnyOrder("G", "PG", "PG-13", "R", "NC-17");
    }
}
