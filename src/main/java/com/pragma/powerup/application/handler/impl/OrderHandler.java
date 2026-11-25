package com.pragma.powerup.application.handler.impl;

import com.pragma.powerup.application.dto.request.OrderRequestDto;
import com.pragma.powerup.application.dto.request.PageRequestDto;
import com.pragma.powerup.application.dto.response.OrderResponseDto;
import com.pragma.powerup.application.dto.response.PageResponseDto;
import com.pragma.powerup.application.handler.IOrderHandler;
import com.pragma.powerup.application.mapper.IOrderRequestMapper;
import com.pragma.powerup.application.mapper.IOrderResponseMapper;
import com.pragma.powerup.domain.api.IOrderServicePort;
import com.pragma.powerup.domain.model.OrderModel;
import com.pragma.powerup.domain.model.OrderStatus;
import com.pragma.powerup.domain.model.PageModel;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class OrderHandler implements IOrderHandler {

    private final IOrderServicePort orderServicePort;
    private final IOrderRequestMapper orderRequestMapper;
    private final IOrderResponseMapper orderResponseMapper;

    @Override
    public void createOrder(OrderRequestDto orderRequestDto, Long clientId) {
        OrderModel order = orderRequestMapper.toOrder(orderRequestDto);

        orderServicePort.createOrder(order, clientId);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponseDto<OrderResponseDto> getOrdersByStatus(OrderStatus status, Long employeeId,
                                                               PageRequestDto pageRequestDto) {
        PageModel<OrderModel> pageModel = orderServicePort.getOrdersByStatus(status, employeeId,
                pageRequestDto.getPage(), pageRequestDto.getSize());

        List<OrderResponseDto> content = pageModel.getContent().stream()
                .map(orderResponseMapper::toResponse)
                .toList();

        return new PageResponseDto<>(content, pageModel.getPage(), pageModel.getSize(),
                pageModel.getTotalElements(), pageModel.getTotalPages());
    }
}
