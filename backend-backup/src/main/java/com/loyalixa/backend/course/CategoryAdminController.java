package com.loyalixa.backend.course;
import com.loyalixa.backend.course.dto.CategoryRequest;
import com.loyalixa.backend.course.dto.CategoryResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;
@RestController
@RequestMapping("/api/v1/admin/categories")
public class CategoryAdminController {
    private final CategoryService categoryService;
    public CategoryAdminController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }
    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('category:get_all')")
    public ResponseEntity<List<CategoryResponse>> getAllCategories() {
        List<CategoryResponse> categories = categoryService.getAllCategoriesResponse();
        return ResponseEntity.ok(categories);
    }
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('category:get_all')")
    public ResponseEntity<CategoryResponse> getCategory(@PathVariable Long id) {
        try {
            CategoryResponse response = categoryService.getCategoryResponse(id);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }
    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('category:create')")
    public ResponseEntity<CategoryResponse> createCategory(@Valid @RequestBody CategoryRequest request) {
        try {
            Category newCategory = categoryService.createCategory(request);
            CategoryResponse response = categoryService.getCategoryResponse(newCategory.getId());
            return new ResponseEntity<>(response, HttpStatus.CREATED);
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
    }
    @PutMapping("/{categoryId}")
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('category:update')")
    public ResponseEntity<CategoryResponse> updateCategory(
            @PathVariable Long categoryId,
            @Valid @RequestBody CategoryRequest request
    ) {
        try {
            Category updatedCategory = categoryService.updateCategory(categoryId, request);
            CategoryResponse response = categoryService.getCategoryResponse(updatedCategory.getId());
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
    }
    @DeleteMapping("/{categoryId}")
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('category:delete')")
    public ResponseEntity<?> deleteCategory(@PathVariable Long categoryId) {
        try {
            categoryService.deleteCategory(categoryId);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                java.util.Map.of("error", e.getMessage())
            );
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(
                java.util.Map.of("error", e.getMessage())
            );
        }
    }
}