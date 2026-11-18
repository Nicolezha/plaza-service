package com.pragma.powerup.application.handler.impl;

import com.pragma.powerup.application.dto.request.DishRequestDto;
import com.pragma.powerup.application.dto.request.DishStatusRequestDto;
import com.pragma.powerup.application.dto.request.DishUpdateRequestDto;
import com.pragma.powerup.application.mapper.IDishRequestMapper;
import com.pragma.powerup.domain.api.IDishServicePort;
import com.pragma.powerup.domain.model.DishModel;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DishHandlerTest {

    @Mock
    private IDishServicePort dishServicePort;

    @Mock
    private IDishRequestMapper dishRequestMapper;

    @InjectMocks
    private DishHandler dishHandler;

    @Test
    void saveDishShouldMapRequestAndDelegateWithTheRequesterId() {
        DishRequestDto request = new DishRequestDto();
        DishModel model = new DishModel();
        when(dishRequestMapper.toDish(request)).thenReturn(model);

        dishHandler.saveDish(request, 1L);

        verify(dishServicePort).saveDish(model, 1L);
    }

    @Test
    void updateDishShouldDelegateTheDtoFieldsToThePort() {
        DishUpdateRequestDto request = new DishUpdateRequestDto();
        request.setPrice(30000);
        request.setDescription("Nueva descripcion");

        dishHandler.updateDish(5L, request, 1L);

        verify(dishServicePort).updateDish(5L, 30000, "Nueva descripcion", 1L);
    }

    @Test
    void updateDishStatusShouldDelegateTheDtoFieldToThePort() {
        DishStatusRequestDto request = new DishStatusRequestDto();
        request.setActive(false);

        dishHandler.updateDishStatus(5L, request, 1L);

        verify(dishServicePort).updateDishStatus(5L, false, 1L);
    }
}
