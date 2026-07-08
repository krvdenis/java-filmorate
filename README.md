# java-filmorate
Template repository for Filmorate project.


# SQL‑запросы для работы с базой данных

### 1. Работа с фильмами

#### Получить список всех фильмов

```sql
SELECT *
FROM film;
```
 
#### Найти фильм по ID

```sql
SELECT *
FROM film
WHERE id = ?;
```

#### Добавить новый фильм

```sql
INSERT INTO film (title, mpa_rating_id, release_date, description, duration)
VALUES (?, ?, ?, ?, ?);
```

#### Обновить данные фильма

```sql
UPDATE film SET title = ? WHERE id = ?;
```

#### Получить топ‑N популярных фильмов (по количеству лайков)

```sql
SELECT f.title
FROM film AS f
JOIN film_like AS fl ON f.id = fl.film_id
GROUP BY f.id, f.title
ORDER BY COUNT(fl.user_id) DESC
LIMIT ?;
```

### 2. Работа с жанрами

#### Добавить новый жанр

```sql
INSERT INTO genre (name) VALUES (?);
```

#### Привязать жанр к фильму

```sql
INSERT INTO film_genre (film_id, genre_id) VALUES (?, ?);
```

### 3. Работа с лайками

#### Поставить лайк фильму

```sql
INSERT INTO film_like (film_id, user_id) VALUES (?, ?);
 ```

#### Удалить лайк

```sql
DELETE FROM film_like WHERE film_id = ? AND user_id = ?;
```

### 4. Работа с пользователями

#### Получить список всех пользователей

```sql
SELECT * FROM user;
 ```

#### Найти пользователя по ID

```sql
SELECT * FROM user WHERE id = ?;
 ```

#### Добавить нового пользователя

```sql
INSERT INTO user (login, name, email, birthday) VALUES (?, ?, ?, ?);
 ```

#### Обновить данные пользователя

```sql
UPDATE user SET login = ?, email = ? WHERE id = ?;
```

### 5. Работа с дружбой

#### Добавить пользователя в друзья

```sql
INSERT INTO user_friend (user_id, friend_user_id) VALUES (?, ?);
 ```
#### Получить всех друзей пользователя
*По умолчанию возвращает только тех, кого пользователь добавил в друзья. (односторонняя дружба)*

```sql
SELECT friend_user_id FROM user_friend WHERE user_id = ?;
```

#### Удалить пользователя из друзей

```sql
DELETE FROM user_friend
WHERE user_id = ? AND friend_user_id = ?;
```
### 6. Поиск общих друзей

Вариант 1: через JOIN

```sql
SELECT DISTINCT uf1.friend_user_id AS common_friend_id
FROM user_friend AS uf1
JOIN user_friend AS uf2 ON uf1.friend_user_id = uf2.friend_user_id
WHERE uf1.user_id = ? AND uf2.user_id = ?;
```
Вариант 2: через INTERSECT

```sql
(SELECT friend_user_id FROM user_friend WHERE user_id = ?)
INTERSECT
(SELECT friend_user_id FROM user_friend WHERE user_id = ?);
```
