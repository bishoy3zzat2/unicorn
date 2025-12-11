package com.loyalixa.backend.course;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
@Data
@NoArgsConstructor
@Entity
@Table(name = "categories")
public class Category {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, unique = true, length = 100)
    private String name;  
    @Column(nullable = false, unique = true, length = 100)
    private String slug;  
    @Column(length = 50)
    private String iconClass;  
    private Integer orderIndex;  
}