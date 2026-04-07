package com.example.ancientstars.repository;

import com.example.ancientstars.entity.TaskAssignment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 任务分配数据访问接口
 */
@Repository
public interface TaskAssignmentRepository extends JpaRepository<TaskAssignment, Long> {

    /**
     * 根据任务ID查询所有分配记录
     */
    List<TaskAssignment> findByTaskId(Long taskId);

    /**
     * 根据学生ID查询所有分配记录
     */
    List<TaskAssignment> findByStudentIdOrderByAssignedAtDesc(Long studentId);

    /**
     * 根据任务ID和学生ID查询分配记录
     */
    Optional<TaskAssignment> findByTaskIdAndStudentId(Long taskId, Long studentId);

    /**
     * 根据学生ID和状态查询分配记录
     */
    List<TaskAssignment> findByStudentIdAndStatus(Long studentId, TaskAssignment.AssignmentStatus status);

    /**
     * 统计任务的完成人数
     */
    @Query("SELECT COUNT(ta) FROM TaskAssignment ta WHERE ta.taskId = :taskId AND ta.status = 'COMPLETED'")
    long countCompletedByTaskId(@Param("taskId") Long taskId);

    /**
     * 统计任务的总分配人数
     */
    long countByTaskId(Long taskId);

    /**
     * 根据学生ID列表查询所有分配记录
     */
    List<TaskAssignment> findByStudentIdIn(List<Long> studentIds);
}
