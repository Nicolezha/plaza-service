package com.pragma.powerup.domain.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class DishModel {
    private Long id;
    private String name;
    private Integer price;
    private String description;
    private String imageUrl;
    private Long categoryId;
    private Long restaurantId;
    private Boolean active;
}
