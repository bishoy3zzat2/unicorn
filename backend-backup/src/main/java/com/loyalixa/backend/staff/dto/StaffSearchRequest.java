package com.loyalixa.backend.staff.dto;
import java.util.UUID;
public record StaffSearchRequest(
    String search,  
    String role,  
    String status,  
    String taskStatus,  
    Boolean hasPendingTasks,  
    Boolean hasRejectedTasks,  
    Boolean hasCompletedTasks,  
    String paymentRequestStatus,  
    Boolean hasPaymentRequests,  
    Boolean hasDiscounts,  
    Boolean hasBonuses,  
    Boolean hasPenalties,  
    Boolean hasDeposits,  
    UUID roleId,  
    int page,
    int size
) {
    public StaffSearchRequest {
        if (page < 0) page = 0;
        if (size <= 0) size = 30;
        if (size > 100) size = 100;
    }
}
