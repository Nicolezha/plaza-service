package com.pragma.powerup.infrastructure.out.jpa.adapter;

import com.pragma.powerup.domain.model.EmployeeRestaurantModel;
import com.pragma.powerup.infrastructure.out.jpa.entity.EmployeeRestaurantEntity;
import com.pragma.powerup.infrastructure.out.jpa.entity.RestaurantEntity;
import com.pragma.powerup.infrastructure.out.jpa.mapper.IEmployeeRestaurantEntityMapper;
import com.pragma.powerup.infrastructure.out.jpa.repository.IEmployeeRestaurantRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EmployeeRestaurantJpaAdapterTest {

    @Mock
    private IEmployeeRestaurantRepository employeeRestaurantRepository;

    @Mock
    private IEmployeeRestaurantEntityMapper employeeRestaurantEntityMapper;

    @InjectMocks
    private EmployeeRestaurantJpaAdapter employeeRestaurantJpaAdapter;

    @Test
    void saveEmployeeRestaurantShouldMapAndSave() {
        EmployeeRestaurantModel model = new EmployeeRestaurantModel(30L, 10L);
        EmployeeRestaurantEntity entity = new EmployeeRestaurantEntity();
        when(employeeRestaurantEntityMapper.toEntity(model)).thenReturn(entity);

        employeeRestaurantJpaAdapter.saveEmployeeRestaurant(model);

        verify(employeeRestaurantRepository).save(entity);
    }

    @Test
    void existsByEmployeeIdShouldDelegateToRepository() {
        when(employeeRestaurantRepository.existsById(30L)).thenReturn(true);

        assertTrue(employeeRestaurantJpaAdapter.existsByEmployeeId(30L));
    }

    @Test
    void findRestaurantIdByEmployeeIdShouldReturnTheRestaurantIdWhenAssigned() {
        RestaurantEntity restaurant = new RestaurantEntity();
        restaurant.setId(10L);
        when(employeeRestaurantRepository.findById(30L))
                .thenReturn(Optional.of(new EmployeeRestaurantEntity(30L, restaurant)));

        assertEquals(Optional.of(10L), employeeRestaurantJpaAdapter.findRestaurantIdByEmployeeId(30L));
    }

    @Test
    void findRestaurantIdByEmployeeIdShouldReturnEmptyWhenNotAssigned() {
        when(employeeRestaurantRepository.findById(30L)).thenReturn(Optional.empty());

        assertTrue(employeeRestaurantJpaAdapter.findRestaurantIdByEmployeeId(30L).isEmpty());
    }
}
