package com.pragma.powerup.application.handler.impl;

import com.pragma.powerup.application.dto.request.RestaurantRequestDto;
import com.pragma.powerup.application.mapper.IRestaurantRequestMapper;
import com.pragma.powerup.domain.api.IRestaurantServicePort;
import com.pragma.powerup.domain.model.RestaurantModel;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RestaurantHandlerTest {

    @Mock
    private IRestaurantServicePort restaurantServicePort;

    @Mock
    private IRestaurantRequestMapper restaurantRequestMapper;

    @InjectMocks
    RestaurantHandler restaurantHandler;

    @Test
    void saveRestaurantShouldMapRequestAndDelegateToServicePort() {
        RestaurantRequestDto request = new RestaurantRequestDto();
        RestaurantModel model = new RestaurantModel();
        when(restaurantRequestMapper.toRestaurant(request)).thenReturn(model);

        restaurantHandler.saveRestaurant(request);

        verify(restaurantServicePort).saveRestaurant(model);
    }
}