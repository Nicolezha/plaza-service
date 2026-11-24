package com.pragma.powerup.domain.usecase;

import com.pragma.powerup.domain.exception.InvalidPaginationException;
import com.pragma.powerup.domain.exception.OwnerNotFoundException;
import com.pragma.powerup.domain.exception.RestaurantAlreadyExistsException;
import com.pragma.powerup.domain.exception.UserNotOwnerException;
import com.pragma.powerup.domain.model.PageModel;
import com.pragma.powerup.domain.model.RestaurantModel;
import com.pragma.powerup.domain.model.Role;
import com.pragma.powerup.domain.spi.IRestaurantPersistencePort;
import com.pragma.powerup.domain.spi.IUserClientPort;
import com.pragma.powerup.domain.validation.PaginationValidator;
import com.pragma.powerup.domain.validation.RestaurantValidator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static com.pragma.powerup.domain.util.PaginationConstants.PAGE_INVALID_MESSAGE;
import static com.pragma.powerup.domain.util.RestaurantConstants.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RestaurantUseCaseTest {

    @Mock
    private IRestaurantPersistencePort restaurantPersistencePort;

    @Mock
    private RestaurantValidator restaurantValidator;

    @Mock
    private IUserClientPort userClientPort;

    @Mock
    private PaginationValidator paginationValidator;

    @InjectMocks
    RestaurantUseCase restaurantUseCase;

    private RestaurantModel validRestaurant(){
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
    void saveRestaurantShouldSaveWhenNitIsNew() {
        RestaurantModel restaurant = validRestaurant();

        when(userClientPort.getUserRole(1L)).thenReturn(Role.OWNER);
        when(restaurantPersistencePort.existsByNit("900123456")).thenReturn(false);

        restaurantUseCase.saveRestaurant(restaurant);

        verify(restaurantPersistencePort).saveRestaurant(restaurant);
    }

    @Test
    void saveRestaurantShouldThrowWhenNitAlreadyExists(){
        RestaurantModel restaurant = validRestaurant();

        when(userClientPort.getUserRole(1L)).thenReturn(Role.OWNER);
        when(restaurantPersistencePort.existsByNit("900123456")).thenReturn(true);

        RestaurantAlreadyExistsException ex = assertThrows(RestaurantAlreadyExistsException.class,
                () -> restaurantUseCase.saveRestaurant(restaurant));

        assertEquals(NIT_ALREADY_EXISTS_MESSAGE, ex.getMessage());
        verify(restaurantPersistencePort, never()).saveRestaurant(any());
    }

    @Test
    void saveRestaurantShouldThrowWhenUserIsNotOwner(){
        RestaurantModel restaurant = validRestaurant();

        when(userClientPort.getUserRole(1L)).thenReturn(Role.ADMIN);

        UserNotOwnerException ex = assertThrows(UserNotOwnerException.class,
                () -> restaurantUseCase.saveRestaurant(restaurant));

        assertEquals(USER_NOT_OWNER_MESSAGE, ex.getMessage());
        verify(restaurantPersistencePort, never()).existsByNit(any());
        verify(restaurantPersistencePort, never()).saveRestaurant(any());
    }

    @Test
    void saveRestaurantShouldThrowWhenOwnerIsNotFound(){
        RestaurantModel restaurant = validRestaurant();

        when(userClientPort.getUserRole(1L))
                .thenThrow(new OwnerNotFoundException(OWNER_NOT_FOUND_MESSAGE));

        OwnerNotFoundException ex = assertThrows(OwnerNotFoundException.class,
                () -> restaurantUseCase.saveRestaurant(restaurant));

        assertEquals(OWNER_NOT_FOUND_MESSAGE, ex.getMessage());
        verify(restaurantPersistencePort, never()).existsByNit(any());
        verify(restaurantPersistencePort, never()).saveRestaurant(any());

    }

    @Test
    void getRestaurantsShouldValidateAndReturnThePageFromThePersistencePort() {
        PageModel<RestaurantModel> page =
                new PageModel<>(List.of(validRestaurant()), 0, 10, 1L, 1);

        when(restaurantPersistencePort.findAllOrderedByName(0, 10)).thenReturn(page);

        PageModel<RestaurantModel> result = restaurantUseCase.getRestaurants(0, 10);

        assertSame(page, result);
        verify(paginationValidator).validatePagination(0, 10);
        verify(restaurantPersistencePort).findAllOrderedByName(0, 10);
    }

    @Test
    void getRestaurantsShouldThrowAndNotQueryWhenPaginationIsInvalid() {
        doThrow(new InvalidPaginationException(PAGE_INVALID_MESSAGE))
                .when(paginationValidator).validatePagination(-1, 10);

        InvalidPaginationException ex = assertThrows(InvalidPaginationException.class,
                () -> restaurantUseCase.getRestaurants(-1, 10));

        assertEquals(PAGE_INVALID_MESSAGE, ex.getMessage());
        verify(restaurantPersistencePort, never()).findAllOrderedByName(anyInt(), anyInt());
    }
}