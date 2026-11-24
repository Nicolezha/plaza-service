package com.pragma.powerup.application.handler.impl;

import com.pragma.powerup.application.dto.request.DishRequestDto;
import com.pragma.powerup.application.dto.request.DishStatusRequestDto;
import com.pragma.powerup.application.dto.request.DishUpdateRequestDto;
import com.pragma.powerup.application.dto.request.PageRequestDto;
import com.pragma.powerup.application.dto.response.DishListResponseDto;
import com.pragma.powerup.application.dto.response.PageResponseDto;
import com.pragma.powerup.application.mapper.IDishRequestMapper;
import com.pragma.powerup.application.mapper.IDishResponseMapper;
import com.pragma.powerup.domain.api.IDishServicePort;
import com.pragma.powerup.domain.model.DishModel;
import com.pragma.powerup.domain.model.PageModel;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DishHandlerTest {

    @Mock
    private IDishServicePort dishServicePort;

    @Mock
    private IDishRequestMapper dishRequestMapper;

    @Mock
    private IDishResponseMapper dishResponseMapper;

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

    @Test
    void getDishesByRestaurantShouldMapContentAndCopyPaginationData() {
        PageRequestDto pageRequest = new PageRequestDto();
        pageRequest.setPage(1);
        pageRequest.setSize(2);

        DishModel model = new DishModel();
        DishListResponseDto dto = new DishListResponseDto();
        PageModel<DishModel> pageModel = new PageModel<>(List.of(model), 1, 2, 3L, 2);

        when(dishServicePort.getDishesByRestaurant(10L, 3L, 1, 2)).thenReturn(pageModel);
        when(dishResponseMapper.toListResponse(model)).thenReturn(dto);

        PageResponseDto<DishListResponseDto> result =
                dishHandler.getDishesByRestaurant(10L, 3L, pageRequest);

        assertEquals(1, result.getContent().size());
        assertSame(dto, result.getContent().get(0));
        assertEquals(1, result.getPage());
        assertEquals(2, result.getSize());
        assertEquals(3L, result.getTotalElements());
        assertEquals(2, result.getTotalPages());
    }
}
