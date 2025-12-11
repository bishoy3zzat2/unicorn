package com.loyalixa.backend.course;
import com.loyalixa.backend.course.dto.CategoryRequest;
import com.loyalixa.backend.course.dto.CategoryResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.stream.Collectors;
@Service
public class CategoryService {
    private final CategoryRepository categoryRepository;
    public CategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }
    @Transactional
    public Category createCategory(CategoryRequest request) {
        if (categoryRepository.existsByName(request.name())) {
            throw new IllegalStateException("Category name already exists.");
        }
        if (categoryRepository.existsBySlug(request.slug())) {
            throw new IllegalStateException("Category slug already exists.");
        }
        Category newCategory = new Category();
        newCategory.setName(request.name());
        newCategory.setSlug(request.slug());
        if (request.iconClass() != null && !request.iconClass().trim().isEmpty()) {
            newCategory.setIconClass(request.iconClass().trim());
        }
        if (request.orderIndex() != null) {
            newCategory.setOrderIndex(request.orderIndex());
        }
        return categoryRepository.save(newCategory);
    }
    @Transactional
    public Category updateCategory(Long categoryId, CategoryRequest request) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new IllegalArgumentException("Category not found."));
        if (!category.getName().equals(request.name()) && 
            categoryRepository.existsByName(request.name())) {
            throw new IllegalStateException("Another category with this name already exists.");
        }
        if (!category.getSlug().equals(request.slug()) && 
            categoryRepository.existsBySlugAndIdNot(request.slug(), categoryId)) {
            throw new IllegalStateException("Another category with this slug already exists.");
        }
        category.setName(request.name());
        category.setSlug(request.slug());
        if (request.iconClass() != null && !request.iconClass().trim().isEmpty()) {
            category.setIconClass(request.iconClass().trim());
        } else {
            category.setIconClass(null);
        }
        if (request.orderIndex() != null) {
            category.setOrderIndex(request.orderIndex());
        } else {
            category.setOrderIndex(null);
        }
        return categoryRepository.save(category);
    }
    @Transactional(readOnly = true)
    public List<CategoryResponse> getAllCategoriesResponse() {
        List<Category> categories = categoryRepository.findAll();
        return categories.stream()
                .map(this::mapToCategoryResponse)
                .collect(Collectors.toList());
    }
    @Transactional(readOnly = true)
    public CategoryResponse getCategoryResponse(Long categoryId) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new IllegalArgumentException("Category not found."));
        return mapToCategoryResponse(category);
    }
    @Transactional
    public void deleteCategory(Long categoryId) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new IllegalArgumentException("Category not found."));
        categoryRepository.delete(category);
    }
    private CategoryResponse mapToCategoryResponse(Category category) {
        return new CategoryResponse(
                category.getId(),
                category.getName(),
                category.getSlug(),
                category.getIconClass(),
                category.getOrderIndex()
        );
    }
}
