package com.loyalixa.backend.course;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
    Boolean existsByName(String name);
    Boolean existsBySlug(String slug);
    Boolean existsBySlugAndIdNot(String slug, Long id);
}