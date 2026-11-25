package com.pragma.powerup.application.handler.impl;

import com.pragma.powerup.application.dto.request.EmployeeAssignmentRequestDto;
import com.pragma.powerup.application.dto.request.PageRequestDto;
import com.pragma.powerup.application.dto.request.RestaurantRequestDto;
import com.pragma.powerup.application.dto.response.PageResponseDto;
import com.pragma.powerup.application.dto.response.RestaurantListResponseDto;
import com.pragma.powerup.application.mapper.IRestaurantRequestMapper;
import com.pragma.powerup.application.mapper.IRestaurantResponseMapper;
import com.pragma.powerup.domain.api.IRestaurantServicePort;
import com.pragma.powerup.domain.model.PageModel;
import com.pragma.powerup.domain.model.RestaurantModel;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RestaurantHandlerTest {

    @Mock
    private IRestaurantServicePort restaurantServicePort;

    @Mock
    private IRestaurantRequestMapper restaurantRequestMapper;

    @Mock
    private IRestaurantResponseMapper restaurantResponseMapper;

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

    @Test
    void getRestaurantsShouldMapContentAndCopyPaginationData() {
        PageRequestDto pageRequest = new PageRequestDto();
        pageRequest.setPage(1);
        pageRequest.setSize(2);

        RestaurantModel model = new RestaurantModel();
        RestaurantListResponseDto dto = new RestaurantListResponseDto();
        dto.setName("El Corral 24");
        dto.setLogoUrl("https://logos.com/corral.png");

        PageModel<RestaurantModel> pageModel = new PageModel<>(List.of(model), 1, 2, 3L, 2);

        when(restaurantServicePort.getRestaurants(1, 2)).thenReturn(pageModel);
        when(restaurantResponseMapper.toListResponse(model)).thenReturn(dto);

        PageResponseDto<RestaurantListResponseDto> result = restaurantHandler.getRestaurants(pageRequest);

        assertEquals(1, result.getContent().size());
        assertSame(dto, result.getContent().get(0));
        assertEquals(1, result.getPage());
        assertEquals(2, result.getSize());
        assertEquals(3L, result.getTotalElements());
        assertEquals(2, result.getTotalPages());
    }

    @Test
    void assignEmployeeShouldDelegateWithRestaurantEmployeeAndRequester() {
        EmployeeAssignmentRequestDto request = new EmployeeAssignmentRequestDto();
        request.setEmployeeId(30L);

        restaurantHandler.assignEmployee(10L, request, 1L);

        verify(restaurantServicePort).assignEmployee(10L, 30L, 1L);
    }
}
