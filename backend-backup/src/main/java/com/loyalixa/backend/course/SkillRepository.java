package com.loyalixa.backend.course;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
@Repository
public interface SkillRepository extends JpaRepository<Skill, UUID> {
    boolean existsByNameIgnoreCase(String name);
    boolean existsByNameIgnoreCaseAndIdNot(String name, UUID id);
    Optional<Skill> findByNameIgnoreCase(String name);
    @Query("SELECT DISTINCT s FROM Skill s " +
           "LEFT JOIN FETCH s.createdBy cb " +
           "LEFT JOIN FETCH cb.role " +
           "LEFT JOIN FETCH s.updatedBy ub " +
           "LEFT JOIN FETCH ub.role " +
           "LEFT JOIN FETCH s.courses " +
           "WHERE s.id = :id")
    Optional<Skill> findByIdWithRelations(UUID id);
    @Query("SELECT DISTINCT s FROM Skill s " +
           "LEFT JOIN FETCH s.createdBy " +
           "LEFT JOIN FETCH s.updatedBy")
    List<Skill> findAllWithCreatedAndUpdatedBy();
    @Query("SELECT DISTINCT s FROM Skill s " +
           "LEFT JOIN FETCH s.createdBy " +
           "LEFT JOIN FETCH s.updatedBy " +
           "WHERE s.id = :id")
    Optional<Skill> findByIdWithCreatedAndUpdatedBy(UUID id);
}