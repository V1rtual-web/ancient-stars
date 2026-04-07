package com.example.ancientstars.dto;

import com.example.ancientstars.entity.Task;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 任务数据传输对象
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TaskDTO {

    private Long id;
    private String title;
    private String description;
    private Long wordListId;
    private String wordListName;
    private Long createdBy;
    private String creatorName;
    private LocalDateTime deadline;
    private Task.TaskStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // 任务统计信息
    private Integer totalStudents;
    private Integer completedStudents;
    private Integer totalWords;

    // 分配的学生ID列表（用于创建任务时）
    private List<Long> studentIds;
}
