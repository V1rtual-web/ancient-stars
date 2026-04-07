package com.example.ancientstars.repository;

import com.example.ancientstars.entity.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 任务数据访问接口
 */
@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {

    /**
     * 根据创建者ID查询任务列表
     */
    List<Task> findByCreatedByOrderByCreatedAtDesc(Long createdBy);

    /**
     * 根据词汇表ID查询任务列表
     */
    List<Task> findByWordListId(Long wordListId);

    /**
     * 查询已过期但状态仍为ACTIVE的任务
     */
    @Query("SELECT t FROM Task t WHERE t.deadline < :now AND t.status = 'ACTIVE'")
    List<Task> findExpiredActiveTasks(@Param("now") LocalDateTime now);

    /**
     * 根据状态查询任务
     */
    List<Task> findByStatus(Task.TaskStatus status);
}
