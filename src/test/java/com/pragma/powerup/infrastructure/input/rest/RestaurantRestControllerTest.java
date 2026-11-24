package com.pragma.powerup.infrastructure.input.rest;

import com.pragma.powerup.application.dto.request.PageRequestDto;
import com.pragma.powerup.application.dto.request.RestaurantRequestDto;
import com.pragma.powerup.application.dto.response.PageResponseDto;
import com.pragma.powerup.application.dto.response.RestaurantListResponseDto;
import com.pragma.powerup.application.handler.IRestaurantHandler;
import com.pragma.powerup.domain.exception.InvalidPaginationException;
import com.pragma.powerup.domain.exception.OwnerNotFoundException;
import com.pragma.powerup.domain.exception.RestaurantAlreadyExistsException;
import com.pragma.powerup.domain.exception.UserNotOwnerException;
import com.pragma.powerup.domain.exception.UserServiceUnavailableException;
import com.pragma.powerup.infrastructure.exceptionhandler.ControllerAdvisor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static com.pragma.powerup.domain.util.PaginationConstants.SIZE_INVALID_MESSAGE;
import static com.pragma.powerup.domain.util.RestaurantConstants.*;
import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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

    @Test
    void getRestaurantsShouldReturn200WithPaginatedNameAndLogoOnly() throws Exception {
        RestaurantListResponseDto dto = new RestaurantListResponseDto();
        dto.setName("El Corral 24");
        dto.setLogoUrl("https://logos.com/corral.png");
        PageResponseDto<RestaurantListResponseDto> response =
                new PageResponseDto<>(List.of(dto), 1, 2, 3L, 2);

        when(restaurantHandler.getRestaurants(any(PageRequestDto.class))).thenReturn(response);

        mockMvc.perform(get(URL).param("page", "1").param("size", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].name").value("El Corral 24"))
                .andExpect(jsonPath("$.content[0].logoUrl").value("https://logos.com/corral.png"))
                .andExpect(jsonPath("$.content[0].nit").doesNotExist())
                .andExpect(jsonPath("$.page").value(1))
                .andExpect(jsonPath("$.size").value(2))
                .andExpect(jsonPath("$.totalElements").value(3))
                .andExpect(jsonPath("$.totalPages").value(2));

        ArgumentCaptor<PageRequestDto> captor = ArgumentCaptor.forClass(PageRequestDto.class);
        verify(restaurantHandler).getRestaurants(captor.capture());
        assertEquals(1, captor.getValue().getPage());
        assertEquals(2, captor.getValue().getSize());
    }

    @Test
    void getRestaurantsShouldUseDefaultPaginationWhenParamsAreMissing() throws Exception {
        when(restaurantHandler.getRestaurants(any(PageRequestDto.class)))
                .thenReturn(new PageResponseDto<>(List.of(), 0, 10, 0L, 0));

        mockMvc.perform(get(URL))
                .andExpect(status().isOk());

        ArgumentCaptor<PageRequestDto> captor = ArgumentCaptor.forClass(PageRequestDto.class);
        verify(restaurantHandler).getRestaurants(captor.capture());
        assertEquals(0, captor.getValue().getPage());
        assertEquals(10, captor.getValue().getSize());
    }

    @Test
    void getRestaurantsShouldReturn400WhenPaginationIsInvalid() throws Exception {
        when(restaurantHandler.getRestaurants(any(PageRequestDto.class)))
                .thenThrow(new InvalidPaginationException(SIZE_INVALID_MESSAGE));

        mockMvc.perform(get(URL).param("size", "0"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(SIZE_INVALID_MESSAGE));
    }
}