package com.pragma.powerup.domain.spi;

import com.pragma.powerup.domain.model.OrderModel;
import com.pragma.powerup.domain.model.OrderStatus;

import java.util.List;

public interface IOrderPersistencePort {

    OrderModel saveOrder(OrderModel order);

    boolean existsByClientIdAndStatusIn(Long clientId, List<OrderStatus> statuses);
}
