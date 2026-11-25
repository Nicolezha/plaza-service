package com.pragma.powerup.application.handler.impl;

import com.pragma.powerup.application.dto.request.EmployeeAssignmentRequestDto;
import com.pragma.powerup.application.dto.request.PageRequestDto;
import com.pragma.powerup.application.dto.request.RestaurantRequestDto;
import com.pragma.powerup.application.dto.response.PageResponseDto;
import com.pragma.powerup.application.dto.response.RestaurantListResponseDto;
import com.pragma.powerup.application.handler.IRestaurantHandler;
import com.pragma.powerup.application.mapper.IRestaurantRequestMapper;
import com.pragma.powerup.application.mapper.IRestaurantResponseMapper;
import com.pragma.powerup.domain.api.IRestaurantServicePort;
import com.pragma.powerup.domain.model.PageModel;
import com.pragma.powerup.domain.model.RestaurantModel;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class RestaurantHandler implements IRestaurantHandler {

    private final IRestaurantServicePort restaurantServicePort;
    private final IRestaurantRequestMapper restaurantRequestMapper;
    private final IRestaurantResponseMapper restaurantResponseMapper;

    @Override
    public void saveRestaurant(RestaurantRequestDto restaurantRequestDto) {
        RestaurantModel restaurantModel = restaurantRequestMapper.toRestaurant(restaurantRequestDto);
        restaurantServicePort.saveRestaurant(restaurantModel);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponseDto<RestaurantListResponseDto> getRestaurants(PageRequestDto pageRequest) {
        PageModel<RestaurantModel> pageModel =
                restaurantServicePort.getRestaurants(pageRequest.getPage(), pageRequest.getSize());

        List<RestaurantListResponseDto> content = pageModel.getContent().stream()
                .map(restaurantResponseMapper::toListResponse)
                .toList();

        return new PageResponseDto<>(content, pageModel.getPage(), pageModel.getSize(),
                pageModel.getTotalElements(), pageModel.getTotalPages());
    }

    @Override
    public void assignEmployee(Long restaurantId, EmployeeAssignmentRequestDto employeeAssignmentRequestDto,
                               Long requesterId) {
        restaurantServicePort.assignEmployee(restaurantId,
                employeeAssignmentRequestDto.getEmployeeId(), requesterId);
    }
}
