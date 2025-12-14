package com.unicorn.backend.appconfig;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for AppConfig entity operations.
 */
@Repository
public interface AppConfigRepository extends JpaRepository<AppConfig, String> {

    /**
     * Find all configs by category.
     */
    List<AppConfig> findByCategory(String category);

    /**
     * Find all configs ordered by category and key.
     */
    List<AppConfig> findAllByOrderByCategoryAscKeyAsc();
}
