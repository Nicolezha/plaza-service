package com.pragma.powerup.infrastructure.out.jpa.mapper;

import com.pragma.powerup.domain.model.DishModel;
import com.pragma.powerup.infrastructure.out.jpa.entity.DishEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        unmappedSourcePolicy = ReportingPolicy.IGNORE
)
public interface IDishEntityMapper {

    @Mapping(source = "category.id", target = "categoryId")
    @Mapping(source = "restaurant.id", target = "restaurantId")
    DishModel toDishModel(DishEntity dishEntity);

    @Mapping(source = "categoryId", target = "category.id")
    @Mapping(source = "restaurantId", target = "restaurant.id")
    DishEntity toDishEntity(DishModel dishModel);

}
