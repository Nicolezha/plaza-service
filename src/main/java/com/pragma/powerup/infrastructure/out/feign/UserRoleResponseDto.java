package com.pragma.powerup.infrastructure.out.jpa.feign;

import com.pragma.powerup.domain.model.Role;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserRoleResponseDto {

    Long id;
    Role role;
}
