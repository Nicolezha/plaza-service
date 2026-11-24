package com.pragma.powerup.application.dto.response;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DishListResponseDto {

    private Long id;
    private String name;
    private Integer price;
    private String description;
    private String imageUrl;
    private Long categoryId;
}
