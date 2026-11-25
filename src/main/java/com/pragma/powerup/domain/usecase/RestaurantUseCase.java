package com.pragma.powerup.domain.usecase;

import com.pragma.powerup.domain.api.IRestaurantServicePort;
import com.pragma.powerup.domain.exception.EmployeeAlreadyAssignedException;
import com.pragma.powerup.domain.exception.NotRestaurantOwnerException;
import com.pragma.powerup.domain.exception.RestaurantAlreadyExistsException;
import com.pragma.powerup.domain.exception.RestaurantNotFoundException;
import com.pragma.powerup.domain.exception.UserNotOwnerException;
import com.pragma.powerup.domain.model.EmployeeRestaurantModel;
import com.pragma.powerup.domain.model.PageModel;
import com.pragma.powerup.domain.model.RestaurantModel;
import com.pragma.powerup.domain.model.Role;
import com.pragma.powerup.domain.spi.IEmployeeRestaurantPersistencePort;
import com.pragma.powerup.domain.spi.IRestaurantPersistencePort;
import com.pragma.powerup.domain.spi.IUserClientPort;
import com.pragma.powerup.domain.validation.PaginationValidator;
import com.pragma.powerup.domain.validation.RestaurantValidator;

import static com.pragma.powerup.domain.util.DishConstants.RESTAURANT_NOT_FOUND_MESSAGE;
import static com.pragma.powerup.domain.util.EmployeeConstants.*;
import static com.pragma.powerup.domain.util.RestaurantConstants.NIT_ALREADY_EXISTS_MESSAGE;
import static com.pragma.powerup.domain.util.RestaurantConstants.USER_NOT_OWNER_MESSAGE;

public class RestaurantUseCase implements IRestaurantServicePort {

    private final IRestaurantPersistencePort restaurantPersistencePort;
    private final IUserClientPort userClientPort;
    private final RestaurantValidator restaurantValidator;
    private final PaginationValidator paginationValidator;
    private final IEmployeeRestaurantPersistencePort employeeRestaurantPersistencePort;

    public RestaurantUseCase(IRestaurantPersistencePort restaurantPersistencePort,
                             RestaurantValidator restaurantValidator,
                             IUserClientPort userClientPort, PaginationValidator paginationValidator,
                             IEmployeeRestaurantPersistencePort employeeRestaurantPersistencePort) {
        this.restaurantPersistencePort = restaurantPersistencePort;
        this.restaurantValidator = restaurantValidator;
        this.userClientPort = userClientPort;
        this.paginationValidator = paginationValidator;
        this.employeeRestaurantPersistencePort = employeeRestaurantPersistencePort;
    }

    @Override
    public PageModel<RestaurantModel> getRestaurants(int page, int size) {
        paginationValidator.validatePagination(page, size);
        return restaurantPersistencePort.findAllOrderedByName(page, size);
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

    @Override
    public void assignEmployee(Long restaurantId, Long employeeId, Long requesterId) {
        RestaurantModel restaurant = restaurantPersistencePort.findById(restaurantId)
                .orElseThrow(() -> new RestaurantNotFoundException(RESTAURANT_NOT_FOUND_MESSAGE));

        if (!restaurant.getOwnerId().equals(requesterId)) {
            throw new NotRestaurantOwnerException(NOT_RESTAURANT_OWNER_EMPLOYEE_MESSAGE);
        }

        if (employeeRestaurantPersistencePort.existsByEmployeeId(employeeId)) {
            throw new EmployeeAlreadyAssignedException(EMPLOYEE_ALREADY_ASSIGNED_MESSAGE);
        }

        employeeRestaurantPersistencePort.saveEmployeeRestaurant(
                new EmployeeRestaurantModel(employeeId, restaurantId));
    }
}
