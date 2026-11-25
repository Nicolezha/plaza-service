package com.pragma.powerup.infrastructure.input.rest;

import com.pragma.powerup.application.dto.request.EmployeeAssignmentRequestDto;
import com.pragma.powerup.application.dto.request.PageRequestDto;
import com.pragma.powerup.application.dto.request.RestaurantRequestDto;
import com.pragma.powerup.application.dto.response.PageResponseDto;
import com.pragma.powerup.application.dto.response.RestaurantListResponseDto;
import com.pragma.powerup.application.handler.IRestaurantHandler;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/restaurant")
@RequiredArgsConstructor
public class RestaurantRestController {

    private final IRestaurantHandler restaurantHandler;

    @Operation(summary = "Add a new restaurant")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Restaurant created", content = @Content),
            @ApiResponse(responseCode = "400", description = "Invalid data", content = @Content),
            @ApiResponse(responseCode = "409", description = "NIT already registered", content = @Content)
    })
    @PostMapping
    public ResponseEntity<Void> saveRestaurant(@Valid @RequestBody RestaurantRequestDto restaurantRequestDto) {
        restaurantHandler.saveRestaurant(restaurantRequestDto);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @Operation(summary = "List restaurants alphabetically, paginated")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Restaurants page returned"),
            @ApiResponse(responseCode = "400", description = "Invalid page or size", content = @Content),
            @ApiResponse(responseCode = "403", description = "Only clients can list restaurants", content = @Content)
    })
    @GetMapping
    public ResponseEntity<PageResponseDto<RestaurantListResponseDto>> getRestaurants(
            @ParameterObject PageRequestDto pageRequest) {
        return ResponseEntity.ok(restaurantHandler.getRestaurants(pageRequest));
    }

    @Operation(summary = "Assign an employee to a restaurant",
            description = "Called by the user service when an owner creates an employee, forwarding the owner's token.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Employee assigned to the restaurant", content = @Content),
            @ApiResponse(responseCode = "400", description = "Invalid data", content = @Content),
            @ApiResponse(responseCode = "403", description = "Requester is not the owner of the restaurant", content = @Content),
            @ApiResponse(responseCode = "404", description = "Restaurant not found", content = @Content),
            @ApiResponse(responseCode = "409", description = "Employee already assigned to a restaurant", content = @Content)
    })
    @PostMapping("/{restaurantId}/employee")
    public ResponseEntity<Void> assignEmployee(@PathVariable Long restaurantId,
                                               @Valid @RequestBody EmployeeAssignmentRequestDto employeeAssignmentRequestDto,
                                               Authentication authentication) {
        Long requesterId = (Long) authentication.getDetails();
        restaurantHandler.assignEmployee(restaurantId, employeeAssignmentRequestDto, requesterId);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }
}
