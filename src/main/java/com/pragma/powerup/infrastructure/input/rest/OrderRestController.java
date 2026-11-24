package com.pragma.powerup.infrastructure.input.rest;

import com.pragma.powerup.application.dto.request.OrderRequestDto;
import com.pragma.powerup.application.handler.IOrderHandler;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/order")
@RequiredArgsConstructor
public class OrderRestController {

    private final IOrderHandler orderHandler;

    @Operation(summary = "Place a new order")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Order created with status PENDING", content = @Content),
            @ApiResponse(responseCode = "400", description = "Invalid data or dishes not from the restaurant", content = @Content),
            @ApiResponse(responseCode = "403", description = "Only clients can place orders", content = @Content),
            @ApiResponse(responseCode = "404", description = "Restaurant or dish not found", content = @Content),
            @ApiResponse(responseCode = "409", description = "The client already has an order in progress", content = @Content)
    })
    @PostMapping
    public ResponseEntity<Void> createOrder(@Valid @RequestBody OrderRequestDto orderRequestDto,
                                            Authentication authentication) {
        Long clientId = (Long) authentication.getDetails();
        orderHandler.createOrder(orderRequestDto, clientId);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }
}
