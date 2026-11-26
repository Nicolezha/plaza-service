package com.pragma.powerup.application.handler;

import com.pragma.powerup.application.dto.request.OrderRequestDto;
import com.pragma.powerup.application.dto.request.PageRequestDto;
import com.pragma.powerup.application.dto.response.OrderResponseDto;
import com.pragma.powerup.application.dto.response.PageResponseDto;
import com.pragma.powerup.domain.model.OrderStatus;

public interface IOrderHandler {

    void createOrder(OrderRequestDto orderRequestDto, Long clientId);

    PageResponseDto<OrderResponseDto> getOrdersByStatus(OrderStatus status, Long employeeId, PageRequestDto pageRequestDto);

    OrderResponseDto assignOrder(Long orderId, Long employeeId);
}
