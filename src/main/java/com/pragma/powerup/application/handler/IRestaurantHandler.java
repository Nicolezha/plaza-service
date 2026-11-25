package com.pragma.powerup.application.handler;

import com.pragma.powerup.application.dto.request.EmployeeAssignmentRequestDto;
import com.pragma.powerup.application.dto.request.PageRequestDto;
import com.pragma.powerup.application.dto.request.RestaurantRequestDto;
import com.pragma.powerup.application.dto.response.PageResponseDto;
import com.pragma.powerup.application.dto.response.RestaurantListResponseDto;

public interface IRestaurantHandler {

    void saveRestaurant(RestaurantRequestDto restaurantRequestDto);
    PageResponseDto<RestaurantListResponseDto> getRestaurants(PageRequestDto pageRequest);
    void assignEmployee(Long restaurantId, EmployeeAssignmentRequestDto employeeAssignmentRequestDto, Long requesterId);
}