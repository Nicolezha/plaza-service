package com.pragma.powerup.domain.api;

import com.pragma.powerup.domain.model.DishModel;
import com.pragma.powerup.domain.model.PageModel;

public interface IDishServicePort {

    void saveDish(DishModel dish, Long requesterId);

    void updateDish(Long dishId, Integer price, String description, Long requesterId);

    void updateDishStatus(Long dishId, Boolean active, Long requesterId);

    PageModel<DishModel> getDishesByRestaurant(Long restaurantId, Long categoryId, int page, int size);
}
