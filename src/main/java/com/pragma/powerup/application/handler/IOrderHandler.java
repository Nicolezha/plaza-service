package com.pragma.powerup.application.handler;

import com.pragma.powerup.application.dto.request.OrderRequestDto;

public interface IOrderHandler {

    void createOrder(OrderRequestDto orderRequestDto, Long clientId);
}
