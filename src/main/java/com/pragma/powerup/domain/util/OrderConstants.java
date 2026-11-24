package com.pragma.powerup.domain.util;

public class OrderConstants {

    private OrderConstants() {
    }

    public static final String DISHES_REQUIRED_MESSAGE =
            "The order must contain at least one dish";

    public static final String QUANTITY_INVALID_MESSAGE =
            "The quantity of each dish must be an integer greater than 0";

    public static final String DUPLICATED_DISH_MESSAGE =
            "Each dish can only appear once in the order";

    public static final String ORDER_IN_PROGRESS_MESSAGE =
            "The client already has an order in progress";

    public static final String DISH_NOT_FROM_RESTAURANT_MESSAGE =
            "All dishes must belong to the restaurant of the order";

    public static final String DISH_NOT_AVAILABLE_MESSAGE =
            "The order contains a dish that is not available";
}
