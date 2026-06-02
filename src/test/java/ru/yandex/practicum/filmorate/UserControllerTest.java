package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.yandex.practicum.filmorate.controller.UserController;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@WebMvcTest(UserController.class)
public class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void createUser_WithValidRequestBody_ShouldReturnOk() throws Exception {
        String validUserJson = "{" +
                "  \"name\": \"Alan Medhurst\"," +
                "  \"login\": \"Fp4Biqd4gl\"," +
                "  \"email\": \"Reba.Collier@yahoo.com\"," +
                "  \"birthday\": \"1988-03-08\"" +
                "}";

        mockMvc.perform(post("/users")
                        .content(validUserJson)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void createUser_WithInvalidEmail_ShouldReturnBadRequest() throws Exception {
        String invalidUserJson = "{" +
                "  \"name\": \"Alan Medhurst\"," +
                "  \"login\": \"Fp4Biqd4gl\"," +
                "  \"email\": \"Reba.Collier\"," +
                "  \"birthday\": \"1988-03-08\"" +
                "}"; // Нет символа @ и домена

        mockMvc.perform(post("/users")
                        .content(invalidUserJson)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createUser_WithBlankEmail_ShouldReturnBadRequest() throws Exception {
        String userJsonWithBlankEmail = "{" +
                "  \"name\": \"Alan Medhurst\"," +
                "  \"login\": \"Fp4Biqd4gl\"," +
                "  \"email\": \"\"," +
                "  \"birthday\": \"1988-03-08\"" +
                "}";

        mockMvc.perform(post("/users")
                        .content(userJsonWithBlankEmail)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createUser_WithNullEmail_ShouldReturnBadRequest() throws Exception {
        String userJsonWithNullEmail = "{" +
                "  \"name\": \"Alan Medhurst\"," +
                "  \"login\": \"Fp4Biqd4gl\"," +
                "  \"birthday\": \"1988-03-08\"" +
                "}";

        mockMvc.perform(post("/users")
                        .content(userJsonWithNullEmail)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createUser_WithLoginContainingSpaces_ShouldReturnBadRequest() throws Exception {
        String userJsonWithSpacesInLogin = "{" +
                "  \"name\": \"Alan Medhurst\"," +
                "  \"login\": \"Fp4Biq d4gl\"," +
                "  \"email\": \"Reba.Collier@yahoo.com\"," +
                "  \"birthday\": \"1988-03-08\"" +
                "}";

        mockMvc.perform(post("/users")
                        .content(userJsonWithSpacesInLogin)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("В логине не должно быть пробелов"));
    }

    @Test
    void createUser_WithBlankLogin_ShouldReturnBadRequest() throws Exception {
        String userJsonWithBlankLogin = "{" +
                "  \"name\": \"Alan Medhurst\"," +
                "  \"login\": \"\"," +
                "  \"email\": \"Reba.Collier@yahoo.com\"," +
                "  \"birthday\": \"1988-03-08\"" +
                "}";

        mockMvc.perform(post("/users")
                        .content(userJsonWithBlankLogin)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createUser_WithNullLogin_ShouldReturnBadRequest() throws Exception {
        String userJsonWithBlankLogin = "{" +
                "  \"name\": \"Alan Medhurst\"," +
                "  \"email\": \"Reba.Collier@yahoo.com\"," +
                "  \"birthday\": \"1988-03-08\"" +
                "}";

        mockMvc.perform(post("/users")
                        .content(userJsonWithBlankLogin)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createUser_WithFutureBirthday_ShouldReturnBadRequest() throws Exception {
        String userJsonWithFutureBirthday = "{" +
                "  \"name\": \"Alan Medhurst\"," +
                "  \"login\": \"Fp4Biqd4gl\"," +
                "  \"email\": \"Reba.Collier@yahoo.com\"," +
                "  \"birthday\": \"2039-03-08\"" +
                "}";

        mockMvc.perform(post("/users")
                        .content(userJsonWithFutureBirthday)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Дата рождения не может быть в будущем"));
    }
}
