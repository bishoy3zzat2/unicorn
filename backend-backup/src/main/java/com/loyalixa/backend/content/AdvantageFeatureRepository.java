package com.loyalixa.backend.content;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
@Repository
public interface AdvantageFeatureRepository extends JpaRepository<AdvantageFeature, Long> {
    List<AdvantageFeature> findAllByOrderByOrderIndexAsc();
}