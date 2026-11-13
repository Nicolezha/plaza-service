package com.pragma.powerup.infrastructure.out.jpa.feign;

import com.pragma.powerup.domain.exception.OwnerNotFoundException;
import com.pragma.powerup.domain.exception.UserServiceUnavailableException;
import com.pragma.powerup.domain.model.Role;
import com.pragma.powerup.domain.spi.IUserClientPort;
import feign.FeignException;
import lombok.RequiredArgsConstructor;

import static com.pragma.powerup.domain.util.RestaurantConstants.OWNER_NOT_FOUND_MESSAGE;
import static com.pragma.powerup.domain.util.RestaurantConstants.USER_SERVICE_UNAVAILABLE_MESSAGE;

@RequiredArgsConstructor
public class UserFeignAdapter implements IUserClientPort {

    private final IUserFeignClient userFeignClient;

    @Override
    public Role getUserRole(Long userId) {
        try {
            return userFeignClient.getUserRole(userId).getRole();
        } catch (FeignException.NotFound e) {
            throw new OwnerNotFoundException(OWNER_NOT_FOUND_MESSAGE);
        } catch (FeignException e) {
            throw new UserServiceUnavailableException(USER_SERVICE_UNAVAILABLE_MESSAGE);
        }
    }
}
