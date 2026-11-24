package com.pragma.powerup.domain.spi;

import com.pragma.powerup.domain.model.PageModel;
import com.pragma.powerup.domain.model.RestaurantModel;

import java.util.Optional;

public interface IRestaurantPersistencePort {

    PageModel<RestaurantModel> findAllOrderedByName(int page, int size);

    RestaurantModel saveRestaurant(RestaurantModel restaurantModel);

    boolean existsByNit(String nit);

    Optional<RestaurantModel> findById(Long id);
}