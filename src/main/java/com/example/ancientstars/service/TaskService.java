package com.example.ancientstars.service;

import com.example.ancientstars.common.ErrorCode;
import com.example.ancientstars.dto.TaskAssignmentDTO;
import com.example.ancientstars.dto.TaskDTO;
import com.example.ancientstars.dto.TaskProgressUpdateRequest;
import com.example.ancientstars.entity.Task;
import com.example.ancientstars.entity.TaskAssignment;
import com.example.ancientstars.entity.User;
import com.example.ancientstars.entity.WordList;
import com.example.ancientstars.exception.BusinessException;
import com.example.ancientstars.repository.TaskAssignmentRepository;
import com.example.ancientstars.repository.TaskRepository;
import com.example.ancientstars.repository.UserRepository;
import com.example.ancientstars.repository.WordListRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 任务服务类
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TaskService {

    private final TaskRepository taskRepository;
    private final TaskAssignmentRepository taskAssignmentRepository;
    private final WordListRepository wordListRepository;
    private final UserRepository userRepository;

    /**
     * 创建任务并分配给学生
     */
    @Transactional
    public TaskDTO createTask(TaskDTO taskDTO) {
        // 验证词汇表是否存在
        WordList wordList = wordListRepository.findById(taskDTO.getWordListId())
                .orElseThrow(() -> new BusinessException(ErrorCode.WORD_LIST_NOT_FOUND));

        // 创建任务
        Task task = new Task();
        task.setTitle(taskDTO.getTitle());
        task.setDescription(taskDTO.getDescription());
        task.setWordListId(taskDTO.getWordListId());
        task.setCreatedBy(taskDTO.getCreatedBy());
        task.setDeadline(taskDTO.getDeadline());
        task.setStatus(Task.TaskStatus.ACTIVE);

        task = taskRepository.save(task);

        // 分配任务给学生
        if (taskDTO.getStudentIds() != null && !taskDTO.getStudentIds().isEmpty()) {
            assignTaskToStudents(task.getId(), taskDTO.getStudentIds());
        }

        return convertToDTO(task);
    }

    /**
     * 分配任务给学生
     */
    @Transactional
    public void assignTaskToStudents(Long taskId, List<Long> studentIds) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new BusinessException(ErrorCode.TASK_NOT_FOUND));

        for (Long studentId : studentIds) {
            // 验证学生是否存在
            User student = userRepository.findById(studentId)
                    .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

            if (student.getRole() != User.Role.STUDENT) {
                throw new BusinessException(ErrorCode.INVALID_USER_ROLE);
            }

            // 检查是否已经分配过
            if (taskAssignmentRepository.findByTaskIdAndStudentId(taskId, studentId).isPresent()) {
                log.warn("Task {} already assigned to student {}", taskId, studentId);
                continue;
            }

            // 创建任务分配记录
            TaskAssignment assignment = new TaskAssignment();
            assignment.setTaskId(taskId);
            assignment.setStudentId(studentId);
            assignment.setProgress(0);
            assignment.setStatus(TaskAssignment.AssignmentStatus.NOT_STARTED);

            taskAssignmentRepository.save(assignment);
            log.info("Task {} assigned to student {}", taskId, studentId);
        }
    }

/**
     * 更新任务进度
     */
    @Transactional
    public void updateTaskProgress(Long taskId, TaskProgressUpdateRequest request) {
        TaskAssignment assignment = taskAssignmentRepository
                .findByTaskIdAndStudentId(taskId, request.getStudentId())
                .orElseThrow(() -> new BusinessException(ErrorCode.TASK_ASSIGNMENT_NOT_FOUND));

        assignment.setProgress(request.getProgress());

        // 更新状态
        if (request.getProgress() == 0) {
            assignment.setStatus(TaskAssignment.AssignmentStatus.NOT_STARTED);
        } else if (request.getProgress() >= 100) {
            assignment.setStatus(TaskAssignment.AssignmentStatus.COMPLETED);
            assignment.setCompletedAt(LocalDateTime.now());
        } else {
            assignment.setStatus(TaskAssignment.AssignmentStatus.IN_PROGRESS);
        }

        taskAssignmentRepository.save(assignment);
        log.info("Updated task {} progress for student {}: {}%", taskId, request.getStudentId(), request.getProgress());
    }

    /**
     * 获取任务详情
     */
    public TaskDTO getTaskById(Long taskId) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new BusinessException(ErrorCode.TASK_NOT_FOUND));
        return convertToDTO(task);
    }

    /**
     * 获取教师创建的所有任务
     */
    public List<TaskDTO> getTasksByTeacher(Long teacherId) {
        List<Task> tasks = taskRepository.findByCreatedByOrderByCreatedAtDesc(teacherId);
        return tasks.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * 获取学生的任务列表
     */
    public List<TaskAssignmentDTO> getStudentTasks(Long studentId) {
        List<TaskAssignment> assignments = taskAssignmentRepository
                .findByStudentIdOrderByAssignedAtDesc(studentId);
        
        return assignments.stream()
                .map(this::convertAssignmentToDTO)
                .collect(Collectors.toList());
    }

    /**
     * 获取任务的所有分配记录
     */
    public List<TaskAssignmentDTO> getTaskAssignments(Long taskId) {
        List<TaskAssignment> assignments = taskAssignmentRepository.findByTaskId(taskId);
        return assignments.stream()
                .map(this::convertAssignmentToDTO)
                .collect(Collectors.toList());
    }

    /**
     * 更新任务信息
     */
    @Transactional
    public TaskDTO updateTask(Long taskId, TaskDTO taskDTO) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new BusinessException(ErrorCode.TASK_NOT_FOUND));

        if (taskDTO.getTitle() != null) {
            task.setTitle(taskDTO.getTitle());
        }
        if (taskDTO.getDescription() != null) {
            task.setDescription(taskDTO.getDescription());
        }
        if (taskDTO.getDeadline() != null) {
            task.setDeadline(taskDTO.getDeadline());
        }
        if (taskDTO.getStatus() != null) {
            task.setStatus(taskDTO.getStatus());
        }

        task = taskRepository.save(task);
        return convertToDTO(task);
    }

    /**
     * 删除任务
     */
    @Transactional
    public void deleteTask(Long taskId) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new BusinessException(ErrorCode.TASK_NOT_FOUND));
        taskRepository.delete(task);
        log.info("Task {} deleted", taskId);
    }

    /**
     * 定时检查过期任务
     * 每小时执行一次
     */
    @Scheduled(cron = "0 0 * * * ?")
    @Transactional
    public void checkExpiredTasks() {
        log.info("Starting expired tasks check...");
        LocalDateTime now = LocalDateTime.now();
        List<Task> expiredTasks = taskRepository.findExpiredActiveTasks(now);

        for (Task task : expiredTasks) {
            task.setStatus(Task.TaskStatus.EXPIRED);
            taskRepository.save(task);
            log.info("Task {} marked as expired", task.getId());
        }

        log.info("Expired tasks check completed. {} tasks marked as expired", expiredTasks.size());
    }

    /**
     * 转换Task实体为DTO
     */
    private TaskDTO convertToDTO(Task task) {
        TaskDTO dto = new TaskDTO();
        dto.setId(task.getId());
        dto.setTitle(task.getTitle());
        dto.setDescription(task.getDescription());
        dto.setWordListId(task.getWordListId());
        dto.setCreatedBy(task.getCreatedBy());
        dto.setDeadline(task.getDeadline());
        dto.setStatus(task.getStatus());
        dto.setCreatedAt(task.getCreatedAt());
        dto.setUpdatedAt(task.getUpdatedAt());

        // 获取词汇表信息
        wordListRepository.findById(task.getWordListId()).ifPresent(wordList -> {
            dto.setWordListName(wordList.getName());
            dto.setTotalWords(wordList.getWordCount());
        });

        // 获取创建者信息
        userRepository.findById(task.getCreatedBy()).ifPresent(user -> {
            dto.setCreatorName(user.getRealName() != null ? user.getRealName() : user.getUsername());
        });

        // 获取任务统计信息
        long totalStudents = taskAssignmentRepository.countByTaskId(task.getId());
        long completedStudents = taskAssignmentRepository.countCompletedByTaskId(task.getId());
        dto.setTotalStudents((int) totalStudents);
        dto.setCompletedStudents((int) completedStudents);

        return dto;
    }

    /**
     * 转换TaskAssignment实体为DTO
     */
    private TaskAssignmentDTO convertAssignmentToDTO(TaskAssignment assignment) {
        TaskAssignmentDTO dto = new TaskAssignmentDTO();
        dto.setId(assignment.getId());
        dto.setTaskId(assignment.getTaskId());
        dto.setStudentId(assignment.getStudentId());
        dto.setProgress(assignment.getProgress());
        dto.setStatus(assignment.getStatus());
        dto.setAssignedAt(assignment.getAssignedAt());
        dto.setCompletedAt(assignment.getCompletedAt());

        // 获取任务信息
        taskRepository.findById(assignment.getTaskId()).ifPresent(task -> {
            dto.setTaskTitle(task.getTitle());
            dto.setDeadline(task.getDeadline());
            dto.setWordListId(task.getWordListId());

            // 获取词汇表信息
            wordListRepository.findById(task.getWordListId()).ifPresent(wordList -> {
                dto.setWordListName(wordList.getName());
                dto.setTotalWords(wordList.getWordCount());
            });
        });

        // 获取学生信息
        userRepository.findById(assignment.getStudentId()).ifPresent(user -> {
            dto.setStudentName(user.getRealName() != null ? user.getRealName() : user.getUsername());
        });

        return dto;
    }
}
