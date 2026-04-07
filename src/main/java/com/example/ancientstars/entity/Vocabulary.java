package com.example.ancientstars.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.annotations.SQLDelete;

import java.time.LocalDateTime;

/**
 * 词汇实体类
 */
@Entity
@Table(name = "vocabulary", indexes = {
        @Index(name = "idx_word", columnList = "word"),
        @Index(name = "idx_created_by", columnList = "created_by")
})
@SQLDelete(sql = "UPDATE vocabulary SET is_deleted = true, updated_at = NOW() WHERE id = ?")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Vocabulary {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String word;

    @Column(length = 100)
    private String phonetic;

    @Column(columnDefinition = "TEXT")
    private String definition;

    @Column(columnDefinition = "TEXT")
    private String translation;

    @Column(name = "example_sentence", columnDefinition = "TEXT")
    private String example;

    @Column(name = "difficulty_level")
    private Integer difficultyLevel;

    @Column(name = "created_by")
    private Long createdBy;

    @Column(name = "is_deleted", nullable = false)
    private Boolean isDeleted = false;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
