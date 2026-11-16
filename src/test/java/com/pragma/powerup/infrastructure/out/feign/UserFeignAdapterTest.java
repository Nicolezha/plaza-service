package com.pragma.powerup.infrastructure.out.feign;

import com.pragma.powerup.domain.exception.OwnerNotFoundException;
import com.pragma.powerup.domain.exception.UserServiceUnavailableException;
import com.pragma.powerup.domain.model.Role;
import feign.FeignException;
import feign.RetryableException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static com.pragma.powerup.domain.util.RestaurantConstants.OWNER_NOT_FOUND_MESSAGE;
import static com.pragma.powerup.domain.util.RestaurantConstants.USER_SERVICE_UNAVAILABLE_MESSAGE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserFeignAdapterTest {

    @Mock
    private IUserFeignClient userFeignClient;

    @InjectMocks
    private UserFeignAdapter userFeignAdapter;

    @Test
    void getUserRoleShouldReturnTheRoleFromTheClient() {
        UserRoleResponseDto user = new UserRoleResponseDto();
        user.setRole(Role.OWNER);

        when(userFeignClient.getUserRole(1L)).thenReturn(user);
        assertEquals(Role.OWNER, userFeignAdapter.getUserRole(1L));
    }

    @Test
    void getUserRoleShouldThrowOwnerNotFoundWhenClientReturns404() {

        when(userFeignClient.getUserRole(1L)).thenThrow(mock(FeignException.NotFound.class));

        OwnerNotFoundException ex = assertThrows(OwnerNotFoundException.class,
                () -> userFeignAdapter.getUserRole(1L));

        assertEquals(OWNER_NOT_FOUND_MESSAGE, ex.getMessage());
    }

    @Test
    void getUserRoleShouldThrowServiceUnavailableWhenClientFails() {

        when(userFeignClient.getUserRole(1L)).thenThrow(mock(FeignException.class));

        UserServiceUnavailableException ex = assertThrows(UserServiceUnavailableException.class,
                () -> userFeignAdapter.getUserRole(1L));

        assertEquals(USER_SERVICE_UNAVAILABLE_MESSAGE, ex.getMessage());
    }

    @Test
    void getUserRoleShouldThrowServiceUnavailableWhenConnectionFails() {

        when(userFeignClient.getUserRole(1L)).thenThrow(mock(RetryableException.class));

        UserServiceUnavailableException ex = assertThrows(UserServiceUnavailableException.class,
                () -> userFeignAdapter.getUserRole(1L));

        assertEquals(USER_SERVICE_UNAVAILABLE_MESSAGE, ex.getMessage());
    }
}