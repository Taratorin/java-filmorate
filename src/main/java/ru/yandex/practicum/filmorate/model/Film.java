package ru.yandex.practicum.filmorate.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Builder;
import lombok.Data;
import ru.yandex.practicum.filmorate.validator.NotEarlierTheFirstFilm;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

@Data
@Builder
public class Film {

    private int id;
    @NotBlank
    private String name;
    @Size(max = 200) @NotNull
    private String description;
    @NotEarlierTheFirstFilm
    private LocalDate releaseDate;
    @Min(1)
    private int duration;
    @NotNull
    private Mpa mpa;
    private LinkedHashSet<Genre> genres;
    private int likesCount;

}