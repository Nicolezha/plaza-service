package com.pragma.powerup.infrastructure.out.jpa.adapter;

import com.pragma.powerup.infrastructure.exception.NoDataFoundException;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import com.pragma.powerup.domain.model.OrderModel;
import com.pragma.powerup.domain.model.OrderStatus;
import com.pragma.powerup.domain.model.PageModel;
import com.pragma.powerup.infrastructure.out.jpa.entity.OrderEntity;
import com.pragma.powerup.infrastructure.out.jpa.mapper.IOrderEntityMapper;
import com.pragma.powerup.infrastructure.out.jpa.repository.IOrderRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
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

    @Test
    void findByRestaurantIdAndStatusShouldPageByDateAscendingAndMapToPageModel() {
        OrderEntity entity = new OrderEntity();
        OrderModel model = new OrderModel();
        Pageable pageable = PageRequest.of(1, 2, Sort.by("date").ascending());
        when(orderRepository.findByRestaurant_IdAndStatus(eq(10L), eq(OrderStatus.PENDING), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(entity), pageable, 5));
        when(orderEntityMapper.toOrderModel(entity)).thenReturn(model);

        PageModel<OrderModel> result = orderJpaAdapter.findByRestaurantIdAndStatus(10L, OrderStatus.PENDING, 1, 2);

        ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
        verify(orderRepository).findByRestaurant_IdAndStatus(eq(10L), eq(OrderStatus.PENDING), captor.capture());
        assertEquals(pageable, captor.getValue());
        assertEquals(List.of(model), result.getContent());
        assertEquals(1, result.getPage());
        assertEquals(2, result.getSize());
        assertEquals(5, result.getTotalElements());
        assertEquals(3, result.getTotalPages());
    }

    @Test
    void findByIdShouldMapTheEntityWhenItExists() {
        OrderEntity entity = new OrderEntity();
        OrderModel model = new OrderModel();
        when(orderRepository.findById(100L)).thenReturn(Optional.of(entity));
        when(orderEntityMapper.toOrderModel(entity)).thenReturn(model);

        assertSame(model, orderJpaAdapter.findById(100L).orElseThrow());
    }

    @Test
    void findByIdShouldReturnEmptyWhenOrderDoesNotExist() {
        when(orderRepository.findById(100L)).thenReturn(Optional.empty());

        assertTrue(orderJpaAdapter.findById(100L).isEmpty());
    }

    @Test
    void updateOrderShouldOnlyChangeStatusAndEmployeeOfTheStoredEntity() {
        OrderEntity entity = new OrderEntity();
        entity.setStatus(OrderStatus.PENDING);
        OrderModel model = new OrderModel();
        model.setId(100L);
        model.setStatus(OrderStatus.IN_PREPARATION);
        model.setEmployeeId(30L);
        OrderModel updated = new OrderModel();
        when(orderRepository.findById(100L)).thenReturn(Optional.of(entity));
        when(orderRepository.save(entity)).thenReturn(entity);
        when(orderEntityMapper.toOrderModel(entity)).thenReturn(updated);

        OrderModel result = orderJpaAdapter.updateOrder(model);

        assertSame(updated, result);
        assertEquals(OrderStatus.IN_PREPARATION, entity.getStatus());
        assertEquals(30L, entity.getEmployeeId());
        verify(orderEntityMapper, never()).toOrderEntity(any());
    }

    @Test
    void updateOrderShouldThrowWhenOrderDoesNotExist() {
        OrderModel model = new OrderModel();
        model.setId(100L);
        when(orderRepository.findById(100L)).thenReturn(Optional.empty());

        assertThrows(NoDataFoundException.class, () -> orderJpaAdapter.updateOrder(model));
    }
}
