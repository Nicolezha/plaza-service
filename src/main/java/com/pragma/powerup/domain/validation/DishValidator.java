package com.pragma.powerup.domain.validation;

import com.pragma.powerup.domain.exception.InvalidDishDataException;
import com.pragma.powerup.domain.model.DishModel;

import static com.pragma.powerup.domain.util.DishConstants.PRICE_INVALID_MESSAGE;

public class DishValidator {

    public void validate(DishModel dishModel){
        if(dishModel.getPrice() == null || dishModel.getPrice() <= 0){
            throw new InvalidDishDataException(PRICE_INVALID_MESSAGE);
        }
    }
}
