package com.pragma.powerup.infrastructure.out.jpa.adapter;

import com.pragma.powerup.domain.model.OrderModel;
import com.pragma.powerup.domain.model.OrderStatus;
import com.pragma.powerup.infrastructure.out.jpa.entity.OrderEntity;
import com.pragma.powerup.infrastructure.out.jpa.mapper.IOrderEntityMapper;
import com.pragma.powerup.infrastructure.out.jpa.repository.IOrderRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderJpaAdapterTest {

    @Mock
    private IOrderRepository orderRepository;

    @Mock
    private IOrderEntityMapper orderEntityMapper;

    @InjectMocks
    private OrderJpaAdapter orderJpaAdapter;

    @Test
    void saveOrderShouldMapSaveAndMapBack() {
        OrderModel model = new OrderModel();
        OrderEntity entity = new OrderEntity();
        OrderEntity savedEntity = new OrderEntity();
        OrderModel savedModel = new OrderModel();

        when(orderEntityMapper.toOrderEntity(model)).thenReturn(entity);
        when(orderRepository.save(entity)).thenReturn(savedEntity);
        when(orderEntityMapper.toOrderModel(savedEntity)).thenReturn(savedModel);

        OrderModel result = orderJpaAdapter.saveOrder(model);

        assertSame(savedModel, result);
    }

    @Test
    void existsByClientIdAndStatusInShouldReturnTrueWhenRepositoryFindsAnOrder() {
        List<OrderStatus> statuses = OrderStatus.inProgress();
        when(orderRepository.existsByClientIdAndStatusIn(7L, statuses)).thenReturn(true);

        assertTrue(orderJpaAdapter.existsByClientIdAndStatusIn(7L, statuses));
    }

    @Test
    void existsByClientIdAndStatusInShouldReturnFalseWhenRepositoryFindsNothing() {
        List<OrderStatus> statuses = OrderStatus.inProgress();
        when(orderRepository.existsByClientIdAndStatusIn(7L, statuses)).thenReturn(false);

        assertFalse(orderJpaAdapter.existsByClientIdAndStatusIn(7L, statuses));
    }
}
