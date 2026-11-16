package com.pragma.powerup.domain.usecase;

import com.pragma.powerup.domain.api.IDishServicePort;
import com.pragma.powerup.domain.exception.CategoryNotFoundException;
import com.pragma.powerup.domain.exception.NotRestaurantOwnerException;
import com.pragma.powerup.domain.exception.RestaurantNotFoundException;
import com.pragma.powerup.domain.model.DishModel;
import com.pragma.powerup.domain.model.RestaurantModel;
import com.pragma.powerup.domain.spi.ICategoryPersistencePort;
import com.pragma.powerup.domain.spi.IDishPersistencePort;
import com.pragma.powerup.domain.spi.IRestaurantPersistencePort;
import com.pragma.powerup.domain.validation.DishValidator;

import static com.pragma.powerup.domain.util.DishConstants.*;

public class DishUseCase implements IDishServicePort {

    private final IDishPersistencePort dishPersistencePort;
    private final ICategoryPersistencePort categoryPersistencePort;
    private final DishValidator dishValidator;
    private final IRestaurantPersistencePort restaurantPersistencePort;

    public DishUseCase(IDishPersistencePort dishPersistencePort,
                       ICategoryPersistencePort categoryPersistencePort,
                       DishValidator dishValidator,
                       IRestaurantPersistencePort restaurantPersistencePort) {
        this.dishPersistencePort = dishPersistencePort;
        this.categoryPersistencePort = categoryPersistencePort;
        this.dishValidator = dishValidator;
        this.restaurantPersistencePort = restaurantPersistencePort;
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
}
