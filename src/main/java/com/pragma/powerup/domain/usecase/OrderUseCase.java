package com.pragma.powerup.domain.usecase;

import com.pragma.powerup.domain.api.IOrderServicePort;
import com.pragma.powerup.domain.exception.DishNotFoundException;
import com.pragma.powerup.domain.exception.InvalidOrderDataException;
import com.pragma.powerup.domain.exception.OrderInProgressException;
import com.pragma.powerup.domain.exception.RestaurantNotFoundException;
import com.pragma.powerup.domain.model.DishModel;
import com.pragma.powerup.domain.model.OrderDishModel;
import com.pragma.powerup.domain.model.OrderModel;
import com.pragma.powerup.domain.model.OrderStatus;
import com.pragma.powerup.domain.spi.IDishPersistencePort;
import com.pragma.powerup.domain.spi.IOrderPersistencePort;
import com.pragma.powerup.domain.spi.IRestaurantPersistencePort;
import com.pragma.powerup.domain.validation.OrderValidator;

import java.time.LocalDateTime;

import static com.pragma.powerup.domain.util.DishConstants.DISH_NOT_FOUND_MESSAGE;
import static com.pragma.powerup.domain.util.DishConstants.RESTAURANT_NOT_FOUND_MESSAGE;
import static com.pragma.powerup.domain.util.OrderConstants.*;

public class OrderUseCase implements IOrderServicePort {

    private final IOrderPersistencePort orderPersistencePort;
    private final IRestaurantPersistencePort restaurantPersistencePort;
    private final IDishPersistencePort dishPersistencePort;
    private final OrderValidator orderValidator;

    public OrderUseCase(IOrderPersistencePort orderPersistencePort,
                        IRestaurantPersistencePort restaurantPersistencePort,
                        IDishPersistencePort dishPersistencePort,
                        OrderValidator orderValidator) {
        this.orderPersistencePort = orderPersistencePort;
        this.restaurantPersistencePort = restaurantPersistencePort;
        this.dishPersistencePort = dishPersistencePort;
        this.orderValidator = orderValidator;
    }

    @Override
    public void createOrder(OrderModel order, Long clientId) {
        orderValidator.validate(order);

        if (orderPersistencePort.existsByClientIdAndStatusIn(clientId, OrderStatus.inProgress())) {
            throw new OrderInProgressException(ORDER_IN_PROGRESS_MESSAGE);
        }

        if (restaurantPersistencePort.findById(order.getRestaurantId()).isEmpty()) {
            throw new RestaurantNotFoundException(RESTAURANT_NOT_FOUND_MESSAGE);
        }

        for (OrderDishModel orderDish : order.getDishes()) {
            validateDishBelongsToRestaurant(orderDish.getDishId(), order.getRestaurantId());
        }

        order.setClientId(clientId);
        order.setStatus(OrderStatus.PENDING);
        order.setDate(LocalDateTime.now());
        order.setEmployeeId(null);

        orderPersistencePort.saveOrder(order);
    }

    private void validateDishBelongsToRestaurant(Long dishId, Long restaurantId) {
        DishModel dish = dishPersistencePort.findById(dishId)
                .orElseThrow(() -> new DishNotFoundException(DISH_NOT_FOUND_MESSAGE));

        if (!restaurantId.equals(dish.getRestaurantId())) {
            throw new InvalidOrderDataException(DISH_NOT_FROM_RESTAURANT_MESSAGE);
        }

        if (!Boolean.TRUE.equals(dish.getActive())) {
            throw new InvalidOrderDataException(DISH_NOT_AVAILABLE_MESSAGE);
        }
    }
}
