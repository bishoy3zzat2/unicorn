package com.loyalixa.backend.content;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;
@Repository
public interface FaqRepository extends JpaRepository<Faq, Long> {
       List<Faq> findByCategoryOrderByOrderIndexAsc(String category);
       @Query("SELECT DISTINCT f FROM Faq f " +
                     "LEFT JOIN FETCH f.createdBy cb " +
                     "LEFT JOIN FETCH cb.role " +
                     "LEFT JOIN FETCH f.updatedBy ub " +
                     "LEFT JOIN FETCH ub.role " +
                     "WHERE f.id = :id")
       Optional<Faq> findByIdWithRelations(Long id);
       @Query("SELECT DISTINCT f FROM Faq f " +
                     "LEFT JOIN FETCH f.createdBy cb " +
                     "LEFT JOIN FETCH cb.role " +
                     "LEFT JOIN FETCH f.updatedBy ub " +
                     "LEFT JOIN FETCH ub.role " +
                     "ORDER BY f.orderIndex ASC, f.id ASC")
       List<Faq> findAllWithCreatedAndUpdatedBy();
       @Query("SELECT DISTINCT f FROM Faq f " +
                     "LEFT JOIN FETCH f.createdBy cb " +
                     "LEFT JOIN FETCH cb.role " +
                     "LEFT JOIN FETCH f.updatedBy ub " +
                     "LEFT JOIN FETCH ub.role " +
                     "WHERE f.id = :id")
       Optional<Faq> findByIdWithCreatedAndUpdatedBy(Long id);
}