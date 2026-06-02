package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.yandex.practicum.filmorate.controller.FilmController;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@WebMvcTest(FilmController.class)
public class FilmControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Test
    void createUser_WithValidRequestBody_ShouldReturnOk() throws Exception {
        String validFilmJson = "{" +
                "  \"name\": \"XKJt2kJZt3OZ5di\"," +
                "  \"description\": \"YU3lweQJsGINhjvwp5lJBOhp30RLD4KQS5UiCJDGSp5KDWpHo5\"," +
                "  \"releaseDate\": \"1964-03-21\"," +
                "  \"duration\": 82" +
                "}";

        mockMvc.perform(post("/films")
                        .content(validFilmJson)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void createUser_WithBlankName_ShouldReturnBadRequest() throws Exception {
        String filmJsonWithBlankName = "{" +
                "  \"name\": \"\"," +
                "  \"description\": \"YU3lweQJsGINhjvwp5lJBOhp30RLD4KQS5UiCJDGSp5KDWpHo5\"," +
                "  \"releaseDate\": \"1964-03-21\"," +
                "  \"duration\": 82" +
                "}";

        mockMvc.perform(post("/films")
                        .content(filmJsonWithBlankName)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createUser_WithNullName_ShouldReturnBadRequest() throws Exception {
        String filmJsonWithNullName = "{" +
                "  \"description\": \"YU3lweQJsGINhjvwp5lJBOhp30RLD4KQS5UiCJDGSp5KDWpHo5\"," +
                "  \"releaseDate\": \"1964-03-21\"," +
                "  \"duration\": 82" +
                "}";

        mockMvc.perform(post("/films")
                        .content(filmJsonWithNullName)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createUser_WithDescription200Chars_ShouldReturnBadRequest() throws Exception {
        String filmJsonDescriptionWith200Chars = "YU3lweQJsGINhjvwp5lJBOhp30RLD4KQS5UiCJDGSp5KDWpHo5safsdgdvjkdsg35fdljfaflj3r" +
                "lkasffladlfjlvjzxvljaskfjlYU3lweQJsGINhjvwp5lJBOhp30RLD4KQS5UiCJDGSp5KDWpHo5safsdgdvjkdsgasd34rfafa" +
                "sasddasdasdsadasdasd12das";

        String filmJsonWithDescription200Chars = "{" +
                "  \"name\": \"XKJt2kJZt3OZ5di\"," +
                "  \"description\": \"" + filmJsonDescriptionWith200Chars + "\"," +
                "  \"releaseDate\": \"1964-03-21\"," +
                "  \"duration\": 82" +
                "}";

        mockMvc.perform(post("/films")
                        .content(filmJsonWithDescription200Chars)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void createUser_WithDescriptionOver200Chars_ShouldReturnBadRequest() throws Exception {
        String filmJsonDescriptionWith201Chars = "YU3lweQJsGINhjvwp5lJBOhp30RLD4KQS5UiCJDGSp5KDWpHo5safsdgdvjkdsg35fdljfaflj3r" +
                "lkasffladlfjlvjzxvljaskfjlYU3lweQJsGINhjvwp5lJBOhp30RLD4KQS5UiCJDGSp5KDWpHo5safsdgdvjkdsgasd34rfafa" +
                "sasddasdasdsadasdasd12das1";
        String filmJsonWithDescriptionOver200Chars = "{" +
                "  \"name\": \"XKJt2kJZt3OZ5di\"," +
                "  \"description\": \"" + filmJsonDescriptionWith201Chars + "\"," +
                "  \"releaseDate\": \"1964-03-21\"," +
                "  \"duration\": 82" +
                "}";

        mockMvc.perform(post("/films")
                        .content(filmJsonWithDescriptionOver200Chars)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createUser_WithMinimumAcceptableReleaseDate_ShouldReturnOk() throws Exception {
        String validFilmJson = "{" +
                "  \"name\": \"XKJt2kJZt3OZ5di\"," +
                "  \"description\": \"YU3lweQJsGINhjvwp5lJBOhp30RLD4KQS5UiCJDGSp5KDWpHo5\"," +
                "  \"releaseDate\": \"1895-12-28\"," +
                "  \"duration\": 82" +
                "}";

        mockMvc.perform(post("/films")
                        .content(validFilmJson)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void createUser_WithBeforeMinimumAcceptableReleaseDate_ShouldReturnOk() throws Exception {
        String validFilmJson = "{" +
                "  \"name\": \"XKJt2kJZt3OZ5di\"," +
                "  \"description\": \"YU3lweQJsGINhjvwp5lJBOhp30RLD4KQS5UiCJDGSp5KDWpHo5\"," +
                "  \"releaseDate\": \"1895-12-27\"," +
                "  \"duration\": 82" +
                "}";

        mockMvc.perform(post("/films")
                        .content(validFilmJson)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Дата релиза — не раньше 28 декабря 1895 " +
                        "года"));
    }

    @Test
    void createFilm_WithOneDuration_ShouldReturnBadRequest() throws Exception {
        String filmJsonWithOneDuration = "{" +
                "  \"name\": \"XKJt2kJZt3OZ5di\"," +
                "  \"description\": \"YU3lweQJsGINhjvwp5lJBOhp30RLD4KQS5UiCJDGSp5KDWpHo5\"," +
                "  \"releaseDate\": \"2000-12-27\"," +
                "  \"duration\": 1" +
                "}";

        mockMvc.perform(post("/films")
                        .content(filmJsonWithOneDuration)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void createFilm_WithZeroDuration_ShouldReturnBadRequest() throws Exception {
        String filmJsonWithZeroDuration = "{" +
                "  \"name\": \"XKJt2kJZt3OZ5di\"," +
                "  \"description\": \"YU3lweQJsGINhjvwp5lJBOhp30RLD4KQS5UiCJDGSp5KDWpHo5\"," +
                "  \"releaseDate\": \"2000-12-27\"," +
                "  \"duration\": 0" +
                "}";

        mockMvc.perform(post("/films")
                        .content(filmJsonWithZeroDuration)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createFilm_WithNegativeDuration_ShouldReturnBadRequest() throws Exception {
        String filmJsonWithNegativeDuration = "{" +
                "  \"name\": \"XKJt2kJZt3OZ5di\"," +
                "  \"description\": \"YU3lweQJsGINhjvwp5lJBOhp30RLD4KQS5UiCJDGSp5KDWpHo5\"," +
                "  \"releaseDate\": \"2000-12-27\"," +
                "  \"duration\": -1" +
                "}";

        mockMvc.perform(post("/films")
                        .content(filmJsonWithNegativeDuration)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }
}