package com.pragma.powerup.application.handler.impl;

import com.pragma.powerup.application.dto.request.OrderRequestDto;
import com.pragma.powerup.application.mapper.IOrderRequestMapper;
import com.pragma.powerup.domain.api.IOrderServicePort;
import com.pragma.powerup.domain.model.OrderModel;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderHandlerTest {

    @Mock
    private IOrderServicePort orderServicePort;

    @Mock
    private IOrderRequestMapper orderRequestMapper;

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
}
