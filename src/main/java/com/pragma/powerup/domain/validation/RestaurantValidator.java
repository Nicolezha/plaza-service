package com.pragma.powerup.domain.validation;

import com.pragma.powerup.domain.exception.InvalidRestaurantDataException;
import com.pragma.powerup.domain.model.RestaurantModel;

import java.util.regex.Pattern;

import static com.pragma.powerup.domain.util.RestaurantConstants.*;

public class RestaurantValidator {

    private static final Pattern NIT_PATTERN = Pattern.compile(NIT_REGEX);
    private static final Pattern PHONE_PATTERN = Pattern.compile(PHONE_REGEX);
    private static final Pattern NAME_LETTER_PATTERN = Pattern.compile(NAME_LETTER_REGEX);

    public void validate(RestaurantModel restaurant){
        validateNit(restaurant.getNit());
        validatePhone(restaurant.getPhone());
        validateName(restaurant.getName());
    }

    private void validateNit(String nit) {
        if (nit == null || !NIT_PATTERN.matcher(nit).matches()) {
            throw new InvalidRestaurantDataException(NIT_INVALID_MESSAGE);
        }
    }

    private void validatePhone(String phone) {
        if (phone == null || phone.length() > PHONE_MAX_LENGTH
                || !PHONE_PATTERN.matcher(phone).matches()) {
            throw new InvalidRestaurantDataException(PHONE_INVALID_MESSAGE);
        }
    }

    private void validateName(String name) {
        if (name == null || !NAME_LETTER_PATTERN.matcher(name).find()) {
            throw new InvalidRestaurantDataException(NAME_INVALID_MESSAGE);
        }
    }

}
