package com.pragma.powerup.domain.spi;

import com.pragma.powerup.domain.model.OrderModel;
import com.pragma.powerup.domain.model.OrderStatus;
import com.pragma.powerup.domain.model.PageModel;

import java.util.List;

public interface IOrderPersistencePort {

    OrderModel saveOrder(OrderModel order);

    boolean existsByClientIdAndStatusIn(Long clientId, List<OrderStatus> statuses);

    PageModel<OrderModel> findByRestaurantIdAndStatus(Long restaurantId, OrderStatus status, int page, int size);
}
