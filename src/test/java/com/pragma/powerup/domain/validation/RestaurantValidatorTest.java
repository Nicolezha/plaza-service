package com.pragma.powerup.domain.validation;

import com.pragma.powerup.domain.exception.InvalidRestaurantDataException;
import com.pragma.powerup.domain.model.RestaurantModel;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static com.pragma.powerup.domain.util.RestaurantConstants.*;
import static org.junit.jupiter.api.Assertions.*;

class RestaurantValidatorTest {

    private final RestaurantValidator validator = new RestaurantValidator();

    private RestaurantModel validRestaurant() {
        RestaurantModel restaurant = new RestaurantModel();
        restaurant.setName("El Corral 24");
        restaurant.setNit("900123456");
        restaurant.setAddress("Calle 10 # 5-20");
        restaurant.setPhone("+573005698325");
        restaurant.setLogoUrl("https://logos.com/corral.png");
        restaurant.setOwnerId(1L);
        return restaurant;
    }

    @Test
    void shouldAcceptValidRestaurant() {
        assertDoesNotThrow(() -> validator.validate(validRestaurant()));
    }

    @ParameterizedTest
    @ValueSource(strings = {"900123456", "1", "0012345"})
    void shouldAcceptNumericNit(String nit) {
        RestaurantModel restaurant = validRestaurant();
        restaurant.setNit(nit);

        assertDoesNotThrow(() -> validator.validate(restaurant));
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"900-123", "90012345A", "900 123", "+900123", "9.001", "ABC"})
    void shouldRejectNonNumericNit(String nit) {
        RestaurantModel restaurant = validRestaurant();
        restaurant.setNit(nit);

        InvalidRestaurantDataException ex = assertThrows(InvalidRestaurantDataException.class,
                () -> validator.validate(restaurant));

        assertEquals(NIT_INVALID_MESSAGE, ex.getMessage());
    }

    @ParameterizedTest
    @ValueSource(strings = {"+573005698325", "3005698325", "1234567890123"})
    void shouldAcceptValidPhone(String phone) {
        RestaurantModel restaurant = validRestaurant();
        restaurant.setPhone(phone);

        assertDoesNotThrow(() -> validator.validate(restaurant));
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {
            "+5730056983250",
            "12345678901234",
            "abc",
            "300 569 8325",
            "300-569-8325",
            "++573005698325",
            "573+005698325",
            "+"
    })
    void shouldRejectInvalidPhone(String phone) {
        RestaurantModel restaurant = validRestaurant();
        restaurant.setPhone(phone);

        InvalidRestaurantDataException ex = assertThrows(InvalidRestaurantDataException.class,
                () -> validator.validate(restaurant));

        assertEquals(PHONE_INVALID_MESSAGE, ex.getMessage());
    }

    @ParameterizedTest
    @ValueSource(strings = {"El Corral 24", "24 Horas", "Burger 3", "A", "Café 1900",
            "McDonald's", "A&W", "Burger-3", "Ñandú 2"})
    void shouldAcceptNameThatContainsAtLeastOneLetter(String name) {
        RestaurantModel restaurant = validRestaurant();
        restaurant.setName(name);

        assertDoesNotThrow(() -> validator.validate(restaurant));
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"12345", "7", "123 456", "   ", "###", "!!!", "@#$%", "123-456", "1 - 2"})
    void shouldRejectNameWithoutLetters(String name) {
        RestaurantModel restaurant = validRestaurant();
        restaurant.setName(name);
        InvalidRestaurantDataException ex = assertThrows(InvalidRestaurantDataException.class,
                () -> validator.validate(restaurant));

        assertEquals(NAME_INVALID_MESSAGE, ex.getMessage());
    }
}