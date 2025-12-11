package com.loyalixa.backend.staff.dto;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.springframework.format.annotation.DateTimeFormat;
import java.math.BigDecimal;
import java.time.LocalDate;
public record TaskCreateRequest(
    @NotNull(message = "User ID is required")
    java.util.UUID userId,
    @NotBlank(message = "Task title is required")
    String taskTitle,
    String taskDescription,
    BigDecimal amount,
    @NotNull(message = "Task date is required")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    LocalDate taskDate,
    String notes,
    String externalTaskId
) {}
