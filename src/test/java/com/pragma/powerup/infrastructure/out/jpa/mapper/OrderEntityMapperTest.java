package com.pragma.powerup.infrastructure.out.jpa.mapper;

import com.pragma.powerup.domain.model.OrderDishModel;
import com.pragma.powerup.domain.model.OrderModel;
import com.pragma.powerup.domain.model.OrderStatus;
import com.pragma.powerup.infrastructure.out.jpa.entity.DishEntity;
import com.pragma.powerup.infrastructure.out.jpa.entity.OrderDishEntity;
import com.pragma.powerup.infrastructure.out.jpa.entity.OrderEntity;
import com.pragma.powerup.infrastructure.out.jpa.entity.RestaurantEntity;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

class OrderEntityMapperTest {

    private final IOrderEntityMapper mapper = Mappers.getMapper(IOrderEntityMapper.class);

    @Test
    void toOrderEntityShouldMapIdsAndLinkEachDishToTheOrder() {
        LocalDateTime date = LocalDateTime.of(2026, 11, 24, 12, 0);
        OrderModel model = new OrderModel(null, 7L, 10L, date, OrderStatus.PENDING, null,
                List.of(new OrderDishModel(5L, 2), new OrderDishModel(6L, 1)));

        OrderEntity entity = mapper.toOrderEntity(model);

        assertEquals(7L, entity.getClientId());
        assertEquals(10L, entity.getRestaurant().getId());
        assertEquals(OrderStatus.PENDING, entity.getStatus());
        assertEquals(date, entity.getDate());
        assertEquals(2, entity.getDishes().size());
        assertEquals(5L, entity.getDishes().get(0).getDish().getId());
        assertEquals(2, entity.getDishes().get(0).getQuantity());
        entity.getDishes().forEach(orderDish -> assertSame(entity, orderDish.getOrder()));
    }

    @Test
    void toOrderModelShouldMapRestaurantAndDishIds() {
        RestaurantEntity restaurant = new RestaurantEntity();
        restaurant.setId(10L);
        DishEntity dish = new DishEntity();
        dish.setId(5L);
        OrderEntity entity = new OrderEntity();
        entity.setId(1L);
        entity.setClientId(7L);
        entity.setRestaurant(restaurant);
        entity.setStatus(OrderStatus.PENDING);
        OrderDishEntity orderDish = new OrderDishEntity(1L, entity, dish, 3);
        entity.setDishes(List.of(orderDish));

        OrderModel model = mapper.toOrderModel(entity);

        assertEquals(1L, model.getId());
        assertEquals(10L, model.getRestaurantId());
        assertEquals(5L, model.getDishes().get(0).getDishId());
        assertEquals(3, model.getDishes().get(0).getQuantity());
    }
}
