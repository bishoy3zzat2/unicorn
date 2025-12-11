package com.loyalixa.backend.user;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.UUID;
@Repository
public interface UserProfileRepository extends JpaRepository<UserProfile, UUID> {
    @Modifying(clearAutomatically = true)
    @Query("UPDATE UserProfile p SET p.firstName = :firstName, p.lastName = :lastName, " +
           "p.bio = :bio, p.avatarUrl = :avatarUrl, p.phoneNumber = :phoneNumber, " +
           "p.phoneSocialApp = :phoneSocialApp, p.secondaryEmail = :secondaryEmail, " +
           "p.tshirtSize = :tshirtSize, p.extraInfo = :extraInfo " +
           "WHERE p.id = :userId")
    int updateProfileFields(@Param("userId") UUID userId,
                           @Param("firstName") String firstName,
                           @Param("lastName") String lastName,
                           @Param("bio") String bio,
                           @Param("avatarUrl") String avatarUrl,
                           @Param("phoneNumber") String phoneNumber,
                           @Param("phoneSocialApp") String phoneSocialApp,
                           @Param("secondaryEmail") String secondaryEmail,
                           @Param("tshirtSize") String tshirtSize,
                           @Param("extraInfo") String extraInfo);
}
