package com.loyalixa.backend.course;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;
import java.util.UUID; 
@Repository
public interface TagRepository extends JpaRepository<Tag, UUID> {
    Optional<Tag> findByNameIgnoreCase(String name); 
    boolean existsByNameIgnoreCase(String name);
    boolean existsByNameIgnoreCaseAndIdNot(String name, UUID id);
    @Query("SELECT DISTINCT t FROM Tag t " +
           "LEFT JOIN FETCH t.createdBy cb " +
           "LEFT JOIN FETCH cb.role " +
           "LEFT JOIN FETCH t.updatedBy ub " +
           "LEFT JOIN FETCH ub.role " +
           "LEFT JOIN FETCH t.courses " +
           "WHERE t.id = :id")
    Optional<Tag> findByIdWithRelations(UUID id);
    @Query("SELECT DISTINCT t FROM Tag t " +
           "LEFT JOIN FETCH t.createdBy " +
           "LEFT JOIN FETCH t.updatedBy")
    List<Tag> findAllWithCreatedAndUpdatedBy();
    @Query("SELECT DISTINCT t FROM Tag t " +
           "LEFT JOIN FETCH t.createdBy " +
           "LEFT JOIN FETCH t.updatedBy " +
           "WHERE t.id = :id")
    Optional<Tag> findByIdWithCreatedAndUpdatedBy(UUID id);
}