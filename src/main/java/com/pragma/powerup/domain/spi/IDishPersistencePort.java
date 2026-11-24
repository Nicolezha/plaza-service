package com.pragma.powerup.domain.spi;

import com.pragma.powerup.domain.model.DishModel;
import com.pragma.powerup.domain.model.PageModel;

import java.util.Optional;

public interface IDishPersistencePort {

    PageModel<DishModel> findByRestaurant(Long restaurantId, Long categoryId, int page, int size);

    DishModel saveDish(DishModel dish);

    Optional<DishModel> findById(Long id);
}
