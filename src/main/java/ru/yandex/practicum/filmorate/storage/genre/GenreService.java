package ru.yandex.practicum.filmorate.storage.genre;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.dao.GenreStorageDb;

import java.util.List;

@Service
@AllArgsConstructor
public class GenreService {

    private final GenreStorageDb genreStorage;

    public List<Genre> getGenreList() {
        return genreStorage.getGenreList();
    }


    public Genre getGenreById(int id) {
        return genreStorage.getGenreById(id)
                .orElseThrow(() -> new NotFoundException("Жанр по id = " + id + " не найден."));
    }
}
