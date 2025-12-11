package com.loyalixa.backend.content;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;
@Repository
public interface HeroSliderRepository extends JpaRepository<HeroSlider, Long> {
       List<HeroSlider> findAllByOrderByOrderIndexAsc();
       @Query("SELECT DISTINCT h FROM HeroSlider h " +
                     "LEFT JOIN FETCH h.createdBy cb " +
                     "LEFT JOIN FETCH cb.role " +
                     "LEFT JOIN FETCH h.updatedBy ub " +
                     "LEFT JOIN FETCH ub.role " +
                     "WHERE h.id = :id")
       Optional<HeroSlider> findByIdWithRelations(Long id);
       @Query("SELECT DISTINCT h FROM HeroSlider h " +
                     "LEFT JOIN FETCH h.createdBy cb " +
                     "LEFT JOIN FETCH cb.role " +
                     "LEFT JOIN FETCH h.updatedBy ub " +
                     "LEFT JOIN FETCH ub.role " +
                     "ORDER BY h.orderIndex ASC, h.id ASC")
       List<HeroSlider> findAllWithCreatedAndUpdatedBy();
       @Query("SELECT DISTINCT h FROM HeroSlider h " +
                     "LEFT JOIN FETCH h.createdBy cb " +
                     "LEFT JOIN FETCH cb.role " +
                     "LEFT JOIN FETCH h.updatedBy ub " +
                     "LEFT JOIN FETCH ub.role " +
                     "WHERE h.id = :id")
       Optional<HeroSlider> findByIdWithCreatedAndUpdatedBy(Long id);
}
