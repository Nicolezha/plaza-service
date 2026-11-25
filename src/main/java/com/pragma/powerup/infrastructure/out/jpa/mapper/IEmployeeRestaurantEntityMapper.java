package com.pragma.powerup.infrastructure.out.jpa.mapper;

import com.pragma.powerup.domain.model.EmployeeRestaurantModel;
import com.pragma.powerup.infrastructure.out.jpa.entity.EmployeeRestaurantEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        unmappedSourcePolicy = ReportingPolicy.IGNORE)
public interface IEmployeeRestaurantEntityMapper {

    @Mapping(source = "restaurantId", target = "restaurant.id")
    EmployeeRestaurantEntity toEntity(EmployeeRestaurantModel employeeRestaurantModel);
}
