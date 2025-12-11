package com.loyalixa.backend.course;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import com.fasterxml.jackson.annotation.JsonIgnore;

@Data
@NoArgsConstructor
@Entity
@Table(name = "questions")
public class Question {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String questionText;

    @Column(nullable = false)
    private Integer orderIndex; // ترتيب السؤال في الاختبار

    @Column(nullable = false)
    private String questionType; // (MCQ, TRUE_FALSE, ESSAY, CODING, AI_INTERVIEW)

    @Column(nullable = false)
    private Integer points; // درجة السؤال

    @Column(columnDefinition = "TEXT")
    private String correctAnswer; // الإجابة الصحيحة (لـ MCQ و T/F)

    @Column(columnDefinition = "TEXT")
    private String optionsJson; // خيارات الإجابة (تُخزن كـ JSON)

    // --- الروابط ---
    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "quiz_id", nullable = false)
    private Quiz quiz;
}