package com.loyalixa.backend.user;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.io.Serializable;  
import java.util.UUID;
@Entity
@Table(name = "instructor_followers")
@Data
@IdClass(InstructorFollower.InstructorFollowerId.class)  
public class InstructorFollower implements Serializable {
    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "instructor_id")
    private User instructor;
    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "follower_id")
    private User follower;  
    @Data
    @NoArgsConstructor
    public static class InstructorFollowerId implements Serializable {
        private UUID instructor;
        private UUID follower;
    }
}