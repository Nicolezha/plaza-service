package com.pragma.powerup.domain.spi;

import com.pragma.powerup.domain.model.Role;

public interface IUserClientPort {
    Role getUserRole(Long userId);
}
