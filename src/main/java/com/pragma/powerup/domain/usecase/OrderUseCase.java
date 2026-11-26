package com.pragma.powerup.domain.usecase;

import com.pragma.powerup.domain.api.IOrderServicePort;
import com.pragma.powerup.domain.exception.DishNotFoundException;
import com.pragma.powerup.domain.exception.EmployeeNotAssignedException;
import com.pragma.powerup.domain.exception.InvalidOrderDataException;
import com.pragma.powerup.domain.exception.InvalidOrderStatusException;
import com.pragma.powerup.domain.exception.OrderInProgressException;
import com.pragma.powerup.domain.exception.OrderNotFoundException;
import com.pragma.powerup.domain.exception.OrderNotFromRestaurantException;
import com.pragma.powerup.domain.exception.RestaurantNotFoundException;
import com.pragma.powerup.domain.model.DishModel;
import com.pragma.powerup.domain.model.OrderDishModel;
import com.pragma.powerup.domain.model.OrderModel;
import com.pragma.powerup.domain.model.OrderStatus;
import com.pragma.powerup.domain.model.PageModel;
import com.pragma.powerup.domain.spi.IDishPersistencePort;
import com.pragma.powerup.domain.spi.IEmployeeRestaurantPersistencePort;
import com.pragma.powerup.domain.spi.IOrderPersistencePort;
import com.pragma.powerup.domain.spi.IRestaurantPersistencePort;
import com.pragma.powerup.domain.validation.OrderValidator;
import com.pragma.powerup.domain.validation.PaginationValidator;

import java.time.LocalDateTime;

import static com.pragma.powerup.domain.util.DishConstants.DISH_NOT_FOUND_MESSAGE;
import static com.pragma.powerup.domain.util.DishConstants.RESTAURANT_NOT_FOUND_MESSAGE;
import static com.pragma.powerup.domain.util.EmployeeConstants.EMPLOYEE_NOT_ASSIGNED_MESSAGE;
import static com.pragma.powerup.domain.util.OrderConstants.*;

public class OrderUseCase implements IOrderServicePort {

    private final IOrderPersistencePort orderPersistencePort;
    private final IRestaurantPersistencePort restaurantPersistencePort;
    private final IDishPersistencePort dishPersistencePort;
    private final IEmployeeRestaurantPersistencePort employeeRestaurantPersistencePort;
    private final OrderValidator orderValidator;
    private final PaginationValidator paginationValidator;

    public OrderUseCase(IOrderPersistencePort orderPersistencePort,
                        IRestaurantPersistencePort restaurantPersistencePort,
                        IDishPersistencePort dishPersistencePort,
                        IEmployeeRestaurantPersistencePort employeeRestaurantPersistencePort,
                        OrderValidator orderValidator,
                        PaginationValidator paginationValidator) {
        this.orderPersistencePort = orderPersistencePort;
        this.restaurantPersistencePort = restaurantPersistencePort;
        this.dishPersistencePort = dishPersistencePort;
        this.employeeRestaurantPersistencePort = employeeRestaurantPersistencePort;
        this.orderValidator = orderValidator;
        this.paginationValidator = paginationValidator;
    }

    @Override
    public void createOrder(OrderModel order, Long clientId) {
        orderValidator.validate(order);

        if (orderPersistencePort.existsByClientIdAndStatusIn(clientId, OrderStatus.inProgress())) {
            throw new OrderInProgressException(ORDER_IN_PROGRESS_MESSAGE);
        }

        if (restaurantPersistencePort.findById(order.getRestaurantId()).isEmpty()) {
            throw new RestaurantNotFoundException(RESTAURANT_NOT_FOUND_MESSAGE);
        }

        for (OrderDishModel orderDish : order.getDishes()) {
            validateDishBelongsToRestaurant(orderDish.getDishId(), order.getRestaurantId());
        }

        order.setClientId(clientId);
        order.setStatus(OrderStatus.PENDING);
        order.setDate(LocalDateTime.now());
        order.setEmployeeId(null);

        orderPersistencePort.saveOrder(order);
    }

    @Override
    public PageModel<OrderModel> getOrdersByStatus(OrderStatus status, Long employeeId, int page, int size) {
        paginationValidator.validatePagination(page, size);

        if (status == null) {
            throw new InvalidOrderDataException(STATUS_REQUIRED_MESSAGE);
        }

        Long restaurantId = findEmployeeRestaurantId(employeeId);

        return orderPersistencePort.findByRestaurantIdAndStatus(restaurantId, status, page, size);
    }

    @Override
    public OrderModel assignOrder(Long orderId, Long employeeId) {
        Long restaurantId = findEmployeeRestaurantId(employeeId);

        OrderModel order = orderPersistencePort.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException(ORDER_NOT_FOUND_MESSAGE));

        if (!restaurantId.equals(order.getRestaurantId())) {
            throw new OrderNotFromRestaurantException(ORDER_NOT_FROM_EMPLOYEE_RESTAURANT_MESSAGE);
        }

        if (order.getStatus() != OrderStatus.PENDING) {
            throw new InvalidOrderStatusException(ORDER_NOT_PENDING_MESSAGE);
        }

        order.setEmployeeId(employeeId);
        order.setStatus(OrderStatus.IN_PREPARATION);

        return orderPersistencePort.updateOrder(order);
    }

    private Long findEmployeeRestaurantId(Long employeeId) {
        return employeeRestaurantPersistencePort.findRestaurantIdByEmployeeId(employeeId)
                .orElseThrow(() -> new EmployeeNotAssignedException(EMPLOYEE_NOT_ASSIGNED_MESSAGE));
    }

    private void validateDishBelongsToRestaurant(Long dishId, Long restaurantId) {
        DishModel dish = dishPersistencePort.findById(dishId)
                .orElseThrow(() -> new DishNotFoundException(DISH_NOT_FOUND_MESSAGE));

        if (!restaurantId.equals(dish.getRestaurantId())) {
            throw new InvalidOrderDataException(DISH_NOT_FROM_RESTAURANT_MESSAGE);
        }

        if (!Boolean.TRUE.equals(dish.getActive())) {
            throw new InvalidOrderDataException(DISH_NOT_AVAILABLE_MESSAGE);
        }
    }
}
