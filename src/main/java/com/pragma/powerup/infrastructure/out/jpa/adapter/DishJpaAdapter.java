package com.pragma.powerup.infrastructure.out.jpa.adapter;

import com.pragma.powerup.domain.model.DishModel;
import com.pragma.powerup.domain.model.PageModel;
import com.pragma.powerup.domain.spi.IDishPersistencePort;
import com.pragma.powerup.infrastructure.out.jpa.entity.DishEntity;
import com.pragma.powerup.infrastructure.out.jpa.mapper.IDishEntityMapper;
import com.pragma.powerup.infrastructure.out.jpa.repository.IDishRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
public class DishJpaAdapter implements IDishPersistencePort {

    private final IDishRepository dishRepository;
    private final IDishEntityMapper dishEntityMapper;

    @Override
    public PageModel<DishModel> findByRestaurant(Long restaurantId, Long categoryId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("name").ascending());

        Page<DishEntity> result = (categoryId == null)
                ? dishRepository.findByRestaurant_IdAndActiveTrue(restaurantId, pageable)
                : dishRepository.findByRestaurant_IdAndCategory_IdAndActiveTrue(restaurantId, categoryId, pageable);

        List<DishModel> content = result.getContent().stream()
                .map(dishEntityMapper::toDishModel)
                .toList();

        return new PageModel<>(content, result.getNumber(), result.getSize(),
                result.getTotalElements(), result.getTotalPages());
    }

    @Override
    public DishModel saveDish(DishModel dish) {
        DishEntity dishEntity = dishRepository.save(dishEntityMapper.toDishEntity(dish));
        return dishEntityMapper.toDishModel(dishEntity);
    }

    @Override
    public Optional<DishModel> findById(Long id) {
        return dishRepository.findById(id)
                .map(dishEntityMapper::toDishModel);
    }
}
