INSERT INTO PUBLIC.GENRES (GENRE_ID, GENRE_TITLE)
VALUES
    (1, 'Комедия'),
    (2, 'Драма'),
    (3, 'Мультфильм'),
    (4, 'Триллер'),
    (5, 'Документальный'),
    (6, 'Боевик');

INSERT INTO PUBLIC.RATINGS (RATING_ID, RATING_NAME)
VALUES
    (1, 'G'),
    (2, 'PG'),
    (3, 'PG-13'),
    (4, 'R'),
    (5, 'NC-17');

--INSERT INTO PUBLIC.USERS (USER_ID, EMAIL, LOGIN, NAME, BIRTHDAY)
--            VALUES(1, 'first@some.com', 'first', 'first', '2021-12-12');
--INSERT INTO PUBLIC.USERS (USER_ID, EMAIL, LOGIN, NAME, BIRTHDAY)
--            VALUES(2, 'second@some.com', 'second', 'second', '2010-12-12');
--INSERT INTO PUBLIC.USERS (USER_ID, EMAIL, LOGIN, NAME, BIRTHDAY)
--            VALUES(3, 'third@some.com', 'third', 'third', '2012-12-12');
--
--INSERT INTO PUBLIC.FILMS (FILM_ID, TITLE, DESCRIPTION, RELEASE_DATE, DURATION, RATING_ID)
--				VALUES(1, 'First film', 'Description of first film', '1960-12-5', 120, 1);
--INSERT INTO PUBLIC.FILMS (FILM_ID, TITLE, DESCRIPTION, RELEASE_DATE, DURATION, RATING_ID)
--				VALUES(2, 'Second film', 'Description of second film', '1990-12-5', 200, 2);
--INSERT INTO PUBLIC.FILMS (FILM_ID, TITLE, DESCRIPTION, RELEASE_DATE, DURATION, RATING_ID)
--				VALUES(3, 'Third film', 'Description of third film', '2002-12-5', 20, 3);