package com.example.ancientstars.dto;

import com.example.ancientstars.entity.TaskAssignment;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 任务分配数据传输对象
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TaskAssignmentDTO {

    private Long id;
    private Long taskId;
    private String taskTitle;
    private Long studentId;
    private String studentName;
    private Integer progress;
    private TaskAssignment.AssignmentStatus status;
    private LocalDateTime assignedAt;
    private LocalDateTime completedAt;
    private LocalDateTime deadline;

    // 任务相关信息
    private Long wordListId;
    private String wordListName;
    private Integer totalWords;
}
