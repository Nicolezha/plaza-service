package com.pragma.powerup.infrastructure.input.rest;

import com.pragma.powerup.application.dto.request.DishRequestDto;
import com.pragma.powerup.application.dto.request.DishStatusRequestDto;
import com.pragma.powerup.application.dto.request.DishUpdateRequestDto;
import com.pragma.powerup.application.handler.IDishHandler;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/dish")
@RequiredArgsConstructor
public class DishRestController {

    private final IDishHandler dishHandler;

    @Operation(summary = "Add a new dish")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Dish created", content = @Content),
            @ApiResponse(responseCode = "400", description = "Invalid data", content = @Content),
            @ApiResponse(responseCode = "403", description = "Requester is not the owner of the restaurant", content = @Content),
            @ApiResponse(responseCode = "404", description = "Restaurant or category not found", content = @Content)
    })
    @PostMapping
    public ResponseEntity<Void> saveDish(@Valid @RequestBody DishRequestDto dishRequestDto,
                                         Authentication authentication){
        Long requesterId = (Long) authentication.getDetails();
        dishHandler.saveDish(dishRequestDto, requesterId);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @Operation(summary = "Update a dish")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Dish updated", content = @Content),
            @ApiResponse(responseCode = "400", description = "Invalid data", content = @Content),
            @ApiResponse(responseCode = "403", description = "Requester is not the owner of the restaurant", content = @Content),
            @ApiResponse(responseCode = "404", description = "Dish not found", content = @Content)
    })
    @PatchMapping("/{id}")
    public ResponseEntity<Void> updateDish(@PathVariable Long id,
                                           @Valid @RequestBody DishUpdateRequestDto dishUpdateRequestDto,
                                           Authentication authentication){

        Long requesterId = (Long) authentication.getDetails();
        dishHandler.updateDish(id,
                dishUpdateRequestDto,
                requesterId);

        return new ResponseEntity<>(HttpStatus.OK);
    }

    @Operation(summary = "Enable or disable a dish")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Dish status updated", content = @Content),
            @ApiResponse(responseCode = "400", description = "Invalid data", content = @Content),
            @ApiResponse(responseCode = "403", description = "Requester is not the owner of the restaurant", content = @Content),
            @ApiResponse(responseCode = "404", description = "Dish not found", content = @Content)
    })
    @PatchMapping("/{id}/status")
    public ResponseEntity<Void> updateDishStatus(@PathVariable Long id,
                                                 @Valid @RequestBody DishStatusRequestDto dishStatusRequestDto,
                                                 Authentication authentication){

        Long requesterId = (Long) authentication.getDetails();
        dishHandler.updateDishStatus(id,
                dishStatusRequestDto,
                requesterId);

        return new ResponseEntity<>(HttpStatus.OK);
    }
}
