package com.example.ancientstars.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 每日进度实体类
 */
@Entity
@Table(name = "daily_progress", indexes = {
        @Index(name = "idx_student_id", columnList = "student_id"),
        @Index(name = "idx_study_date", columnList = "study_date")
}, uniqueConstraints = {
        @UniqueConstraint(name = "uk_student_date", columnNames = { "student_id", "study_date" })
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DailyProgress {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "student_id", nullable = false)
    private Long studentId;

    @Column(name = "study_date", nullable = false)
    private LocalDate studyDate;

    @Column(name = "new_words_count", nullable = false)
    private Integer newWordsCount = 0;

    @Column(name = "review_words_count", nullable = false)
    private Integer reviewWordsCount = 0;

    @Column(name = "total_study_time", nullable = false)
    private Integer totalStudyTime = 0;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
