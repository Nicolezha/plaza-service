package com.pragma.powerup.domain.usecase;

import com.pragma.powerup.domain.exception.InvalidOrderStatusException;
import com.pragma.powerup.domain.exception.OrderNotFoundException;
import com.pragma.powerup.domain.exception.OrderNotFromRestaurantException;
import com.pragma.powerup.domain.exception.DishNotFoundException;
import com.pragma.powerup.domain.exception.EmployeeNotAssignedException;
import com.pragma.powerup.domain.exception.InvalidPaginationException;
import com.pragma.powerup.domain.exception.InvalidOrderDataException;
import com.pragma.powerup.domain.exception.OrderInProgressException;
import com.pragma.powerup.domain.exception.RestaurantNotFoundException;
import com.pragma.powerup.domain.model.DishModel;
import com.pragma.powerup.domain.model.OrderDishModel;
import com.pragma.powerup.domain.model.OrderModel;
import com.pragma.powerup.domain.model.OrderStatus;
import com.pragma.powerup.domain.model.PageModel;
import com.pragma.powerup.domain.model.RestaurantModel;
import com.pragma.powerup.domain.spi.IDishPersistencePort;
import com.pragma.powerup.domain.spi.IEmployeeRestaurantPersistencePort;
import com.pragma.powerup.domain.spi.IOrderPersistencePort;
import com.pragma.powerup.domain.spi.IRestaurantPersistencePort;
import com.pragma.powerup.domain.validation.OrderValidator;
import com.pragma.powerup.domain.validation.PaginationValidator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.pragma.powerup.domain.util.DishConstants.DISH_NOT_FOUND_MESSAGE;
import static com.pragma.powerup.domain.util.DishConstants.RESTAURANT_NOT_FOUND_MESSAGE;
import static com.pragma.powerup.domain.util.EmployeeConstants.EMPLOYEE_NOT_ASSIGNED_MESSAGE;
import static com.pragma.powerup.domain.util.PaginationConstants.SIZE_INVALID_MESSAGE;
import static com.pragma.powerup.domain.util.OrderConstants.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderUseCaseTest {

    private static final Long CLIENT_ID = 7L;
    private static final Long RESTAURANT_ID = 10L;
    private static final Long OTHER_RESTAURANT_ID = 20L;
    private static final Long DISH_ID = 5L;
    private static final Long SECOND_DISH_ID = 6L;
    private static final Long EMPLOYEE_ID = 30L;

    @Mock
    private IOrderPersistencePort orderPersistencePort;

    @Mock
    private IRestaurantPersistencePort restaurantPersistencePort;

    @Mock
    private IDishPersistencePort dishPersistencePort;

    @Mock
    private IEmployeeRestaurantPersistencePort employeeRestaurantPersistencePort;

    @Mock
    private OrderValidator orderValidator;

    @Mock
    private PaginationValidator paginationValidator;

    @InjectMocks
    private OrderUseCase orderUseCase;

    private OrderModel validOrder() {
        OrderModel order = new OrderModel();
        order.setRestaurantId(RESTAURANT_ID);
        order.setDishes(new ArrayList<>(List.of(
                new OrderDishModel(DISH_ID, 2),
                new OrderDishModel(SECOND_DISH_ID, 1))));
        return order;
    }

    private DishModel dish(Long id, Long restaurantId, Boolean active) {
        DishModel dish = new DishModel();
        dish.setId(id);
        dish.setRestaurantId(restaurantId);
        dish.setActive(active);
        return dish;
    }

    private void givenClientWithoutOrdersInProgress() {
        when(orderPersistencePort.existsByClientIdAndStatusIn(CLIENT_ID, OrderStatus.inProgress()))
                .thenReturn(false);
    }

    private void givenRestaurantExists() {
        when(restaurantPersistencePort.findById(RESTAURANT_ID))
                .thenReturn(Optional.of(new RestaurantModel()));
    }

    @Test
    void createOrderShouldSaveAsPendingWithClientAndDateWhenEverythingIsValid() {
        OrderModel order = validOrder();
        order.setStatus(OrderStatus.READY);
        order.setEmployeeId(99L);
        givenClientWithoutOrdersInProgress();
        givenRestaurantExists();
        when(dishPersistencePort.findById(DISH_ID))
                .thenReturn(Optional.of(dish(DISH_ID, RESTAURANT_ID, true)));
        when(dishPersistencePort.findById(SECOND_DISH_ID))
                .thenReturn(Optional.of(dish(SECOND_DISH_ID, RESTAURANT_ID, true)));

        orderUseCase.createOrder(order, CLIENT_ID);

        assertEquals(OrderStatus.PENDING, order.getStatus());
        assertEquals(CLIENT_ID, order.getClientId());
        assertNotNull(order.getDate());
        assertNull(order.getEmployeeId());
        verify(orderValidator).validate(order);
        verify(orderPersistencePort).saveOrder(order);
    }

    @Test
    void createOrderShouldThrowWhenOrderDataIsInvalid() {
        OrderModel order = validOrder();
        doThrow(new InvalidOrderDataException(DISHES_REQUIRED_MESSAGE))
                .when(orderValidator).validate(order);

        InvalidOrderDataException ex = assertThrows(InvalidOrderDataException.class,
                () -> orderUseCase.createOrder(order, CLIENT_ID));

        assertEquals(DISHES_REQUIRED_MESSAGE, ex.getMessage());
        verifyNoInteractions(orderPersistencePort, restaurantPersistencePort, dishPersistencePort);
    }

    @Test
    void createOrderShouldThrowWhenClientHasAnOrderInProgress() {
        OrderModel order = validOrder();
        when(orderPersistencePort.existsByClientIdAndStatusIn(CLIENT_ID, OrderStatus.inProgress()))
                .thenReturn(true);

        OrderInProgressException ex = assertThrows(OrderInProgressException.class,
                () -> orderUseCase.createOrder(order, CLIENT_ID));

        assertEquals(ORDER_IN_PROGRESS_MESSAGE, ex.getMessage());
        verifyNoInteractions(restaurantPersistencePort, dishPersistencePort);
        verify(orderPersistencePort, never()).saveOrder(any());
    }

    @Test
    void createOrderShouldThrowWhenRestaurantDoesNotExist() {
        OrderModel order = validOrder();
        givenClientWithoutOrdersInProgress();
        when(restaurantPersistencePort.findById(RESTAURANT_ID)).thenReturn(Optional.empty());

        RestaurantNotFoundException ex = assertThrows(RestaurantNotFoundException.class,
                () -> orderUseCase.createOrder(order, CLIENT_ID));

        assertEquals(RESTAURANT_NOT_FOUND_MESSAGE, ex.getMessage());
        verifyNoInteractions(dishPersistencePort);
        verify(orderPersistencePort, never()).saveOrder(any());
    }

    @Test
    void createOrderShouldThrowWhenADishDoesNotExist() {
        OrderModel order = validOrder();
        givenClientWithoutOrdersInProgress();
        givenRestaurantExists();
        when(dishPersistencePort.findById(DISH_ID)).thenReturn(Optional.empty());

        DishNotFoundException ex = assertThrows(DishNotFoundException.class,
                () -> orderUseCase.createOrder(order, CLIENT_ID));

        assertEquals(DISH_NOT_FOUND_MESSAGE, ex.getMessage());
        verify(orderPersistencePort, never()).saveOrder(any());
    }

    @Test
    void createOrderShouldThrowWhenADishBelongsToAnotherRestaurant() {
        OrderModel order = validOrder();
        givenClientWithoutOrdersInProgress();
        givenRestaurantExists();
        when(dishPersistencePort.findById(DISH_ID))
                .thenReturn(Optional.of(dish(DISH_ID, RESTAURANT_ID, true)));
        when(dishPersistencePort.findById(SECOND_DISH_ID))
                .thenReturn(Optional.of(dish(SECOND_DISH_ID, OTHER_RESTAURANT_ID, true)));

        InvalidOrderDataException ex = assertThrows(InvalidOrderDataException.class,
                () -> orderUseCase.createOrder(order, CLIENT_ID));

        assertEquals(DISH_NOT_FROM_RESTAURANT_MESSAGE, ex.getMessage());
        verify(orderPersistencePort, never()).saveOrder(any());
    }

    @Test
    void createOrderShouldThrowWhenADishIsInactive() {
        OrderModel order = validOrder();
        givenClientWithoutOrdersInProgress();
        givenRestaurantExists();
        when(dishPersistencePort.findById(DISH_ID))
                .thenReturn(Optional.of(dish(DISH_ID, RESTAURANT_ID, false)));

        InvalidOrderDataException ex = assertThrows(InvalidOrderDataException.class,
                () -> orderUseCase.createOrder(order, CLIENT_ID));

        assertEquals(DISH_NOT_AVAILABLE_MESSAGE, ex.getMessage());
        verify(orderPersistencePort, never()).saveOrder(any());
    }

    @Test
    void inProgressStatusesShouldBePendingInPreparationAndReady() {
        assertEquals(List.of(OrderStatus.PENDING, OrderStatus.IN_PREPARATION, OrderStatus.READY),
                OrderStatus.inProgress());
    }

    @Test
    void getOrdersByStatusShouldReturnOrdersOfTheEmployeeRestaurant() {
        PageModel<OrderModel> page = new PageModel<>(List.of(new OrderModel()), 0, 10, 1, 1);
        when(employeeRestaurantPersistencePort.findRestaurantIdByEmployeeId(EMPLOYEE_ID))
                .thenReturn(Optional.of(RESTAURANT_ID));
        when(orderPersistencePort.findByRestaurantIdAndStatus(RESTAURANT_ID, OrderStatus.PENDING, 0, 10))
                .thenReturn(page);

        PageModel<OrderModel> result = orderUseCase.getOrdersByStatus(OrderStatus.PENDING, EMPLOYEE_ID, 0, 10);

        assertSame(page, result);
        verify(paginationValidator).validatePagination(0, 10);
    }

    @Test
    void getOrdersByStatusShouldThrowWhenPaginationIsInvalid() {
        doThrow(new InvalidPaginationException(SIZE_INVALID_MESSAGE))
                .when(paginationValidator).validatePagination(0, 0);

        InvalidPaginationException ex = assertThrows(InvalidPaginationException.class,
                () -> orderUseCase.getOrdersByStatus(OrderStatus.PENDING, EMPLOYEE_ID, 0, 0));

        assertEquals(SIZE_INVALID_MESSAGE, ex.getMessage());
        verifyNoInteractions(employeeRestaurantPersistencePort, orderPersistencePort);
    }

    @Test
    void getOrdersByStatusShouldThrowWhenStatusIsNull() {
        InvalidOrderDataException ex = assertThrows(InvalidOrderDataException.class,
                () -> orderUseCase.getOrdersByStatus(null, EMPLOYEE_ID, 0, 10));

        assertEquals(STATUS_REQUIRED_MESSAGE, ex.getMessage());
        verifyNoInteractions(employeeRestaurantPersistencePort, orderPersistencePort);
    }

    @Test
    void getOrdersByStatusShouldThrowWhenEmployeeIsNotAssignedToARestaurant() {
        when(employeeRestaurantPersistencePort.findRestaurantIdByEmployeeId(EMPLOYEE_ID))
                .thenReturn(Optional.empty());

        EmployeeNotAssignedException ex = assertThrows(EmployeeNotAssignedException.class,
                () -> orderUseCase.getOrdersByStatus(OrderStatus.PENDING, EMPLOYEE_ID, 0, 10));

        assertEquals(EMPLOYEE_NOT_ASSIGNED_MESSAGE, ex.getMessage());
        verifyNoInteractions(orderPersistencePort);
    }

    private static final Long ORDER_ID = 100L;

    private OrderModel storedOrder(Long restaurantId, OrderStatus status) {
        OrderModel order = new OrderModel();
        order.setId(ORDER_ID);
        order.setRestaurantId(restaurantId);
        order.setStatus(status);
        return order;
    }

    private void givenEmployeeOfRestaurant() {
        when(employeeRestaurantPersistencePort.findRestaurantIdByEmployeeId(EMPLOYEE_ID))
                .thenReturn(Optional.of(RESTAURANT_ID));
    }

    @Test
    void assignOrderShouldSetEmployeeAndInPreparationWhenOrderIsPending() {
        OrderModel order = storedOrder(RESTAURANT_ID, OrderStatus.PENDING);
        givenEmployeeOfRestaurant();
        when(orderPersistencePort.findById(ORDER_ID)).thenReturn(Optional.of(order));
        when(orderPersistencePort.updateOrder(order)).thenReturn(order);

        OrderModel result = orderUseCase.assignOrder(ORDER_ID, EMPLOYEE_ID);

        assertSame(order, result);
        assertEquals(EMPLOYEE_ID, result.getEmployeeId());
        assertEquals(OrderStatus.IN_PREPARATION, result.getStatus());
        verify(orderPersistencePort).updateOrder(order);
    }

    @Test
    void assignOrderShouldThrowWhenEmployeeIsNotAssignedToARestaurant() {
        when(employeeRestaurantPersistencePort.findRestaurantIdByEmployeeId(EMPLOYEE_ID))
                .thenReturn(Optional.empty());

        EmployeeNotAssignedException ex = assertThrows(EmployeeNotAssignedException.class,
                () -> orderUseCase.assignOrder(ORDER_ID, EMPLOYEE_ID));

        assertEquals(EMPLOYEE_NOT_ASSIGNED_MESSAGE, ex.getMessage());
        verifyNoInteractions(orderPersistencePort);
    }

    @Test
    void assignOrderShouldThrowWhenOrderDoesNotExist() {
        givenEmployeeOfRestaurant();
        when(orderPersistencePort.findById(ORDER_ID)).thenReturn(Optional.empty());

        OrderNotFoundException ex = assertThrows(OrderNotFoundException.class,
                () -> orderUseCase.assignOrder(ORDER_ID, EMPLOYEE_ID));

        assertEquals(ORDER_NOT_FOUND_MESSAGE, ex.getMessage());
        verify(orderPersistencePort, never()).updateOrder(any());
    }

    @Test
    void assignOrderShouldThrowWhenOrderBelongsToAnotherRestaurant() {
        givenEmployeeOfRestaurant();
        when(orderPersistencePort.findById(ORDER_ID))
                .thenReturn(Optional.of(storedOrder(OTHER_RESTAURANT_ID, OrderStatus.PENDING)));

        OrderNotFromRestaurantException ex = assertThrows(OrderNotFromRestaurantException.class,
                () -> orderUseCase.assignOrder(ORDER_ID, EMPLOYEE_ID));

        assertEquals(ORDER_NOT_FROM_EMPLOYEE_RESTAURANT_MESSAGE, ex.getMessage());
        verify(orderPersistencePort, never()).updateOrder(any());
    }

    @Test
    void assignOrderShouldThrowWhenOrderIsNotPending() {
        OrderModel order = storedOrder(RESTAURANT_ID, OrderStatus.IN_PREPARATION);
        order.setEmployeeId(99L);
        givenEmployeeOfRestaurant();
        when(orderPersistencePort.findById(ORDER_ID)).thenReturn(Optional.of(order));

        InvalidOrderStatusException ex = assertThrows(InvalidOrderStatusException.class,
                () -> orderUseCase.assignOrder(ORDER_ID, EMPLOYEE_ID));

        assertEquals(ORDER_NOT_PENDING_MESSAGE, ex.getMessage());
        assertEquals(99L, order.getEmployeeId());
        verify(orderPersistencePort, never()).updateOrder(any());
    }
}
