package com.pragma.powerup.application.handler.impl;

import com.pragma.powerup.application.dto.request.DishRequestDto;
import com.pragma.powerup.application.dto.request.DishStatusRequestDto;
import com.pragma.powerup.application.dto.request.DishUpdateRequestDto;
import com.pragma.powerup.application.handler.IDishHandler;
import com.pragma.powerup.application.mapper.IDishRequestMapper;
import com.pragma.powerup.domain.api.IDishServicePort;
import com.pragma.powerup.domain.model.DishModel;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class DishHandler implements IDishHandler {

    private final IDishServicePort dishServicePort;
    private final IDishRequestMapper dishRequestMapper;

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
}
