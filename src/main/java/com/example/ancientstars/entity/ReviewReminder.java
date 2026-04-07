package com.example.ancientstars.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * 复习提醒实体类
 */
@Entity
@Table(name = "review_reminder", indexes = {
        @Index(name = "idx_student_remind", columnList = "student_id,remind_time,status"),
        @Index(name = "idx_remind_time", columnList = "remind_time")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReviewReminder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "student_id", nullable = false)
    private Long studentId;

    @Column(name = "vocabulary_id", nullable = false)
    private Long vocabularyId;

    @Column(name = "study_record_id", nullable = false)
    private Long studyRecordId;

    @Column(name = "remind_time", nullable = false)
    private LocalDateTime remindTime;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private Status status = Status.PENDING;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    /**
     * 状态枚举
     */
    public enum Status {
        PENDING, // 待复习
        COMPLETED, // 已完成
        SKIPPED // 已跳过
    }
}
