package com.loyalixa.backend.course;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;
@Repository
public interface RankingWeightsRepository extends JpaRepository<RankingWeights, Long> {
    Optional<RankingWeights> findByFactorName(String factorName);
    boolean existsByFactorName(String factorName);
}
