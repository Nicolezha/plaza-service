package com.pragma.powerup.application.handler.impl;

import com.pragma.powerup.application.dto.request.DishRequestDto;
import com.pragma.powerup.application.dto.request.DishStatusRequestDto;
import com.pragma.powerup.application.dto.request.DishUpdateRequestDto;
import com.pragma.powerup.application.dto.request.PageRequestDto;
import com.pragma.powerup.application.dto.response.DishListResponseDto;
import com.pragma.powerup.application.dto.response.PageResponseDto;
import com.pragma.powerup.application.handler.IDishHandler;
import com.pragma.powerup.application.mapper.IDishRequestMapper;
import com.pragma.powerup.application.mapper.IDishResponseMapper;
import com.pragma.powerup.domain.api.IDishServicePort;
import com.pragma.powerup.domain.model.DishModel;
import com.pragma.powerup.domain.model.PageModel;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class DishHandler implements IDishHandler {

    private final IDishServicePort dishServicePort;
    private final IDishRequestMapper dishRequestMapper;
    private final IDishResponseMapper dishResponseMapper;

    @Override
    public void saveDish(DishRequestDto dishRequestDto, Long requesterId) {
        DishModel dish = dishRequestMapper.toDish(dishRequestDto);

        dishServicePort.saveDish(dish, requesterId);
    }

    @Override
    public void updateDish(Long dishId, DishUpdateRequestDto dishUpdateRequestDto, Long requesterId) {
        dishServicePort.updateDish(dishId,
                dishUpdateRequestDto.getPrice(),
                dishUpdateRequestDto.getDescription(),
                requesterId);
    }

    @Override
    public void updateDishStatus(Long dishId, DishStatusRequestDto dishStatusRequestDto, Long requesterId) {
        dishServicePort.updateDishStatus(dishId,
                dishStatusRequestDto.getActive(),
                requesterId);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponseDto<DishListResponseDto> getDishesByRestaurant(Long restaurantId, Long categoryId, PageRequestDto pageRequestDto) {
        PageModel<DishModel> pageModel = dishServicePort.getDishesByRestaurant(restaurantId,
                categoryId, pageRequestDto.getPage(), pageRequestDto.getSize());

        List<DishListResponseDto> content = pageModel.getContent().stream()
                .map(dishResponseMapper::toListResponse).toList();

        return  new PageResponseDto<>(content, pageModel.getPage(), pageModel.getSize(),
                pageModel.getTotalElements(), pageModel.getTotalPages());
    }
}
