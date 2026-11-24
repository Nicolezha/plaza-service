package com.pragma.powerup.domain.model;

import java.util.List;

public enum OrderStatus {
    PENDING, IN_PREPARATION, READY, DELIVERED, CANCELLED;

    public static List<OrderStatus> inProgress() {
        return List.of(PENDING, IN_PREPARATION, READY);
    }
}
