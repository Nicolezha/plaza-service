package com.pragma.powerup.domain.validation;

import com.pragma.powerup.domain.exception.InvalidOrderDataException;
import com.pragma.powerup.domain.model.OrderDishModel;
import com.pragma.powerup.domain.model.OrderModel;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static com.pragma.powerup.domain.util.OrderConstants.*;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class OrderValidatorTest {

    private final OrderValidator orderValidator = new OrderValidator();

    private OrderModel orderWith(List<OrderDishModel> dishes) {
        OrderModel order = new OrderModel();
        order.setRestaurantId(10L);
        order.setDishes(dishes);
        return order;
    }

    @Test
    void validateShouldPassWhenDishesAreValid() {
        OrderModel order = orderWith(List.of(new OrderDishModel(5L, 2), new OrderDishModel(6L, 1)));

        assertDoesNotThrow(() -> orderValidator.validate(order));
    }

    @Test
    void validateShouldThrowWhenDishesAreNull() {
        OrderModel order = orderWith(null);

        InvalidOrderDataException ex = assertThrows(InvalidOrderDataException.class,
                () -> orderValidator.validate(order));

        assertEquals(DISHES_REQUIRED_MESSAGE, ex.getMessage());
    }

    @Test
    void validateShouldThrowWhenDishesAreEmpty() {
        OrderModel order = orderWith(new ArrayList<>());

        InvalidOrderDataException ex = assertThrows(InvalidOrderDataException.class,
                () -> orderValidator.validate(order));

        assertEquals(DISHES_REQUIRED_MESSAGE, ex.getMessage());
    }

    @Test
    void validateShouldThrowWhenQuantityIsNull() {
        OrderModel order = orderWith(List.of(new OrderDishModel(5L, null)));

        InvalidOrderDataException ex = assertThrows(InvalidOrderDataException.class,
                () -> orderValidator.validate(order));

        assertEquals(QUANTITY_INVALID_MESSAGE, ex.getMessage());
    }

    @Test
    void validateShouldThrowWhenQuantityIsZero() {
        OrderModel order = orderWith(List.of(new OrderDishModel(5L, 0)));

        InvalidOrderDataException ex = assertThrows(InvalidOrderDataException.class,
                () -> orderValidator.validate(order));

        assertEquals(QUANTITY_INVALID_MESSAGE, ex.getMessage());
    }

    @Test
    void validateShouldThrowWhenADishIsRepeated() {
        OrderModel order = orderWith(List.of(new OrderDishModel(5L, 1), new OrderDishModel(5L, 3)));

        InvalidOrderDataException ex = assertThrows(InvalidOrderDataException.class,
                () -> orderValidator.validate(order));

        assertEquals(DUPLICATED_DISH_MESSAGE, ex.getMessage());
    }
}
