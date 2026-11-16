package com.pragma.powerup.domain.util;

public class DishConstants {

    private DishConstants() {
    }

    public static final String PRICE_INVALID_MESSAGE =
            "Price must be an integer greater than 0";

    public static final String RESTAURANT_NOT_FOUND_MESSAGE =
            "The restaurant with the given id does not exist";

    public static final String NOT_RESTAURANT_OWNER_MESSAGE =
            "Only the owner of the restaurant can manage its dishes";

    public static final String CATEGORY_NOT_FOUND_MESSAGE =
            "The category with the given id does not exist";

    public static final String DISH_NOT_FOUND_MESSAGE =
            "The dish with the given id does not exist";
}
