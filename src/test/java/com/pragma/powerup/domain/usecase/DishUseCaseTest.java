package com.pragma.powerup.domain.usecase;

import com.pragma.powerup.domain.exception.CategoryNotFoundException;
import com.pragma.powerup.domain.exception.DishNotFoundException;
import com.pragma.powerup.domain.exception.InvalidDishDataException;
import com.pragma.powerup.domain.exception.InvalidPaginationException;
import com.pragma.powerup.domain.exception.NotRestaurantOwnerException;
import com.pragma.powerup.domain.exception.RestaurantNotFoundException;
import com.pragma.powerup.domain.model.DishModel;
import com.pragma.powerup.domain.model.PageModel;
import com.pragma.powerup.domain.model.RestaurantModel;
import com.pragma.powerup.domain.spi.ICategoryPersistencePort;
import com.pragma.powerup.domain.spi.IDishPersistencePort;
import com.pragma.powerup.domain.spi.IRestaurantPersistencePort;
import com.pragma.powerup.domain.validation.DishValidator;
import com.pragma.powerup.domain.validation.PaginationValidator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static com.pragma.powerup.domain.util.DishConstants.*;
import static com.pragma.powerup.domain.util.PaginationConstants.PAGE_INVALID_MESSAGE;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DishUseCaseTest {

    private static final Long OWNER_ID = 1L;
    private static final Long RESTAURANT_ID = 10L;
    private static final Long CATEGORY_ID = 3L;
    private static final Long DISH_ID = 5L;

    @Mock
    private IDishPersistencePort dishPersistencePort;

    @Mock
    private ICategoryPersistencePort categoryPersistencePort;

    @Mock
    private DishValidator dishValidator;

    @Mock
    private IRestaurantPersistencePort restaurantPersistencePort;

    @Mock
    private PaginationValidator paginationValidator;

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

    private DishModel storedDish() {
        DishModel dish = validDish();
        dish.setId(DISH_ID);
        dish.setActive(true);
        return dish;
    }

    @Test
    void updateDishShouldChangeOnlyPriceAndDescriptionAndSave() {
        DishModel dish = storedDish();

        when(dishPersistencePort.findById(DISH_ID)).thenReturn(Optional.of(dish));
        when(restaurantPersistencePort.findById(RESTAURANT_ID))
                .thenReturn(Optional.of(restaurantOwnedBy(OWNER_ID)));

        dishUseCase.updateDish(DISH_ID, 30000, "Nueva descripcion", OWNER_ID);

        assertEquals(30000, dish.getPrice());
        assertEquals("Nueva descripcion", dish.getDescription());
        assertEquals("Bandeja paisa", dish.getName());
        assertEquals("https://imagenes.com/bandeja.png", dish.getImageUrl());
        assertEquals(CATEGORY_ID, dish.getCategoryId());
        assertEquals(RESTAURANT_ID, dish.getRestaurantId());
        assertTrue(dish.getActive());
        verify(dishPersistencePort).saveDish(dish);
    }

    @Test
    void updateDishShouldValidateTheNewPriceNotTheOldOne() {
        DishModel dish = storedDish();

        when(dishPersistencePort.findById(DISH_ID)).thenReturn(Optional.of(dish));
        when(restaurantPersistencePort.findById(RESTAURANT_ID))
                .thenReturn(Optional.of(restaurantOwnedBy(OWNER_ID)));
        doAnswer(invocation -> {
            DishModel validated = invocation.getArgument(0);
            assertEquals(-5, validated.getPrice());
            throw new InvalidDishDataException(PRICE_INVALID_MESSAGE);
        }).when(dishValidator).validate(dish);

        InvalidDishDataException ex = assertThrows(InvalidDishDataException.class,
                () -> dishUseCase.updateDish(DISH_ID, -5, "Nueva descripcion", OWNER_ID));

        assertEquals(PRICE_INVALID_MESSAGE, ex.getMessage());
        verify(dishPersistencePort, never()).saveDish(any());
    }

    @Test
    void updateDishShouldThrowWhenDishDoesNotExist() {
        when(dishPersistencePort.findById(DISH_ID)).thenReturn(Optional.empty());

        DishNotFoundException ex = assertThrows(DishNotFoundException.class,
                () -> dishUseCase.updateDish(DISH_ID, 30000, "Nueva descripcion", OWNER_ID));

        assertEquals(DISH_NOT_FOUND_MESSAGE, ex.getMessage());
        verifyNoInteractions(restaurantPersistencePort);
        verify(dishPersistencePort, never()).saveDish(any());
    }

    @Test
    void updateDishShouldThrowWhenRestaurantDoesNotExist() {
        when(dishPersistencePort.findById(DISH_ID)).thenReturn(Optional.of(storedDish()));
        when(restaurantPersistencePort.findById(RESTAURANT_ID)).thenReturn(Optional.empty());

        RestaurantNotFoundException ex = assertThrows(RestaurantNotFoundException.class,
                () -> dishUseCase.updateDish(DISH_ID, 30000, "Nueva descripcion", OWNER_ID));

        assertEquals(RESTAURANT_NOT_FOUND_MESSAGE, ex.getMessage());
        verify(dishPersistencePort, never()).saveDish(any());
    }

    @Test
    void updateDishShouldThrowWhenRequesterIsNotTheRestaurantOwner() {
        DishModel dish = storedDish();

        when(dishPersistencePort.findById(DISH_ID)).thenReturn(Optional.of(dish));
        when(restaurantPersistencePort.findById(RESTAURANT_ID))
                .thenReturn(Optional.of(restaurantOwnedBy(OWNER_ID)));

        NotRestaurantOwnerException ex = assertThrows(NotRestaurantOwnerException.class,
                () -> dishUseCase.updateDish(DISH_ID, 30000, "Nueva descripcion", 99L));

        assertEquals(NOT_RESTAURANT_OWNER_MESSAGE, ex.getMessage());
        assertEquals(25000, dish.getPrice());
        assertEquals("Plato tipico", dish.getDescription());
        verifyNoInteractions(dishValidator);
        verify(dishPersistencePort, never()).saveDish(any());
    }

    @Test
    void updateDishStatusShouldDisableTheDishAndSave() {
        DishModel dish = storedDish();

        when(dishPersistencePort.findById(DISH_ID)).thenReturn(Optional.of(dish));
        when(restaurantPersistencePort.findById(RESTAURANT_ID))
                .thenReturn(Optional.of(restaurantOwnedBy(OWNER_ID)));

        dishUseCase.updateDishStatus(DISH_ID, false, OWNER_ID);

        assertFalse(dish.getActive());
        assertEquals(25000, dish.getPrice());
        verify(dishPersistencePort).saveDish(dish);
        verifyNoInteractions(dishValidator);
    }

    @Test
    void updateDishStatusShouldEnableTheDishAndSave() {
        DishModel dish = storedDish();
        dish.setActive(false);

        when(dishPersistencePort.findById(DISH_ID)).thenReturn(Optional.of(dish));
        when(restaurantPersistencePort.findById(RESTAURANT_ID))
                .thenReturn(Optional.of(restaurantOwnedBy(OWNER_ID)));

        dishUseCase.updateDishStatus(DISH_ID, true, OWNER_ID);

        assertTrue(dish.getActive());
        verify(dishPersistencePort).saveDish(dish);
    }

    @Test
    void updateDishStatusShouldThrowWhenDishDoesNotExist() {
        when(dishPersistencePort.findById(DISH_ID)).thenReturn(Optional.empty());

        DishNotFoundException ex = assertThrows(DishNotFoundException.class,
                () -> dishUseCase.updateDishStatus(DISH_ID, false, OWNER_ID));

        assertEquals(DISH_NOT_FOUND_MESSAGE, ex.getMessage());
        verifyNoInteractions(restaurantPersistencePort);
        verify(dishPersistencePort, never()).saveDish(any());
    }

    @Test
    void updateDishStatusShouldThrowWhenRestaurantDoesNotExist() {
        when(dishPersistencePort.findById(DISH_ID)).thenReturn(Optional.of(storedDish()));
        when(restaurantPersistencePort.findById(RESTAURANT_ID)).thenReturn(Optional.empty());

        RestaurantNotFoundException ex = assertThrows(RestaurantNotFoundException.class,
                () -> dishUseCase.updateDishStatus(DISH_ID, false, OWNER_ID));

        assertEquals(RESTAURANT_NOT_FOUND_MESSAGE, ex.getMessage());
        verify(dishPersistencePort, never()).saveDish(any());
    }

    @Test
    void updateDishStatusShouldThrowWhenRequesterIsNotTheRestaurantOwner() {
        DishModel dish = storedDish();

        when(dishPersistencePort.findById(DISH_ID)).thenReturn(Optional.of(dish));
        when(restaurantPersistencePort.findById(RESTAURANT_ID))
                .thenReturn(Optional.of(restaurantOwnedBy(OWNER_ID)));

        NotRestaurantOwnerException ex = assertThrows(NotRestaurantOwnerException.class,
                () -> dishUseCase.updateDishStatus(DISH_ID, false, 99L));

        assertEquals(NOT_RESTAURANT_OWNER_MESSAGE, ex.getMessage());
        assertTrue(dish.getActive());
        verify(dishPersistencePort, never()).saveDish(any());
    }

    @Test
    void getDishesByRestaurantShouldReturnThePageWithoutCategoryFilter() {
        PageModel<DishModel> page = new PageModel<>(List.of(validDish()), 0, 10, 1L, 1);

        when(restaurantPersistencePort.findById(RESTAURANT_ID))
                .thenReturn(Optional.of(restaurantOwnedBy(OWNER_ID)));
        when(dishPersistencePort.findByRestaurant(RESTAURANT_ID, null, 0, 10)).thenReturn(page);

        PageModel<DishModel> result = dishUseCase.getDishesByRestaurant(RESTAURANT_ID, null, 0, 10);

        assertSame(page, result);
        verify(paginationValidator).validatePagination(0, 10);
        verifyNoInteractions(categoryPersistencePort);
    }

    @Test
    void getDishesByRestaurantShouldPassTheCategoryFilterToThePersistencePort() {
        PageModel<DishModel> page = new PageModel<>(List.of(validDish()), 0, 10, 1L, 1);

        when(restaurantPersistencePort.findById(RESTAURANT_ID))
                .thenReturn(Optional.of(restaurantOwnedBy(OWNER_ID)));
        when(dishPersistencePort.findByRestaurant(RESTAURANT_ID, CATEGORY_ID, 0, 10)).thenReturn(page);

        PageModel<DishModel> result = dishUseCase.getDishesByRestaurant(RESTAURANT_ID, CATEGORY_ID, 0, 10);

        assertSame(page, result);
        verify(dishPersistencePort).findByRestaurant(RESTAURANT_ID, CATEGORY_ID, 0, 10);
    }

    @Test
    void getDishesByRestaurantShouldThrowWhenRestaurantDoesNotExist() {
        when(restaurantPersistencePort.findById(RESTAURANT_ID)).thenReturn(Optional.empty());

        RestaurantNotFoundException ex = assertThrows(RestaurantNotFoundException.class,
                () -> dishUseCase.getDishesByRestaurant(RESTAURANT_ID, null, 0, 10));

        assertEquals(RESTAURANT_NOT_FOUND_MESSAGE, ex.getMessage());
        verify(dishPersistencePort, never()).findByRestaurant(any(), any(), anyInt(), anyInt());
    }

    @Test
    void getDishesByRestaurantShouldThrowAndNotQueryWhenPaginationIsInvalid() {
        doThrow(new InvalidPaginationException(PAGE_INVALID_MESSAGE))
                .when(paginationValidator).validatePagination(-1, 10);

        InvalidPaginationException ex = assertThrows(InvalidPaginationException.class,
                () -> dishUseCase.getDishesByRestaurant(RESTAURANT_ID, null, -1, 10));

        assertEquals(PAGE_INVALID_MESSAGE, ex.getMessage());
        verifyNoInteractions(restaurantPersistencePort);
        verify(dishPersistencePort, never()).findByRestaurant(any(), any(), anyInt(), anyInt());
    }
}
