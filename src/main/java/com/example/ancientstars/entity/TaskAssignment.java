package com.example.ancientstars.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * 任务分配实体类
 */
@Entity
@Table(name = "task_assignment", uniqueConstraints = @UniqueConstraint(name = "uk_task_student", columnNames = {
        "task_id", "student_id" }), indexes = {
                @Index(name = "idx_task_id", columnList = "task_id"),
                @Index(name = "idx_student_id", columnList = "student_id"),
                @Index(name = "idx_status", columnList = "status")
        })
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TaskAssignment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "task_id", nullable = false)
    private Long taskId;

    @Column(name = "student_id", nullable = false)
    private Long studentId;

    @Column(nullable = false)
    private Integer progress = 0;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AssignmentStatus status = AssignmentStatus.NOT_STARTED;

    @CreationTimestamp
    @Column(name = "assigned_at", nullable = false, updatable = false)
    private LocalDateTime assignedAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    public enum AssignmentStatus {
        NOT_STARTED, IN_PROGRESS, COMPLETED
    }
}
