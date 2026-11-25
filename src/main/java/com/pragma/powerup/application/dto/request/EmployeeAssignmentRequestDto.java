package com.pragma.powerup.application.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EmployeeAssignmentRequestDto {

    @NotNull
    private Long employeeId;
}
