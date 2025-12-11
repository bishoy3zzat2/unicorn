package com.loyalixa.backend.user;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;  
import org.hibernate.type.SqlTypes;  
import java.util.UUID;
@Data
@NoArgsConstructor
@Entity
@Table(name = "user_profiles")
public class UserProfile {
    @Id  
    private UUID id;
    @OneToOne(fetch = FetchType.LAZY)  
    @MapsId  
    @JoinColumn(name = "user_id")  
    private User user;
    private String firstName;
    private String lastName;
    @Column(columnDefinition = "TEXT")  
    private String bio;
    private String avatarUrl;
    private String phoneNumber;
    private String phoneSocialApp;  
    @Column(unique = true)  
    private String secondaryEmail;
    private String tshirtSize;
    @JdbcTypeCode(SqlTypes.JSON)  
    @Column(columnDefinition = "jsonb")  
    private String extraInfo;  
}