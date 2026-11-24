package com.pragma.powerup.infrastructure.input.rest;

import com.pragma.powerup.application.dto.request.OrderRequestDto;
import com.pragma.powerup.application.handler.IOrderHandler;
import com.pragma.powerup.domain.exception.DishNotFoundException;
import com.pragma.powerup.domain.exception.InvalidOrderDataException;
import com.pragma.powerup.domain.exception.OrderInProgressException;
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

import static com.pragma.powerup.domain.util.DishConstants.DISH_NOT_FOUND_MESSAGE;
import static com.pragma.powerup.domain.util.DishConstants.RESTAURANT_NOT_FOUND_MESSAGE;
import static com.pragma.powerup.domain.util.OrderConstants.DISH_NOT_FROM_RESTAURANT_MESSAGE;
import static com.pragma.powerup.domain.util.OrderConstants.ORDER_IN_PROGRESS_MESSAGE;
import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class OrderRestControllerTest {

    private static final String URL = "/api/v1/order";

    private static final String VALID_BODY = """
            {
              "restaurantId": 10,
              "dishes": [
                { "dishId": 5, "quantity": 2 },
                { "dishId": 6, "quantity": 1 }
              ]
            }
            """;

    @Mock
    private IOrderHandler orderHandler;

    @InjectMocks
    private OrderRestController orderRestController;

    private MockMvc mockMvc;

    private Authentication authenticatedAs(Long userId) {
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                "client@gmail.com", null, List.of(new SimpleGrantedAuthority("ROLE_CLIENT")));
        authentication.setDetails(userId);
        return authentication;
    }

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(orderRestController)
                .setControllerAdvice(new ControllerAdvisor())
                .build();
    }

    @Test
    void createOrderShouldReturn201AndPassTheBodyAndClientId() throws Exception {
        mockMvc.perform(post(URL).principal(authenticatedAs(7L))
                        .contentType(MediaType.APPLICATION_JSON).content(VALID_BODY))
                .andExpect(status().isCreated());

        ArgumentCaptor<OrderRequestDto> captor = ArgumentCaptor.forClass(OrderRequestDto.class);
        verify(orderHandler).createOrder(captor.capture(), eq(7L));
        OrderRequestDto sent = captor.getValue();
        assertEquals(10L, sent.getRestaurantId());
        assertEquals(2, sent.getDishes().size());
        assertEquals(5L, sent.getDishes().get(0).getDishId());
        assertEquals(2, sent.getDishes().get(0).getQuantity());
    }

    @Test
    void createOrderShouldReturn400WhenRestaurantIdIsMissing() throws Exception {
        String body = VALID_BODY.replace("\"restaurantId\": 10,", "");

        mockMvc.perform(post(URL).principal(authenticatedAs(7L))
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("restaurantId")));

        verifyNoInteractions(orderHandler);
    }

    @Test
    void createOrderShouldReturn400WhenDishesAreEmpty() throws Exception {
        String body = "{\"restaurantId\": 10, \"dishes\": []}";

        mockMvc.perform(post(URL).principal(authenticatedAs(7L))
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("dishes")));

        verifyNoInteractions(orderHandler);
    }

    @Test
    void createOrderShouldReturn400WhenAQuantityIsNotPositive() throws Exception {
        String body = VALID_BODY.replace("\"quantity\": 2", "\"quantity\": 0");

        mockMvc.perform(post(URL).principal(authenticatedAs(7L))
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("quantity")));

        verifyNoInteractions(orderHandler);
    }

    @Test
    void createOrderShouldReturn400WhenADishIsFromAnotherRestaurant() throws Exception {
        doThrow(new InvalidOrderDataException(DISH_NOT_FROM_RESTAURANT_MESSAGE))
                .when(orderHandler).createOrder(any(OrderRequestDto.class), eq(7L));

        mockMvc.perform(post(URL).principal(authenticatedAs(7L))
                        .contentType(MediaType.APPLICATION_JSON).content(VALID_BODY))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(DISH_NOT_FROM_RESTAURANT_MESSAGE));
    }

    @Test
    void createOrderShouldReturn404WhenRestaurantDoesNotExist() throws Exception {
        doThrow(new RestaurantNotFoundException(RESTAURANT_NOT_FOUND_MESSAGE))
                .when(orderHandler).createOrder(any(OrderRequestDto.class), eq(7L));

        mockMvc.perform(post(URL).principal(authenticatedAs(7L))
                        .contentType(MediaType.APPLICATION_JSON).content(VALID_BODY))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value(RESTAURANT_NOT_FOUND_MESSAGE));
    }

    @Test
    void createOrderShouldReturn404WhenADishDoesNotExist() throws Exception {
        doThrow(new DishNotFoundException(DISH_NOT_FOUND_MESSAGE))
                .when(orderHandler).createOrder(any(OrderRequestDto.class), eq(7L));

        mockMvc.perform(post(URL).principal(authenticatedAs(7L))
                        .contentType(MediaType.APPLICATION_JSON).content(VALID_BODY))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value(DISH_NOT_FOUND_MESSAGE));
    }

    @Test
    void createOrderShouldReturn409WhenClientHasAnOrderInProgress() throws Exception {
        doThrow(new OrderInProgressException(ORDER_IN_PROGRESS_MESSAGE))
                .when(orderHandler).createOrder(any(OrderRequestDto.class), eq(7L));

        mockMvc.perform(post(URL).principal(authenticatedAs(7L))
                        .contentType(MediaType.APPLICATION_JSON).content(VALID_BODY))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value(ORDER_IN_PROGRESS_MESSAGE));
    }
}
