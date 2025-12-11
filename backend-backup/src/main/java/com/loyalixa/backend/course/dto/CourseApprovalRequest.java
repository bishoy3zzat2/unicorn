package com.loyalixa.backend.course.dto;
public record CourseApprovalRequest(
    String approvalStatus,  
    String comments  
) {}
