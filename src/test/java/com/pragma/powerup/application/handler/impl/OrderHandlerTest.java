package com.pragma.powerup.application.handler.impl;


import com.pragma.powerup.application.dto.request.OrderRequestDto;
import com.pragma.powerup.application.dto.request.PageRequestDto;
import com.pragma.powerup.application.dto.response.OrderResponseDto;
import com.pragma.powerup.application.dto.response.PageResponseDto;
import com.pragma.powerup.application.mapper.IOrderRequestMapper;
import com.pragma.powerup.application.mapper.IOrderResponseMapper;
import com.pragma.powerup.domain.api.IOrderServicePort;
import com.pragma.powerup.domain.model.OrderModel;
import com.pragma.powerup.domain.model.OrderStatus;
import com.pragma.powerup.domain.model.PageModel;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderHandlerTest {

    @Mock
    private IOrderServicePort orderServicePort;

    @Mock
    private IOrderRequestMapper orderRequestMapper;

    @Mock
    private IOrderResponseMapper orderResponseMapper;

    @InjectMocks
    private OrderHandler orderHandler;

    @Test
    void createOrderShouldMapRequestAndDelegateWithTheClientId() {
        OrderRequestDto request = new OrderRequestDto();
        OrderModel model = new OrderModel();
        when(orderRequestMapper.toOrder(request)).thenReturn(model);

        orderHandler.createOrder(request, 7L);

        verify(orderServicePort).createOrder(model, 7L);
    }

    @Test
    void getOrdersByStatusShouldDelegateAndMapThePage() {
        PageRequestDto pageRequest = new PageRequestDto();
        pageRequest.setPage(1);
        pageRequest.setSize(5);
        OrderModel model = new OrderModel();
        OrderResponseDto response = new OrderResponseDto();
        when(orderServicePort.getOrdersByStatus(OrderStatus.READY, 30L, 1, 5))
                .thenReturn(new PageModel<>(List.of(model), 1, 5, 6, 2));
        when(orderResponseMapper.toResponse(model)).thenReturn(response);

        PageResponseDto<OrderResponseDto> result = orderHandler.getOrdersByStatus(OrderStatus.READY, 30L, pageRequest);

        assertEquals(List.of(response), result.getContent());
        assertEquals(1, result.getPage());
        assertEquals(5, result.getSize());
        assertEquals(6, result.getTotalElements());
        assertEquals(2, result.getTotalPages());
    }

    @Test
    void assignOrderShouldDelegateAndMapTheResult() {
        OrderModel model = new OrderModel();
        OrderResponseDto response = new OrderResponseDto();
        when(orderServicePort.assignOrder(100L, 30L)).thenReturn(model);
        when(orderResponseMapper.toResponse(model)).thenReturn(response);

        OrderResponseDto result = orderHandler.assignOrder(100L, 30L);

        assertEquals(response, result);
    }
}
