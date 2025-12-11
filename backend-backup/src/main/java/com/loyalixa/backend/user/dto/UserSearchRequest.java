package com.loyalixa.backend.user.dto;
import java.time.LocalDateTime;
import java.util.List;
public record UserSearchRequest(
    String search,
    List<String> roles,
    List<String> statuses,
    String username,
    List<String> authProviders,
    LocalDateTime createdAtFrom,   
    LocalDateTime createdAtTo,     
    LocalDateTime lastLoginFrom,   
    LocalDateTime lastLoginTo,     
    LocalDateTime passwordChangedFrom,   
    LocalDateTime passwordChangedTo,     
    LocalDateTime suspendedFrom,   
    LocalDateTime suspendedTo,     
    LocalDateTime bannedFrom,      
    LocalDateTime bannedTo,        
    List<String> deviceTypes,     
    List<String> browsers,        
    List<String> operatingSystems,  
    String ipAddress,      
    Integer maxDevices,    
    String appealStatus,   
    Boolean inverse,       
    Integer page,       
    Integer size        
) {
    public UserSearchRequest {
        if (page == null || page < 0) page = 0;
        if (size == null || size <= 0) size = 10;
        if (size > 100) size = 100;  
    }
}
