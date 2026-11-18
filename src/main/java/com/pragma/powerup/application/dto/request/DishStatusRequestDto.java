package com.pragma.powerup.application.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DishStatusRequestDto {

    @NotNull
    private Boolean active;
}
