package com.pragma.powerup.domain.api;

import com.pragma.powerup.domain.model.DishModel;

public interface IDishServicePort {

    void saveDish(DishModel dish, Long requesterId);

    void updateDish(Long dishId, Integer price, String description, Long requesterId);

    void updateDishStatus(Long dishId, Boolean active, Long requesterId);
}
