package com.example.ancientstars.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * 学习记录实体类
 */
@Entity
@Table(name = "study_record", indexes = {
        @Index(name = "idx_student_id", columnList = "student_id"),
        @Index(name = "idx_next_review_time", columnList = "next_review_time")
}, uniqueConstraints = {
        @UniqueConstraint(name = "uk_student_vocabulary", columnNames = { "student_id", "vocabulary_id" })
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class StudyRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "student_id", nullable = false)
    private Long studentId;

    @Column(name = "vocabulary_id", nullable = false)
    private Long vocabularyId;

    @Column(name = "study_time", nullable = false)
    private LocalDateTime studyTime;

    @Column(name = "review_count", nullable = false)
    private Integer reviewCount = 0;

    @Column(name = "last_review_time")
    private LocalDateTime lastReviewTime;

    @Column(name = "next_review_time")
    private LocalDateTime nextReviewTime;

    @Enumerated(EnumType.STRING)
    @Column(name = "mastery_level", nullable = false, length = 20)
    private MasteryLevel masteryLevel = MasteryLevel.NEW;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    /**
     * 掌握程度枚举
     */
    public enum MasteryLevel {
        NEW, // 新学习
        LEARNING, // 学习中
        FAMILIAR, // 熟悉
        MASTERED // 已掌握
    }
}
