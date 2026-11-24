package com.pragma.powerup.domain.usecase;

import com.pragma.powerup.domain.exception.DishNotFoundException;
import com.pragma.powerup.domain.exception.InvalidOrderDataException;
import com.pragma.powerup.domain.exception.OrderInProgressException;
import com.pragma.powerup.domain.exception.RestaurantNotFoundException;
import com.pragma.powerup.domain.model.DishModel;
import com.pragma.powerup.domain.model.OrderDishModel;
import com.pragma.powerup.domain.model.OrderModel;
import com.pragma.powerup.domain.model.OrderStatus;
import com.pragma.powerup.domain.model.RestaurantModel;
import com.pragma.powerup.domain.spi.IDishPersistencePort;
import com.pragma.powerup.domain.spi.IOrderPersistencePort;
import com.pragma.powerup.domain.spi.IRestaurantPersistencePort;
import com.pragma.powerup.domain.validation.OrderValidator;
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

    @Mock
    private IOrderPersistencePort orderPersistencePort;

    @Mock
    private IRestaurantPersistencePort restaurantPersistencePort;

    @Mock
    private IDishPersistencePort dishPersistencePort;

    @Mock
    private OrderValidator orderValidator;

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
}
