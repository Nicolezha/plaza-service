package com.pragma.powerup.domain.usecase;

import com.pragma.powerup.domain.exception.CategoryNotFoundException;
import com.pragma.powerup.domain.exception.InvalidDishDataException;
import com.pragma.powerup.domain.exception.NotRestaurantOwnerException;
import com.pragma.powerup.domain.exception.RestaurantNotFoundException;
import com.pragma.powerup.domain.model.DishModel;
import com.pragma.powerup.domain.model.RestaurantModel;
import com.pragma.powerup.domain.spi.ICategoryPersistencePort;
import com.pragma.powerup.domain.spi.IDishPersistencePort;
import com.pragma.powerup.domain.spi.IRestaurantPersistencePort;
import com.pragma.powerup.domain.validation.DishValidator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static com.pragma.powerup.domain.util.DishConstants.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DishUseCaseTest {

    private static final Long OWNER_ID = 1L;
    private static final Long RESTAURANT_ID = 10L;
    private static final Long CATEGORY_ID = 3L;

    @Mock
    private IDishPersistencePort dishPersistencePort;

    @Mock
    private ICategoryPersistencePort categoryPersistencePort;

    @Mock
    private DishValidator dishValidator;

    @Mock
    private IRestaurantPersistencePort restaurantPersistencePort;

    @InjectMocks
    private DishUseCase dishUseCase;

    private DishModel validDish() {
        DishModel dish = new DishModel();
        dish.setName("Bandeja paisa");
        dish.setPrice(25000);
        dish.setDescription("Plato tipico");
        dish.setImageUrl("https://imagenes.com/bandeja.png");
        dish.setCategoryId(CATEGORY_ID);
        dish.setRestaurantId(RESTAURANT_ID);
        return dish;
    }

    private RestaurantModel restaurantOwnedBy(Long ownerId) {
        RestaurantModel restaurant = new RestaurantModel();
        restaurant.setId(RESTAURANT_ID);
        restaurant.setOwnerId(ownerId);
        return restaurant;
    }

    @Test
    void saveDishShouldActivateAndSaveWhenEverythingIsValid() {
        DishModel dish = validDish();

        when(restaurantPersistencePort.findById(RESTAURANT_ID))
                .thenReturn(Optional.of(restaurantOwnedBy(OWNER_ID)));
        when(categoryPersistencePort.existsById(CATEGORY_ID)).thenReturn(true);

        dishUseCase.saveDish(dish, OWNER_ID);

        assertTrue(dish.getActive());
        verify(dishPersistencePort).saveDish(dish);
    }

    @Test
    void saveDishShouldThrowWhenDishDataIsInvalid() {
        DishModel dish = validDish();
        doThrow(new InvalidDishDataException(PRICE_INVALID_MESSAGE))
                .when(dishValidator).validate(dish);

        InvalidDishDataException ex = assertThrows(InvalidDishDataException.class,
                () -> dishUseCase.saveDish(dish, OWNER_ID));

        assertEquals(PRICE_INVALID_MESSAGE, ex.getMessage());
        verifyNoInteractions(restaurantPersistencePort, categoryPersistencePort);
        verify(dishPersistencePort, never()).saveDish(any());
    }

    @Test
    void saveDishShouldThrowWhenRestaurantDoesNotExist() {
        DishModel dish = validDish();

        when(restaurantPersistencePort.findById(RESTAURANT_ID)).thenReturn(Optional.empty());

        RestaurantNotFoundException ex = assertThrows(RestaurantNotFoundException.class,
                () -> dishUseCase.saveDish(dish, OWNER_ID));

        assertEquals(RESTAURANT_NOT_FOUND_MESSAGE, ex.getMessage());
        verifyNoInteractions(categoryPersistencePort);
        verify(dishPersistencePort, never()).saveDish(any());
    }

    @Test
    void saveDishShouldThrowWhenRequesterIsNotTheRestaurantOwner() {
        DishModel dish = validDish();

        when(restaurantPersistencePort.findById(RESTAURANT_ID))
                .thenReturn(Optional.of(restaurantOwnedBy(OWNER_ID)));

        NotRestaurantOwnerException ex = assertThrows(NotRestaurantOwnerException.class,
                () -> dishUseCase.saveDish(dish, 99L));

        assertEquals(NOT_RESTAURANT_OWNER_MESSAGE, ex.getMessage());
        verifyNoInteractions(categoryPersistencePort);
        verify(dishPersistencePort, never()).saveDish(any());
    }

    @Test
    void saveDishShouldThrowWhenCategoryDoesNotExist() {
        DishModel dish = validDish();

        when(restaurantPersistencePort.findById(RESTAURANT_ID))
                .thenReturn(Optional.of(restaurantOwnedBy(OWNER_ID)));
        when(categoryPersistencePort.existsById(CATEGORY_ID)).thenReturn(false);

        CategoryNotFoundException ex = assertThrows(CategoryNotFoundException.class,
                () -> dishUseCase.saveDish(dish, OWNER_ID));

        assertEquals(CATEGORY_NOT_FOUND_MESSAGE, ex.getMessage());
        assertNull(dish.getActive());
        verify(dishPersistencePort, never()).saveDish(any());
    }
}
