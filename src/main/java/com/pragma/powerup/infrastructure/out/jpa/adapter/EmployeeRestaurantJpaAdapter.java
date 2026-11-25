package com.pragma.powerup.infrastructure.out.jpa.adapter;

import com.pragma.powerup.domain.model.EmployeeRestaurantModel;
import com.pragma.powerup.domain.spi.IEmployeeRestaurantPersistencePort;
import com.pragma.powerup.infrastructure.out.jpa.mapper.IEmployeeRestaurantEntityMapper;
import com.pragma.powerup.infrastructure.out.jpa.repository.IEmployeeRestaurantRepository;
import lombok.RequiredArgsConstructor;

import java.util.Optional;

@RequiredArgsConstructor
public class EmployeeRestaurantJpaAdapter implements IEmployeeRestaurantPersistencePort {

    private final IEmployeeRestaurantRepository employeeRestaurantRepository;
    private final IEmployeeRestaurantEntityMapper employeeRestaurantEntityMapper;

    @Override
    public void saveEmployeeRestaurant(EmployeeRestaurantModel employeeRestaurant) {
        employeeRestaurantRepository.save(employeeRestaurantEntityMapper.toEntity(employeeRestaurant));
    }

    @Override
    public boolean existsByEmployeeId(Long employeeId) {
        return employeeRestaurantRepository.existsById(employeeId);
    }

    @Override
    public Optional<Long> findRestaurantIdByEmployeeId(Long employeeId) {
        return employeeRestaurantRepository.findById(employeeId)
                .map(employeeRestaurant -> employeeRestaurant.getRestaurant().getId());
    }
}
