package com.loyalixa.backend.user.dto;
import java.util.List;
public record PageResponse<T>(
    List<T> content,       
    int page,              
    int size,              
    long totalElements,    
    int totalPages         
) {
    public PageResponse {
        if (totalPages < 0) {
            totalPages = (int) Math.ceil((double) totalElements / size);
        }
    }
}
