package com.loyalixa.backend.staff.dto;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;
public record EmploymentInfoUpdateRequest(
    String employmentType,  
    Boolean hasFixedSalary,  
    BigDecimal monthlySalary,  
    String salaryCurrency,  
    Integer salaryPaymentDay,  
    LocalDate employmentStartDate,
    LocalDate employmentEndDate,
    @Size(max = 1000) String workSchedule,  
    Integer hoursPerWeek,  
    @Size(max = 5000) String workInstructions,  
    String paymentMethod  
) {}
