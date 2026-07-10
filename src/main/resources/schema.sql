CREATE TABLE IF NOT EXISTS "user" (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    login VARCHAR(16) NOT NULL UNIQUE CHECK(TRIM(login) != '' AND LENGTH(login) BETWEEN 4 AND 16),
    name VARCHAR(50),
    email VARCHAR(50) NOT NULL UNIQUE CHECK(TRIM(email) != '' AND POSITION('@' IN email) > 0),
    birthday DATE
);

CREATE TABLE IF NOT EXISTS user_friend (
    user_id BIGINT REFERENCES "user" (id),
    friend_user_id BIGINT REFERENCES "user" (id),
    PRIMARY KEY (user_id, friend_user_id),
    CHECK (user_id != friend_user_id)
);

CREATE TABLE IF NOT EXISTS mpa_rating (
    mpa_rating_id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY CHECK (mpa_rating_id BETWEEN 1 AND 5),
    name VARCHAR(50) UNIQUE
);

CREATE TABLE IF NOT EXISTS film (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    name VARCHAR(120) NOT NULL CHECK(TRIM(name) != ''),
    mpa_rating_id BIGINT REFERENCES mpa_rating,
    release_date DATE CHECK (release_date >= '1895-12-28'),
    description VARCHAR(200),
    duration INTEGER CHECK (duration > 0)
);

CREATE TABLE IF NOT EXISTS film_like (
    film_id BIGINT REFERENCES film (id),
    user_id BIGINT REFERENCES "user" (id),
    PRIMARY KEY (film_id, user_id)
);

CREATE TABLE IF NOT EXISTS genre (
    genre_id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    name VARCHAR(50) UNIQUE
);

CREATE TABLE IF NOT EXISTS film_genre (
    film_id BIGINT REFERENCES film (id),
    genre_id BIGINT REFERENCES genre (genre_id),
    PRIMARY KEY (film_id, genre_id)
);