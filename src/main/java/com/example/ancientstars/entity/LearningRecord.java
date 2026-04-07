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
@Table(name = "learning_record", uniqueConstraints = {
        @UniqueConstraint(name = "uk_student_vocab_task", columnNames = { "student_id", "vocabulary_id", "task_id" })
}, indexes = {
        @Index(name = "idx_student", columnList = "student_id"),
        @Index(name = "idx_vocabulary", columnList = "vocabulary_id"),
        @Index(name = "idx_task", columnList = "task_id"),
        @Index(name = "idx_mastery_level", columnList = "mastery_level")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LearningRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "student_id", nullable = false)
    private Long studentId;

    @Column(name = "vocabulary_id", nullable = false)
    private Long vocabularyId;

    @Column(name = "task_id")
    private Long taskId;

    @Enumerated(EnumType.STRING)
    @Column(name = "mastery_level", nullable = false, length = 20)
    private MasteryLevel masteryLevel = MasteryLevel.LEARNING;

    @Column(name = "review_count", nullable = false)
    private Integer reviewCount = 0;

    @Column(name = "last_reviewed_at")
    private LocalDateTime lastReviewedAt;

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
        UNKNOWN, // 未知
        LEARNING, // 学习中
        FAMILIAR, // 熟悉
        MASTERED // 已掌握
    }
}
