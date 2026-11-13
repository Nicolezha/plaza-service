package com.pragma.powerup.domain.util;

public class RestaurantConstants {

    private RestaurantConstants() {
        /* This utility class should not be instantiated */
    }

    public static final String NIT_ALREADY_EXISTS_MESSAGE =
            "A restaurant with this NIT already exists";

    public static final String NIT_REGEX = "^\\d+$";
    public static final String NIT_INVALID_MESSAGE = "NIT must contain only numbers";

    public static final int PHONE_MAX_LENGTH = 13;
    public static final String PHONE_REGEX = "^\\+?\\d+$";
    public static final String PHONE_INVALID_MESSAGE =
            "Phone must contain only numbers, may start with + and have at most 13 characters";

    public static final String NAME_LETTER_REGEX = "\\p{L}";
    public static final String NAME_INVALID_MESSAGE =
            "The restaurant's name must contain at least one letter";

    public static final String OWNER_NOT_FOUND_MESSAGE =
            "The owner with the given id does not exist";

    public static final String USER_NOT_OWNER_MESSAGE =
            "The user with the given id does not have the OWNER role";

    public static final String USER_SERVICE_UNAVAILABLE_MESSAGE =
            "The user service is currently unavailable, please try again later";
}
