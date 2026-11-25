package com.pragma.powerup.infrastructure.input.rest;

import com.pragma.powerup.application.dto.request.OrderRequestDto;
import com.pragma.powerup.application.dto.request.PageRequestDto;
import com.pragma.powerup.application.dto.response.OrderDishResponseDto;
import com.pragma.powerup.application.dto.response.OrderResponseDto;
import com.pragma.powerup.application.dto.response.PageResponseDto;
import com.pragma.powerup.application.handler.IOrderHandler;
import com.pragma.powerup.domain.exception.DishNotFoundException;
import com.pragma.powerup.domain.exception.EmployeeNotAssignedException;
import com.pragma.powerup.domain.exception.InvalidPaginationException;
import com.pragma.powerup.domain.exception.InvalidOrderDataException;
import com.pragma.powerup.domain.exception.OrderInProgressException;
import com.pragma.powerup.domain.exception.RestaurantNotFoundException;
import com.pragma.powerup.domain.model.OrderStatus;
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

import java.time.LocalDateTime;
import java.util.List;

import static com.pragma.powerup.domain.util.DishConstants.DISH_NOT_FOUND_MESSAGE;
import static com.pragma.powerup.domain.util.DishConstants.RESTAURANT_NOT_FOUND_MESSAGE;
import static com.pragma.powerup.domain.util.EmployeeConstants.EMPLOYEE_NOT_ASSIGNED_MESSAGE;
import static com.pragma.powerup.domain.util.PaginationConstants.SIZE_INVALID_MESSAGE;
import static com.pragma.powerup.domain.util.OrderConstants.DISH_NOT_FROM_RESTAURANT_MESSAGE;
import static com.pragma.powerup.domain.util.OrderConstants.ORDER_IN_PROGRESS_MESSAGE;
import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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

    private OrderResponseDto pendingOrderResponse() {
        OrderDishResponseDto dish = new OrderDishResponseDto();
        dish.setDishId(5L);
        dish.setQuantity(2);

        OrderResponseDto order = new OrderResponseDto();
        order.setId(1L);
        order.setClientId(7L);
        order.setRestaurantId(10L);
        order.setDate(LocalDateTime.of(2026, 11, 24, 12, 0));
        order.setStatus(OrderStatus.PENDING);
        order.setDishes(List.of(dish));
        return order;
    }

    @Test
    void getOrdersByStatusShouldReturn200WithThePageAndPassStatusEmployeeAndPagination() throws Exception {
        when(orderHandler.getOrdersByStatus(eq(OrderStatus.PENDING), eq(30L), any(PageRequestDto.class)))
                .thenReturn(new PageResponseDto<>(List.of(pendingOrderResponse()), 2, 5, 11, 3));

        mockMvc.perform(get(URL).principal(authenticatedAs(30L))
                        .param("status", "PENDING").param("page", "2").param("size", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(1))
                .andExpect(jsonPath("$.content[0].clientId").value(7))
                .andExpect(jsonPath("$.content[0].restaurantId").value(10))
                .andExpect(jsonPath("$.content[0].status").value("PENDING"))
                .andExpect(jsonPath("$.content[0].dishes[0].dishId").value(5))
                .andExpect(jsonPath("$.content[0].dishes[0].quantity").value(2))
                .andExpect(jsonPath("$.page").value(2))
                .andExpect(jsonPath("$.totalElements").value(11));

        ArgumentCaptor<PageRequestDto> captor = ArgumentCaptor.forClass(PageRequestDto.class);
        verify(orderHandler).getOrdersByStatus(eq(OrderStatus.PENDING), eq(30L), captor.capture());
        assertEquals(2, captor.getValue().getPage());
        assertEquals(5, captor.getValue().getSize());
    }

    @Test
    void getOrdersByStatusShouldReturn400WhenStatusIsMissing() throws Exception {
        mockMvc.perform(get(URL).principal(authenticatedAs(30L)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(orderHandler);
    }

    @Test
    void getOrdersByStatusShouldReturn400WhenStatusIsNotValid() throws Exception {
        mockMvc.perform(get(URL).principal(authenticatedAs(30L)).param("status", "COOKING"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(orderHandler);
    }

    @Test
    void getOrdersByStatusShouldReturn400WhenPaginationIsInvalid() throws Exception {
        when(orderHandler.getOrdersByStatus(eq(OrderStatus.PENDING), eq(30L), any(PageRequestDto.class)))
                .thenThrow(new InvalidPaginationException(SIZE_INVALID_MESSAGE));

        mockMvc.perform(get(URL).principal(authenticatedAs(30L))
                        .param("status", "PENDING").param("size", "0"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(SIZE_INVALID_MESSAGE));
    }

    @Test
    void getOrdersByStatusShouldReturn403WhenEmployeeIsNotAssignedToARestaurant() throws Exception {
        when(orderHandler.getOrdersByStatus(eq(OrderStatus.PENDING), eq(30L), any(PageRequestDto.class)))
                .thenThrow(new EmployeeNotAssignedException(EMPLOYEE_NOT_ASSIGNED_MESSAGE));

        mockMvc.perform(get(URL).principal(authenticatedAs(30L)).param("status", "PENDING"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value(EMPLOYEE_NOT_ASSIGNED_MESSAGE));
    }
}
