package ru.yandex.practicum.filmorate.dal.storage;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import ru.yandex.practicum.filmorate.exception.InternalServerException;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.Collection;
import java.util.Optional;

@RequiredArgsConstructor
public class BaseDbStorage<T>{
    protected final JdbcTemplate jdbc;
    protected final RowMapper<T> mapper;

    protected Collection<T> findMany(String query, Object... params) {

        return jdbc.query(query, mapper, params);
    }

    protected Optional<T> findOne(String query, Object... params) {
        try {
            T result = jdbc.queryForObject(query, mapper, params);
            return Optional.ofNullable(result);
        } catch (EmptyResultDataAccessException ignored) {
            return Optional.empty();
        }
    }

    protected void update(String query, Object... params) {
        int rowUpdated = jdbc.update(query, params);
        if (rowUpdated == 0) {
            throw new InternalServerException("Не удалось обновить данные");
        }

    }

    protected long insert(String query, Object... params) {
        GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
        jdbc.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
            for (int idx = 0; idx < params.length; idx++) {
                ps.setObject(idx + 1, params[idx]);
            }
            return ps;
        }, keyHolder);
        Long id = keyHolder.getKeyAs(Long.class);

        if (id != null) {
            return id;
        } else {
            throw new InternalServerException("Не удалось сохранить данные");
        }
    }

    protected void insertWithoutGeneratedKey(String query, Object... params) {
        jdbc.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(query);
            for (int idx = 0; idx < params.length; idx++) {
                ps.setObject(idx + 1, params[idx]);
            }
            return ps;
        });
    }

    protected boolean delete(String query, Object... params) {
        int rowDeleted = jdbc.update(query, params);
        return rowDeleted > 0;
    }

    protected long count(String query, Object... params) {
        try {
            Long result = jdbc.queryForObject(query, Long.class, params);
            return result != null ? result : 0L;
        } catch (EmptyResultDataAccessException e) {
            return 0L;
        }
    }
}
