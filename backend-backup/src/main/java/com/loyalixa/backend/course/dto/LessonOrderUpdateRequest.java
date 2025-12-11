package com.loyalixa.backend.course.dto;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;
public record LessonOrderUpdateRequest(
    @NotNull(message = "Section ID is required.")
    Long sectionId,
    @NotEmpty(message = "Lessons list cannot be empty.")
    List<@NotNull LessonOrderItem> lessons
) {
    public record LessonOrderItem(
        @NotNull(message = "Lesson ID is required.")
        Long lessonId,
        @NotNull(message = "Order index is required.")
        Integer orderIndex
    ) {}
}
