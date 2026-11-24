package com.pragma.powerup.domain.validation;

import com.pragma.powerup.domain.exception.InvalidOrderDataException;
import com.pragma.powerup.domain.model.OrderDishModel;
import com.pragma.powerup.domain.model.OrderModel;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.pragma.powerup.domain.util.OrderConstants.*;

public class OrderValidator {

    public void validate(OrderModel order) {
        List<OrderDishModel> dishes = order.getDishes();

        if (dishes == null || dishes.isEmpty()) {
            throw new InvalidOrderDataException(DISHES_REQUIRED_MESSAGE);
        }

        Set<Long> dishIds = new HashSet<>();
        for (OrderDishModel orderDish : dishes) {
            if (orderDish.getQuantity() == null || orderDish.getQuantity() <= 0) {
                throw new InvalidOrderDataException(QUANTITY_INVALID_MESSAGE);
            }
            if (!dishIds.add(orderDish.getDishId())) {
                throw new InvalidOrderDataException(DUPLICATED_DISH_MESSAGE);
            }
        }
    }
}
