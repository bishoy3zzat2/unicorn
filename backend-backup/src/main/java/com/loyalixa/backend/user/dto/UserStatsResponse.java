package com.loyalixa.backend.user.dto;
import java.util.List;
public record UserStatsResponse(
    long total,            
    long active,           
    long suspended,        
    long banned,           
    List<RoleStats> roles,  
    long totalSubscriptions,       
    long activeSubscriptions,      
    long expiredSubscriptions,     
    long cancelledSubscriptions,   
    long freePlanSubscriptions     
) {
    public record RoleStats(
        String roleName,   
        long count         
    ) {}
}
