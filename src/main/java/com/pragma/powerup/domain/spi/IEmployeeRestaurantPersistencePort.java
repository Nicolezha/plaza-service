package com.pragma.powerup.domain.spi;

import com.pragma.powerup.domain.model.EmployeeRestaurantModel;

import java.util.Optional;

public interface IEmployeeRestaurantPersistencePort {

    void saveEmployeeRestaurant(EmployeeRestaurantModel employeeRestaurant);

    boolean existsByEmployeeId(Long employeeId);

    Optional<Long> findRestaurantIdByEmployeeId(Long employeeId);
}
