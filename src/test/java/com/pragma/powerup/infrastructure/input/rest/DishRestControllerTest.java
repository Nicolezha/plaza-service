package com.pragma.powerup.infrastructure.input.rest;

import com.pragma.powerup.application.dto.request.DishRequestDto;
import com.pragma.powerup.application.dto.request.DishStatusRequestDto;
import com.pragma.powerup.application.dto.request.DishUpdateRequestDto;
import com.pragma.powerup.application.dto.request.PageRequestDto;
import com.pragma.powerup.application.dto.response.DishListResponseDto;
import com.pragma.powerup.application.dto.response.PageResponseDto;
import com.pragma.powerup.application.handler.IDishHandler;
import com.pragma.powerup.domain.exception.CategoryNotFoundException;
import com.pragma.powerup.domain.exception.DishNotFoundException;
import com.pragma.powerup.domain.exception.InvalidDishDataException;
import com.pragma.powerup.domain.exception.InvalidPaginationException;
import com.pragma.powerup.domain.exception.NotRestaurantOwnerException;
import com.pragma.powerup.domain.exception.RestaurantNotFoundException;
import com.pragma.powerup.infrastructure.exceptionhandler.ControllerAdvisor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static com.pragma.powerup.domain.util.DishConstants.*;
import static com.pragma.powerup.domain.util.PaginationConstants.SIZE_INVALID_MESSAGE;
import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class DishRestControllerTest {

    private static final String URL = "/api/v1/dish";

    private static final String STATUS_URL = "/api/v1/dish/5/status";

    private static final String VALID_STATUS_BODY = "{\"active\": false}";

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

    private Authentication authenticatedAs(Long userId) {
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                "owner@gmail.com", null, List.of(new SimpleGrantedAuthority("ROLE_OWNER")));
        authentication.setDetails(userId);
        return authentication;
    }

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(dishRestController)
                .setControllerAdvice(new ControllerAdvisor())
                .build();
    }

    @Test
    void saveDishShouldReturn201() throws Exception {
        mockMvc.perform(post(URL).principal(authenticatedAs(1L))
                        .contentType(MediaType.APPLICATION_JSON).content(VALID_BODY))
                .andExpect(status().isCreated());

        verify(dishHandler).saveDish(any(DishRequestDto.class), eq(1L));
    }

    @Test
    void saveDishShouldReturn400WhenARequiredFieldIsBlank() throws Exception {
        String body = VALID_BODY.replace("\"Bandeja paisa\"", "\"\"");

        mockMvc.perform(post(URL).principal(authenticatedAs(1L))
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("name")));

        verifyNoInteractions(dishHandler);
    }

    @Test
    void saveDishShouldReturn400WhenPriceIsNotPositive() throws Exception {
        String body = VALID_BODY.replace("25000", "0");

        mockMvc.perform(post(URL).principal(authenticatedAs(1L))
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("price")));

        verifyNoInteractions(dishHandler);
    }

    @Test
    void saveDishShouldReturn400WhenDomainRuleFails() throws Exception {
        doThrow(new InvalidDishDataException(PRICE_INVALID_MESSAGE))
                .when(dishHandler).saveDish(any(), any());

        mockMvc.perform(post(URL).principal(authenticatedAs(1L))
                        .contentType(MediaType.APPLICATION_JSON).content(VALID_BODY))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(PRICE_INVALID_MESSAGE));
    }

    @Test
    void saveDishShouldReturn404WhenRestaurantNotFound() throws Exception {
        doThrow(new RestaurantNotFoundException(RESTAURANT_NOT_FOUND_MESSAGE))
                .when(dishHandler).saveDish(any(), any());

        mockMvc.perform(post(URL).principal(authenticatedAs(1L))
                        .contentType(MediaType.APPLICATION_JSON).content(VALID_BODY))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value(RESTAURANT_NOT_FOUND_MESSAGE));
    }

    @Test
    void saveDishShouldReturn404WhenCategoryNotFound() throws Exception {
        doThrow(new CategoryNotFoundException(CATEGORY_NOT_FOUND_MESSAGE))
                .when(dishHandler).saveDish(any(), any());

        mockMvc.perform(post(URL).principal(authenticatedAs(1L))
                        .contentType(MediaType.APPLICATION_JSON).content(VALID_BODY))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value(CATEGORY_NOT_FOUND_MESSAGE));
    }

    @Test
    void saveDishShouldReturn403WhenRequesterIsNotTheOwner() throws Exception {
        doThrow(new NotRestaurantOwnerException(NOT_RESTAURANT_OWNER_MESSAGE))
                .when(dishHandler).saveDish(any(), any());

        mockMvc.perform(post(URL).principal(authenticatedAs(99L))
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
        mockMvc.perform(patch(UPDATE_URL).principal(authenticatedAs(1L))
                        .contentType(MediaType.APPLICATION_JSON).content(VALID_UPDATE_BODY))
                .andExpect(status().isOk());

        verify(dishHandler).updateDish(eq(5L), any(DishUpdateRequestDto.class), eq(1L));
    }

    @Test
    void updateDishShouldReturn400WhenPriceIsNotPositive() throws Exception {
        String body = VALID_UPDATE_BODY.replace("30000", "0");

        mockMvc.perform(patch(UPDATE_URL).principal(authenticatedAs(1L))
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("price")));

        verifyNoInteractions(dishHandler);
    }

    @Test
    void updateDishShouldReturn400WhenDescriptionIsBlank() throws Exception {
        String body = VALID_UPDATE_BODY.replace("\"Nueva descripcion\"", "\"\"");

        mockMvc.perform(patch(UPDATE_URL).principal(authenticatedAs(1L))
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("description")));

        verifyNoInteractions(dishHandler);
    }

    @Test
    void updateDishShouldReturn404WhenDishNotFound() throws Exception {
        doThrow(new DishNotFoundException(DISH_NOT_FOUND_MESSAGE))
                .when(dishHandler).updateDish(any(), any(), any());

        mockMvc.perform(patch(UPDATE_URL).principal(authenticatedAs(1L))
                        .contentType(MediaType.APPLICATION_JSON).content(VALID_UPDATE_BODY))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value(DISH_NOT_FOUND_MESSAGE));
    }

    @Test
    void updateDishShouldReturn403WhenRequesterIsNotTheOwner() throws Exception {
        doThrow(new NotRestaurantOwnerException(NOT_RESTAURANT_OWNER_MESSAGE))
                .when(dishHandler).updateDish(any(), any(), any());

        mockMvc.perform(patch(UPDATE_URL).principal(authenticatedAs(99L))
                        .contentType(MediaType.APPLICATION_JSON).content(VALID_UPDATE_BODY))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value(NOT_RESTAURANT_OWNER_MESSAGE));
    }

    @Test
    void updateDishStatusShouldReturn200() throws Exception {
        mockMvc.perform(patch(STATUS_URL).principal(authenticatedAs(1L))
                        .contentType(MediaType.APPLICATION_JSON).content(VALID_STATUS_BODY))
                .andExpect(status().isOk());

        verify(dishHandler).updateDishStatus(eq(5L), any(DishStatusRequestDto.class), eq(1L));
    }

    @Test
    void updateDishStatusShouldReturn400WhenActiveIsMissing() throws Exception {
        mockMvc.perform(patch(STATUS_URL).principal(authenticatedAs(1L))
                        .contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("active")));

        verifyNoInteractions(dishHandler);
    }

    @Test
    void updateDishStatusShouldReturn404WhenDishNotFound() throws Exception {
        doThrow(new DishNotFoundException(DISH_NOT_FOUND_MESSAGE))
                .when(dishHandler).updateDishStatus(any(), any(), any());

        mockMvc.perform(patch(STATUS_URL).principal(authenticatedAs(1L))
                        .contentType(MediaType.APPLICATION_JSON).content(VALID_STATUS_BODY))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value(DISH_NOT_FOUND_MESSAGE));
    }

    @Test
    void updateDishStatusShouldReturn403WhenRequesterIsNotTheOwner() throws Exception {
        doThrow(new NotRestaurantOwnerException(NOT_RESTAURANT_OWNER_MESSAGE))
                .when(dishHandler).updateDishStatus(any(), any(), any());

        mockMvc.perform(patch(STATUS_URL).principal(authenticatedAs(99L))
                        .contentType(MediaType.APPLICATION_JSON).content(VALID_STATUS_BODY))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value(NOT_RESTAURANT_OWNER_MESSAGE));
    }

    private DishListResponseDto dishListResponse() {
        DishListResponseDto dto = new DishListResponseDto();
        dto.setId(5L);
        dto.setName("Bandeja paisa");
        dto.setPrice(25000);
        dto.setDescription("Plato tipico");
        dto.setImageUrl("https://imagenes.com/bandeja.png");
        dto.setCategoryId(3L);
        return dto;
    }

    @Test
    void getDishesByRestaurantShouldReturn200WithPaginatedDishes() throws Exception {
        PageResponseDto<DishListResponseDto> response =
                new PageResponseDto<>(List.of(dishListResponse()), 1, 2, 3L, 2);

        when(dishHandler.getDishesByRestaurant(eq(10L), eq(3L), any(PageRequestDto.class)))
                .thenReturn(response);

        mockMvc.perform(get(URL).param("restaurantId", "10").param("categoryId", "3")
                        .param("page", "1").param("size", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(5))
                .andExpect(jsonPath("$.content[0].name").value("Bandeja paisa"))
                .andExpect(jsonPath("$.content[0].price").value(25000))
                .andExpect(jsonPath("$.content[0].description").value("Plato tipico"))
                .andExpect(jsonPath("$.content[0].imageUrl").value("https://imagenes.com/bandeja.png"))
                .andExpect(jsonPath("$.content[0].categoryId").value(3))
                .andExpect(jsonPath("$.content[0].active").doesNotExist())
                .andExpect(jsonPath("$.page").value(1))
                .andExpect(jsonPath("$.size").value(2))
                .andExpect(jsonPath("$.totalElements").value(3))
                .andExpect(jsonPath("$.totalPages").value(2));

        ArgumentCaptor<PageRequestDto> captor = ArgumentCaptor.forClass(PageRequestDto.class);
        verify(dishHandler).getDishesByRestaurant(eq(10L), eq(3L), captor.capture());
        assertEquals(1, captor.getValue().getPage());
        assertEquals(2, captor.getValue().getSize());
    }

    @Test
    void getDishesByRestaurantShouldSendNullCategoryAndDefaultPaginationWhenParamsAreMissing() throws Exception {
        when(dishHandler.getDishesByRestaurant(eq(10L), any(), any(PageRequestDto.class)))
                .thenReturn(new PageResponseDto<>(List.of(), 0, 10, 0L, 0));

        mockMvc.perform(get(URL).param("restaurantId", "10"))
                .andExpect(status().isOk());

        ArgumentCaptor<Long> categoryCaptor = ArgumentCaptor.forClass(Long.class);
        ArgumentCaptor<PageRequestDto> pageCaptor = ArgumentCaptor.forClass(PageRequestDto.class);
        verify(dishHandler).getDishesByRestaurant(eq(10L), categoryCaptor.capture(), pageCaptor.capture());
        assertNull(categoryCaptor.getValue());
        assertEquals(0, pageCaptor.getValue().getPage());
        assertEquals(10, pageCaptor.getValue().getSize());
    }

    @Test
    void getDishesByRestaurantShouldReturn400WhenRestaurantIdIsMissing() throws Exception {
        mockMvc.perform(get(URL))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(dishHandler);
    }

    @Test
    void getDishesByRestaurantShouldReturn404WhenRestaurantNotFound() throws Exception {
        when(dishHandler.getDishesByRestaurant(any(), any(), any()))
                .thenThrow(new RestaurantNotFoundException(RESTAURANT_NOT_FOUND_MESSAGE));

        mockMvc.perform(get(URL).param("restaurantId", "99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value(RESTAURANT_NOT_FOUND_MESSAGE));
    }

    @Test
    void getDishesByRestaurantShouldReturn400WhenPaginationIsInvalid() throws Exception {
        when(dishHandler.getDishesByRestaurant(any(), any(), any()))
                .thenThrow(new InvalidPaginationException(SIZE_INVALID_MESSAGE));

        mockMvc.perform(get(URL).param("restaurantId", "10").param("size", "0"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(SIZE_INVALID_MESSAGE));
    }
}
