package com.pragma.powerup.domain.api;

import com.pragma.powerup.domain.model.PageModel;
import com.pragma.powerup.domain.model.RestaurantModel;

public interface IRestaurantServicePort {

    PageModel<RestaurantModel> getRestaurants(int page, int size);

    void saveRestaurant(RestaurantModel restaurantModel);
}