package com.pragma.powerup.infrastructure.out.jpa.adapter;

import com.pragma.powerup.domain.model.OrderModel;
import com.pragma.powerup.domain.model.OrderStatus;
import com.pragma.powerup.domain.model.PageModel;
import com.pragma.powerup.domain.spi.IOrderPersistencePort;
import com.pragma.powerup.infrastructure.out.jpa.entity.OrderEntity;
import com.pragma.powerup.infrastructure.out.jpa.mapper.IOrderEntityMapper;
import com.pragma.powerup.infrastructure.out.jpa.repository.IOrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.List;

@RequiredArgsConstructor
public class OrderJpaAdapter implements IOrderPersistencePort {

    private final IOrderRepository orderRepository;
    private final IOrderEntityMapper orderEntityMapper;

    @Override
    public OrderModel saveOrder(OrderModel order) {
        OrderEntity orderEntity = orderRepository.save(orderEntityMapper.toOrderEntity(order));
        return orderEntityMapper.toOrderModel(orderEntity);
    }

    @Override
    public boolean existsByClientIdAndStatusIn(Long clientId, List<OrderStatus> statuses) {
        return orderRepository.existsByClientIdAndStatusIn(clientId, statuses);
    }

    @Override
    public PageModel<OrderModel> findByRestaurantIdAndStatus(Long restaurantId, OrderStatus status, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("date").ascending());

        Page<OrderEntity> result = orderRepository.findByRestaurant_IdAndStatus(restaurantId, status, pageable);

        List<OrderModel> content = result.getContent().stream()
                .map(orderEntityMapper::toOrderModel)
                .toList();

        return new PageModel<>(content, result.getNumber(), result.getSize(),
                result.getTotalElements(), result.getTotalPages());
    }
}
