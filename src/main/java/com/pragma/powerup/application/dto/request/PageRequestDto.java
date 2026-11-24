package com.pragma.powerup.application.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PageRequestDto {
    private int page = 0;
    private int size = 10;
}
