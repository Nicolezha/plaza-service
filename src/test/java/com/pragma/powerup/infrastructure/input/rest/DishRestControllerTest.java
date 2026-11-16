package com.pragma.powerup.infrastructure.input.rest;

import com.pragma.powerup.application.dto.request.DishRequestDto;
import com.pragma.powerup.application.dto.request.DishUpdateRequestDto;
import com.pragma.powerup.application.handler.IDishHandler;
import com.pragma.powerup.domain.exception.CategoryNotFoundException;
import com.pragma.powerup.domain.exception.DishNotFoundException;
import com.pragma.powerup.domain.exception.InvalidDishDataException;
import com.pragma.powerup.domain.exception.NotRestaurantOwnerException;
import com.pragma.powerup.domain.exception.RestaurantNotFoundException;
import com.pragma.powerup.infrastructure.exceptionhandler.ControllerAdvisor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static com.pragma.powerup.domain.util.DishConstants.*;
import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class DishRestControllerTest {

    private static final String URL = "/api/v1/dish";
    private static final String HEADER = "X-Requester-Id";

    private static final String VALID_BODY = """
            {
              "name": "Bandeja paisa",
              "price": 25000,
              "description": "Plato tipico",
              "imageUrl": "https://imagenes.com/bandeja.png",
              "categoryId": 3,
              "restaurantId": 10
            }
            """;

    @Mock
    private IDishHandler dishHandler;

    @InjectMocks
    private DishRestController dishRestController;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(dishRestController)
                .setControllerAdvice(new ControllerAdvisor())
                .build();
    }

    @Test
    void saveDishShouldReturn201() throws Exception {
        mockMvc.perform(post(URL).header(HEADER, 1L)
                        .contentType(MediaType.APPLICATION_JSON).content(VALID_BODY))
                .andExpect(status().isCreated());

        verify(dishHandler).saveDish(any(DishRequestDto.class), eq(1L));
    }

    @Test
    void saveDishShouldReturn400WhenARequiredFieldIsBlank() throws Exception {
        String body = VALID_BODY.replace("\"Bandeja paisa\"", "\"\"");

        mockMvc.perform(post(URL).header(HEADER, 1L)
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("name")));

        verifyNoInteractions(dishHandler);
    }

    @Test
    void saveDishShouldReturn400WhenPriceIsNotPositive() throws Exception {
        String body = VALID_BODY.replace("25000", "0");

        mockMvc.perform(post(URL).header(HEADER, 1L)
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("price")));

        verifyNoInteractions(dishHandler);
    }

    @Test
    void saveDishShouldReturn400WhenTheRequesterHeaderIsMissing() throws Exception {
        mockMvc.perform(post(URL)
                        .contentType(MediaType.APPLICATION_JSON).content(VALID_BODY))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString(HEADER)));

        verifyNoInteractions(dishHandler);
    }

    @Test
    void saveDishShouldReturn400WhenDomainRuleFails() throws Exception {
        doThrow(new InvalidDishDataException(PRICE_INVALID_MESSAGE))
                .when(dishHandler).saveDish(any(), any());

        mockMvc.perform(post(URL).header(HEADER, 1L)
                        .contentType(MediaType.APPLICATION_JSON).content(VALID_BODY))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(PRICE_INVALID_MESSAGE));
    }

    @Test
    void saveDishShouldReturn404WhenRestaurantNotFound() throws Exception {
        doThrow(new RestaurantNotFoundException(RESTAURANT_NOT_FOUND_MESSAGE))
                .when(dishHandler).saveDish(any(), any());

        mockMvc.perform(post(URL).header(HEADER, 1L)
                        .contentType(MediaType.APPLICATION_JSON).content(VALID_BODY))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value(RESTAURANT_NOT_FOUND_MESSAGE));
    }

    @Test
    void saveDishShouldReturn404WhenCategoryNotFound() throws Exception {
        doThrow(new CategoryNotFoundException(CATEGORY_NOT_FOUND_MESSAGE))
                .when(dishHandler).saveDish(any(), any());

        mockMvc.perform(post(URL).header(HEADER, 1L)
                        .contentType(MediaType.APPLICATION_JSON).content(VALID_BODY))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value(CATEGORY_NOT_FOUND_MESSAGE));
    }

    @Test
    void saveDishShouldReturn403WhenRequesterIsNotTheOwner() throws Exception {
        doThrow(new NotRestaurantOwnerException(NOT_RESTAURANT_OWNER_MESSAGE))
                .when(dishHandler).saveDish(any(), any());

        mockMvc.perform(post(URL).header(HEADER, 99L)
                        .contentType(MediaType.APPLICATION_JSON).content(VALID_BODY))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value(NOT_RESTAURANT_OWNER_MESSAGE));
    }

    private static final String UPDATE_URL = URL + "/5";

    private static final String VALID_UPDATE_BODY = """
            {
              "price": 30000,
              "description": "Nueva descripcion"
            }
            """;

    @Test
    void updateDishShouldReturn200() throws Exception {
        mockMvc.perform(patch(UPDATE_URL).header(HEADER, 1L)
                        .contentType(MediaType.APPLICATION_JSON).content(VALID_UPDATE_BODY))
                .andExpect(status().isOk());

        verify(dishHandler).updateDish(eq(5L), any(DishUpdateRequestDto.class), eq(1L));
    }

    @Test
    void updateDishShouldReturn400WhenPriceIsNotPositive() throws Exception {
        String body = VALID_UPDATE_BODY.replace("30000", "0");

        mockMvc.perform(patch(UPDATE_URL).header(HEADER, 1L)
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("price")));

        verifyNoInteractions(dishHandler);
    }

    @Test
    void updateDishShouldReturn400WhenDescriptionIsBlank() throws Exception {
        String body = VALID_UPDATE_BODY.replace("\"Nueva descripcion\"", "\"\"");

        mockMvc.perform(patch(UPDATE_URL).header(HEADER, 1L)
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("description")));

        verifyNoInteractions(dishHandler);
    }

    @Test
    void updateDishShouldReturn400WhenTheRequesterHeaderIsMissing() throws Exception {
        mockMvc.perform(patch(UPDATE_URL)
                        .contentType(MediaType.APPLICATION_JSON).content(VALID_UPDATE_BODY))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString(HEADER)));

        verifyNoInteractions(dishHandler);
    }

    @Test
    void updateDishShouldReturn404WhenDishNotFound() throws Exception {
        doThrow(new DishNotFoundException(DISH_NOT_FOUND_MESSAGE))
                .when(dishHandler).updateDish(any(), any(), any());

        mockMvc.perform(patch(UPDATE_URL).header(HEADER, 1L)
                        .contentType(MediaType.APPLICATION_JSON).content(VALID_UPDATE_BODY))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value(DISH_NOT_FOUND_MESSAGE));
    }

    @Test
    void updateDishShouldReturn403WhenRequesterIsNotTheOwner() throws Exception {
        doThrow(new NotRestaurantOwnerException(NOT_RESTAURANT_OWNER_MESSAGE))
                .when(dishHandler).updateDish(any(), any(), any());

        mockMvc.perform(patch(UPDATE_URL).header(HEADER, 99L)
                        .contentType(MediaType.APPLICATION_JSON).content(VALID_UPDATE_BODY))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value(NOT_RESTAURANT_OWNER_MESSAGE));
    }
}
