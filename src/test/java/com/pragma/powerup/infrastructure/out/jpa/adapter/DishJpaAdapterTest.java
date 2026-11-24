package com.pragma.powerup.infrastructure.out.jpa.adapter;

import com.pragma.powerup.domain.model.DishModel;
import com.pragma.powerup.domain.model.PageModel;
import com.pragma.powerup.infrastructure.out.jpa.entity.DishEntity;
import com.pragma.powerup.infrastructure.out.jpa.mapper.IDishEntityMapper;
import com.pragma.powerup.infrastructure.out.jpa.repository.IDishRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DishJpaAdapterTest {

    @Mock
    private IDishRepository dishRepository;

    @Mock
    private IDishEntityMapper dishEntityMapper;

    @InjectMocks
    private DishJpaAdapter dishJpaAdapter;

    @Test
    void saveDishShouldMapSaveAndMapBack() {
        DishModel model = new DishModel();
        DishEntity entity = new DishEntity();
        DishEntity savedEntity = new DishEntity();
        DishModel savedModel = new DishModel();

        when(dishEntityMapper.toDishEntity(model)).thenReturn(entity);
        when(dishRepository.save(entity)).thenReturn(savedEntity);
        when(dishEntityMapper.toDishModel(savedEntity)).thenReturn(savedModel);

        DishModel result = dishJpaAdapter.saveDish(model);

        assertSame(savedModel, result);
    }

    @Test
    void findByIdShouldReturnTheMappedModelWhenTheDishExists() {
        DishEntity entity = new DishEntity();
        DishModel model = new DishModel();

        when(dishRepository.findById(5L)).thenReturn(Optional.of(entity));
        when(dishEntityMapper.toDishModel(entity)).thenReturn(model);

        Optional<DishModel> result = dishJpaAdapter.findById(5L);

        assertTrue(result.isPresent());
        assertSame(model, result.get());
    }

    @Test
    void findByIdShouldReturnEmptyWhenTheDishDoesNotExist() {
        when(dishRepository.findById(5L)).thenReturn(Optional.empty());

        Optional<DishModel> result = dishJpaAdapter.findById(5L);

        assertTrue(result.isEmpty());
        verifyNoInteractions(dishEntityMapper);
    }

    @Test
    void findByRestaurantShouldUseQueryWithoutCategoryWhenCategoryIsNull() {
        DishEntity entity = new DishEntity();
        DishModel model = new DishModel();
        Page<DishEntity> springPage = new PageImpl<>(List.of(entity), PageRequest.of(1, 2), 3);

        when(dishRepository.findByRestaurant_IdAndActiveTrue(eq(10L), any(Pageable.class)))
                .thenReturn(springPage);
        when(dishEntityMapper.toDishModel(entity)).thenReturn(model);

        PageModel<DishModel> result = dishJpaAdapter.findByRestaurant(10L, null, 1, 2);

        ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
        verify(dishRepository).findByRestaurant_IdAndActiveTrue(eq(10L), captor.capture());
        Pageable pageable = captor.getValue();
        assertEquals(1, pageable.getPageNumber());
        assertEquals(2, pageable.getPageSize());
        assertEquals(Sort.by("name").ascending(), pageable.getSort());
        verify(dishRepository, never())
                .findByRestaurant_IdAndCategory_IdAndActiveTrue(anyLong(), anyLong(), any(Pageable.class));

        assertEquals(List.of(model), result.getContent());
        assertEquals(1, result.getPage());
        assertEquals(2, result.getSize());
        assertEquals(3L, result.getTotalElements());
        assertEquals(2, result.getTotalPages());
    }

    @Test
    void findByRestaurantShouldUseQueryWithCategoryWhenCategoryIsGiven() {
        DishEntity entity = new DishEntity();
        DishModel model = new DishModel();
        Page<DishEntity> springPage = new PageImpl<>(List.of(entity), PageRequest.of(0, 10), 1);

        when(dishRepository.findByRestaurant_IdAndCategory_IdAndActiveTrue(eq(10L), eq(3L), any(Pageable.class)))
                .thenReturn(springPage);
        when(dishEntityMapper.toDishModel(entity)).thenReturn(model);

        PageModel<DishModel> result = dishJpaAdapter.findByRestaurant(10L, 3L, 0, 10);

        assertEquals(List.of(model), result.getContent());
        assertEquals(1L, result.getTotalElements());
        verify(dishRepository, never()).findByRestaurant_IdAndActiveTrue(anyLong(), any(Pageable.class));
    }
}
