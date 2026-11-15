package com.pragma.powerup.application.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DishRequestDto {

    @NotBlank
    private String name;

    @NotNull
    @Positive
    private Integer price;

    @NotBlank
    private String description;

    @NotBlank
    private String imageUrl;

    @NotNull
    private Long categoryId;

    @NotNull
    private Long restaurantId;
}
