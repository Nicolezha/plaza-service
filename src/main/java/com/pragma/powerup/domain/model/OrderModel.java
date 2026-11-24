package com.pragma.powerup.domain.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class OrderModel {
    private Long id;
    private Long clientId;
    private Long restaurantId;
    private LocalDateTime date;
    private OrderStatus status;
    private Long employeeId;
    private List<OrderDishModel> dishes;
}
