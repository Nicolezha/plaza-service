package com.pragma.powerup.infrastructure.out.jpa.adapter;

import com.pragma.powerup.domain.model.RestaurantModel;
import com.pragma.powerup.infrastructure.out.jpa.entity.RestaurantEntity;
import com.pragma.powerup.infrastructure.out.jpa.mapper.IRestaurantEntityMapper;
import com.pragma.powerup.infrastructure.out.jpa.repository.IRestaurantRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RestaurantJpaAdapterTest {

    @Mock
    private IRestaurantRepository restaurantRepository;

    @Mock
    private IRestaurantEntityMapper restaurantEntityMapper;

    @InjectMocks
    private RestaurantJpaAdapter restaurantJpaAdapter;

    @Test
    void saveRestaurantShouldMapSaveAndMapBack(){
        RestaurantModel model = new RestaurantModel();
        RestaurantEntity entity = new RestaurantEntity();
        RestaurantModel savedModel = new RestaurantModel();
        RestaurantEntity savedEntity = new RestaurantEntity();

        when(restaurantEntityMapper.toEntity(model)).thenReturn(entity);
        when(restaurantRepository.save(entity)).thenReturn(savedEntity);
        when(restaurantEntityMapper.toRestaurantModel(savedEntity)).thenReturn(savedModel);

        RestaurantModel result = restaurantJpaAdapter.saveRestaurant(model);

        assertSame(savedModel, result);
    }

    @Test
    void existsByNitShouldReturnWhatTheRepositoryReturns(){
        when(restaurantRepository.existsByNit("900123456")).thenReturn(true);
        when(restaurantRepository.existsByNit("111")).thenReturn(false);

        assertTrue(restaurantJpaAdapter.existsByNit("900123456"));
        assertFalse(restaurantJpaAdapter.existsByNit("111"));
    }

    @Test
    void findByIdShouldReturnTheMappedRestaurantWhenItExists(){
        RestaurantEntity entity = new RestaurantEntity();
        RestaurantModel model = new RestaurantModel();

        when(restaurantRepository.findById(1L)).thenReturn(Optional.of(entity));
        when(restaurantEntityMapper.toRestaurantModel(entity)).thenReturn(model);

        Optional<RestaurantModel> result = restaurantJpaAdapter.findById(1L);

        assertTrue(result.isPresent());
        assertSame(model, result.get());
    }

    @Test
    void findByIdShouldReturnEmptyWhenTheRestaurantDoesNotExist(){
        when(restaurantRepository.findById(99L)).thenReturn(Optional.empty());

        assertTrue(restaurantJpaAdapter.findById(99L).isEmpty());
    }


}