package com.example.ancientstars.repository;

import com.example.ancientstars.entity.TestRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 测试记录Repository
 */
@Repository
public interface TestRecordRepository extends JpaRepository<TestRecord, Long> {

    /**
     * 根据学生ID查询测试记录（分页）
     */
    Page<TestRecord> findByStudentIdOrderByCreatedAtDesc(Long studentId, Pageable pageable);

    /**
     * 根据学生ID和任务ID查询测试记录
     */
    List<TestRecord> findByStudentIdAndTaskId(Long studentId, Long taskId);

    /**
     * 根据任务ID查询测试记录
     */
    List<TestRecord> findByTaskId(Long taskId);

    /**
     * 根据学生ID查询所有测试记录（不分页）
     */
    List<TestRecord> findByStudentIdOrderByCreatedAtDesc(Long studentId);

    /**
     * 根据学生ID和时间范围查询测试记录
     */
    List<TestRecord> findByStudentIdAndCreatedAtBetweenOrderByCreatedAtDesc(
            Long studentId,
            java.time.LocalDateTime startDate,
            java.time.LocalDateTime endDate);

    /**
     * 查找学生最近的测试记录
     */
    List<TestRecord> findTop5ByStudentIdOrderByCreatedAtDesc(Long studentId);
}
