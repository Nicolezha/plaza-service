package com.pragma.powerup.infrastructure.out.jpa.mapper;

import com.pragma.powerup.domain.model.OrderDishModel;
import com.pragma.powerup.domain.model.OrderModel;
import com.pragma.powerup.infrastructure.out.jpa.entity.OrderDishEntity;
import com.pragma.powerup.infrastructure.out.jpa.entity.OrderEntity;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        unmappedSourcePolicy = ReportingPolicy.IGNORE
)
public interface IOrderEntityMapper {

    @Mapping(source = "restaurantId", target = "restaurant.id")
    OrderEntity toOrderEntity(OrderModel orderModel);

    @Mapping(source = "dishId", target = "dish.id")
    OrderDishEntity toOrderDishEntity(OrderDishModel orderDishModel);

    @Mapping(source = "restaurant.id", target = "restaurantId")
    OrderModel toOrderModel(OrderEntity orderEntity);

    @Mapping(source = "dish.id", target = "dishId")
    OrderDishModel toOrderDishModel(OrderDishEntity orderDishEntity);

    @AfterMapping
    default void linkDishesToOrder(@MappingTarget OrderEntity orderEntity) {
        if (orderEntity.getDishes() != null) {
            orderEntity.getDishes().forEach(orderDish -> orderDish.setOrder(orderEntity));
        }
    }
}
