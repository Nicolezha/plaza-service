package com.pragma.powerup.domain.util;

public class EmployeeConstants {

    private EmployeeConstants() {
    }

    public static final String NOT_RESTAURANT_OWNER_EMPLOYEE_MESSAGE =
            "Only the owner of the restaurant can assign employees to it";

    public static final String EMPLOYEE_ALREADY_ASSIGNED_MESSAGE =
            "The employee is already assigned to a restaurant";

    public static final String EMPLOYEE_NOT_ASSIGNED_MESSAGE =
            "The employee is not assigned to any restaurant";
}
