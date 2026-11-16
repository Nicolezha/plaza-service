package com.pragma.powerup.infrastructure.input.rest;

import com.pragma.powerup.application.dto.request.RestaurantRequestDto;
import com.pragma.powerup.application.handler.IRestaurantHandler;
import com.pragma.powerup.domain.exception.OwnerNotFoundException;
import com.pragma.powerup.domain.exception.RestaurantAlreadyExistsException;
import com.pragma.powerup.domain.exception.UserNotOwnerException;
import com.pragma.powerup.domain.exception.UserServiceUnavailableException;
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

import static com.pragma.powerup.domain.util.RestaurantConstants.*;
import static org.hamcrest.Matchers.containsString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class RestaurantRestControllerTest {

    private static final String URL = "/api/v1/restaurant";

    private static final String VALID_BODY = """
            {
              "name": "El Corral 24",
              "nit": "900123456",
              "address": "Calle 10 # 5-20",
              "phone": "+573005698325",
              "logoUrl": "https://logos.com/corral.png",
              "ownerId": 1
            }
            """;

    @Mock
    private IRestaurantHandler restaurantHandler;

    @InjectMocks
    private RestaurantRestController restaurantRestController;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(restaurantRestController)
                .setControllerAdvice(new ControllerAdvisor())
                .build();
    }

    @Test
    void saveRestaurantShouldReturn201() throws Exception {
        mockMvc.perform(post(URL).contentType(MediaType.APPLICATION_JSON).content(VALID_BODY))
                .andExpect(status().isCreated());

        verify(restaurantHandler).saveRestaurant(any(RestaurantRequestDto.class));
    }

    @Test
    void saveRestaurantShouldReturn400WhenARequiredFieldIsBlank() throws Exception {
        String body = VALID_BODY.replace("\"El Corral 24\"", "\"\"");

        mockMvc.perform(post(URL).contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("name")));

        verifyNoInteractions(restaurantHandler);
    }

    @Test
    void saveRestaurantShouldReturn400WhenOwnerIdIsMissing() throws Exception {
        String body = VALID_BODY.replace("\"ownerId\": 1", "\"ownerId\": null");

        mockMvc.perform(post(URL).contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("ownerId")));

        verifyNoInteractions(restaurantHandler);
    }

    @Test
    void saveRestaurantShouldReturn409WhenNitAlreadyExists() throws Exception {
        doThrow(new RestaurantAlreadyExistsException(NIT_ALREADY_EXISTS_MESSAGE))
                .when(restaurantHandler).saveRestaurant(any());

        mockMvc.perform(post(URL).contentType(MediaType.APPLICATION_JSON).content(VALID_BODY))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value(NIT_ALREADY_EXISTS_MESSAGE));
    }

    @Test
    void saveRestaurantShouldReturn404WhenOwnerNotFound() throws Exception {
        doThrow(new OwnerNotFoundException(OWNER_NOT_FOUND_MESSAGE))
                .when(restaurantHandler).saveRestaurant(any());

        mockMvc.perform(post(URL).contentType(MediaType.APPLICATION_JSON).content(VALID_BODY))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value(OWNER_NOT_FOUND_MESSAGE));
    }

    @Test
    void saveRestaurantShouldReturn400WhenUserIsNotOwner() throws Exception {
        doThrow(new UserNotOwnerException(USER_NOT_OWNER_MESSAGE))
                .when(restaurantHandler).saveRestaurant(any());

        mockMvc.perform(post(URL).contentType(MediaType.APPLICATION_JSON).content(VALID_BODY))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(USER_NOT_OWNER_MESSAGE));
    }

    @Test
    void  saveRestaurantShouldReturn503WhenUserServiceUnavailable() throws Exception {
        doThrow(new UserServiceUnavailableException(USER_SERVICE_UNAVAILABLE_MESSAGE))
                .when(restaurantHandler).saveRestaurant(any());

        mockMvc.perform(post(URL).contentType(MediaType.APPLICATION_JSON).content(VALID_BODY))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.message").value(USER_SERVICE_UNAVAILABLE_MESSAGE));
    }
}