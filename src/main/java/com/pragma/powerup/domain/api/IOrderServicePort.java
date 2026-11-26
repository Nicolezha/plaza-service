package com.pragma.powerup.domain.api;

import com.pragma.powerup.domain.model.OrderModel;
import com.pragma.powerup.domain.model.OrderStatus;
import com.pragma.powerup.domain.model.PageModel;

public interface IOrderServicePort {

    void createOrder(OrderModel order, Long clientId);

    PageModel<OrderModel> getOrdersByStatus(OrderStatus status, Long employeeId, int page, int size);

    OrderModel assignOrder(Long orderId, Long employeeId);
}
