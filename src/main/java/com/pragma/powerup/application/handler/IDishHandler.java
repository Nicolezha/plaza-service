package com.pragma.powerup.application.handler;


import com.pragma.powerup.application.dto.request.DishRequestDto;
import com.pragma.powerup.application.dto.request.DishStatusRequestDto;
import com.pragma.powerup.application.dto.request.DishUpdateRequestDto;
import com.pragma.powerup.application.dto.request.PageRequestDto;
import com.pragma.powerup.application.dto.response.DishListResponseDto;
import com.pragma.powerup.application.dto.response.PageResponseDto;

public interface IDishHandler {

    void saveDish(DishRequestDto dishRequestDto, Long requesterId);

    void updateDish(Long dishId, DishUpdateRequestDto dishUpdateRequestDto, Long requesterId);

    void updateDishStatus(Long dishId, DishStatusRequestDto dishStatusRequestDto, Long requesterId);

    PageResponseDto<DishListResponseDto> getDishesByRestaurant(Long restaurantId, Long categoryId, PageRequestDto pageRequestDto);
}