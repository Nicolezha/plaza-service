package com.pragma.powerup.domain.usecase;

import com.pragma.powerup.domain.api.IRestaurantServicePort;
import com.pragma.powerup.domain.exception.RestaurantAlreadyExistsException;
import com.pragma.powerup.domain.exception.UserNotOwnerException;
import com.pragma.powerup.domain.model.RestaurantModel;
import com.pragma.powerup.domain.model.Role;
import com.pragma.powerup.domain.spi.IRestaurantPersistencePort;
import com.pragma.powerup.domain.spi.IUserClientPort;
import com.pragma.powerup.domain.validation.RestaurantValidator;


import static com.pragma.powerup.domain.util.RestaurantConstants.NIT_ALREADY_EXISTS_MESSAGE;
import static com.pragma.powerup.domain.util.RestaurantConstants.USER_NOT_OWNER_MESSAGE;

public class RestaurantUseCase implements IRestaurantServicePort {

    private final IRestaurantPersistencePort restaurantPersistencePort;
    private final IUserClientPort userClientPort;
    private final RestaurantValidator restaurantValidator;

    public RestaurantUseCase(IRestaurantPersistencePort restaurantPersistencePort,
                             RestaurantValidator restaurantValidator,
                             IUserClientPort userClientPort) {
        this.restaurantPersistencePort = restaurantPersistencePort;
        this.restaurantValidator = restaurantValidator;
        this.userClientPort = userClientPort;
    }

    @Override
    public void saveRestaurant(RestaurantModel restaurantModel) {
        restaurantValidator.validate(restaurantModel);
        if(!Role.OWNER.equals(userClientPort.getUserRole(restaurantModel.getOwnerId()))){
            throw new UserNotOwnerException(USER_NOT_OWNER_MESSAGE);
        }
        if (restaurantPersistencePort.existsByNit(restaurantModel.getNit())) {
            throw new RestaurantAlreadyExistsException(NIT_ALREADY_EXISTS_MESSAGE);
        }
        restaurantPersistencePort.saveRestaurant(restaurantModel);
    }
}