# java-filmorate
**[Ссылка на ER diagram проекта](/Filmorate-ER-diagram.svg)**
![](/Filmorate-ER-diagram.svg)
Пояснения к схеме:

* Получение всех пользователей:

SELECT *

FROM User;

* Получение всех фильмов:

SELECT *

FROM Film;

* Получение списка друзей пользователя:

SELECT *

FROM Friends

WHERE if_approved = true

AND user_id = 0;

> в примере 0 - это id пользователя, для которго запрашивается список друзей;


* Получение списка общих друзей:

SELECT DISTINCT friend_id

FROM (SELECT *

FROM Friends

WHERE if_approved = true

AND user_id IN (0, 2));

> в примере 0 и 2 - это id пользователей, у которых находим общих друзей;


* Получение списка наиболее популярных фильмов

SELECT film_id,

COUNT(user_id) AS likes
      
FROM Likes

GROUP BY film_id

ORDER BY likes DESC

LIMIT 10;

> в примере 10 - количество наиболее популярных фильмов;
