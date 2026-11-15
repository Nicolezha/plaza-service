package com.pragma.powerup.domain.validation;

import com.pragma.powerup.domain.exception.InvalidDishDataException;
import com.pragma.powerup.domain.model.DishModel;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static com.pragma.powerup.domain.util.DishConstants.PRICE_INVALID_MESSAGE;
import static org.junit.jupiter.api.Assertions.*;

class DishValidatorTest {

    private final DishValidator validator = new DishValidator();

    private DishModel dishWithPrice(Integer price) {
        DishModel dish = new DishModel();
        dish.setPrice(price);
        return dish;
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 100, 25000})
    void shouldAcceptPositivePrice(int price) {
        assertDoesNotThrow(() -> validator.validate(dishWithPrice(price)));
    }

    @ParameterizedTest
    @ValueSource(ints = {0, -1, -25000})
    void shouldRejectZeroOrNegativePrice(int price) {
        InvalidDishDataException ex = assertThrows(InvalidDishDataException.class,
                () -> validator.validate(dishWithPrice(price)));

        assertEquals(PRICE_INVALID_MESSAGE, ex.getMessage());
    }

    @Test
    void shouldRejectNullPrice() {
        InvalidDishDataException ex = assertThrows(InvalidDishDataException.class,
                () -> validator.validate(dishWithPrice(null)));

        assertEquals(PRICE_INVALID_MESSAGE, ex.getMessage());
    }
}
