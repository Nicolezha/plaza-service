package com.pragma.powerup.domain.usecase;

import com.pragma.powerup.domain.api.IDishServicePort;
import com.pragma.powerup.domain.exception.*;
import com.pragma.powerup.domain.model.DishModel;
import com.pragma.powerup.domain.model.PageModel;
import com.pragma.powerup.domain.model.RestaurantModel;
import com.pragma.powerup.domain.spi.ICategoryPersistencePort;
import com.pragma.powerup.domain.spi.IDishPersistencePort;
import com.pragma.powerup.domain.spi.IRestaurantPersistencePort;
import com.pragma.powerup.domain.validation.DishValidator;
import com.pragma.powerup.domain.validation.PaginationValidator;

import static com.pragma.powerup.domain.util.DishConstants.*;

public class DishUseCase implements IDishServicePort {

    private final IDishPersistencePort dishPersistencePort;
    private final ICategoryPersistencePort categoryPersistencePort;
    private final DishValidator dishValidator;
    private final IRestaurantPersistencePort restaurantPersistencePort;
    private final PaginationValidator paginationValidator;

    public DishUseCase(IDishPersistencePort dishPersistencePort,
                       ICategoryPersistencePort categoryPersistencePort,
                       DishValidator dishValidator,
                       IRestaurantPersistencePort restaurantPersistencePort, PaginationValidator paginationValidator) {
        this.dishPersistencePort = dishPersistencePort;
        this.categoryPersistencePort = categoryPersistencePort;
        this.dishValidator = dishValidator;
        this.restaurantPersistencePort = restaurantPersistencePort;
        this.paginationValidator = paginationValidator;
    }

    @Override
    public void saveDish(DishModel dish, Long requesterId) {
        dishValidator.validate(dish);

        RestaurantModel restaurant = restaurantPersistencePort.findById(dish.getRestaurantId())
                .orElseThrow(() -> new RestaurantNotFoundException(RESTAURANT_NOT_FOUND_MESSAGE));

        if(!restaurant.getOwnerId().equals(requesterId)){
            throw new NotRestaurantOwnerException(NOT_RESTAURANT_OWNER_MESSAGE);
        }

        if(!categoryPersistencePort.existsById(dish.getCategoryId())){
            throw new CategoryNotFoundException(CATEGORY_NOT_FOUND_MESSAGE);
        }

        dish.setActive(true);

        dishPersistencePort.saveDish(dish);
    }

    @Override
    public void updateDish(Long dishId, Integer price, String description, Long requesterId) {
        DishModel dish = findDishOwnedBy(dishId, requesterId);

        dish.setPrice(price);
        dish.setDescription(description);

        dishValidator.validate(dish);

        dishPersistencePort.saveDish(dish);
    }

    @Override
    public void updateDishStatus(Long dishId, Boolean active, Long requesterId) {
        DishModel dish = findDishOwnedBy(dishId, requesterId);

        dish.setActive(active);

        dishPersistencePort.saveDish(dish);
    }

    @Override
    public PageModel<DishModel> getDishesByRestaurant(Long restaurantId, Long categoryId, int page, int size) {
        paginationValidator.validatePagination(page, size);
        if (restaurantPersistencePort.findById(restaurantId).isEmpty()) {
            throw new RestaurantNotFoundException(RESTAURANT_NOT_FOUND_MESSAGE);
        }
        return dishPersistencePort.findByRestaurant(restaurantId, categoryId, page, size);
    }

    private DishModel findDishOwnedBy(Long dishId, Long requesterId) {
        DishModel dish = dishPersistencePort.findById(dishId)
                .orElseThrow(() -> new DishNotFoundException(DISH_NOT_FOUND_MESSAGE));

        RestaurantModel restaurant = restaurantPersistencePort.findById(dish.getRestaurantId())
                .orElseThrow(() -> new RestaurantNotFoundException(RESTAURANT_NOT_FOUND_MESSAGE));

        if(!restaurant.getOwnerId().equals(requesterId)){
            throw new NotRestaurantOwnerException(NOT_RESTAURANT_OWNER_MESSAGE);
        }

        return dish;
    }
}
