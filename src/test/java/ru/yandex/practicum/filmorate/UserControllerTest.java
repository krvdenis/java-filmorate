package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@SpringBootTest
@AutoConfigureMockMvc
public class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void getUsers_ShouldReturnOk() throws Exception {
        mockMvc.perform(get("/users")).andExpect(status().isOk());
    }

    @Test
    void createUser_WithOutRequestBody_ShouldReturnBadRequest() throws Exception {
        mockMvc.perform(post("/users")
                        .content("{}")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

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
                .andExpect(status().isBadRequest());
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
                .andExpect(status().isBadRequest());
    }
}
