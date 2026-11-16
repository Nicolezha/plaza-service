package com.pragma.powerup.application.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DishUpdateRequestDto {

    @NotNull
    @Positive
    private Integer price;

    @NotBlank
    private String description;
}
