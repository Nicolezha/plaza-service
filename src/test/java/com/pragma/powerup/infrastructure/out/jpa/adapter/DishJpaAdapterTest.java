package com.pragma.powerup.infrastructure.out.jpa.adapter;

import com.pragma.powerup.domain.model.DishModel;
import com.pragma.powerup.infrastructure.out.jpa.entity.DishEntity;
import com.pragma.powerup.infrastructure.out.jpa.mapper.IDishEntityMapper;
import com.pragma.powerup.infrastructure.out.jpa.repository.IDishRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
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
}
