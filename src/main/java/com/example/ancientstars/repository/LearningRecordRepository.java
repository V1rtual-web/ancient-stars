package com.example.ancientstars.repository;

import com.example.ancientstars.entity.LearningRecord;
import com.example.ancientstars.entity.LearningRecord.MasteryLevel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 学习记录数据访问层
 */
@Repository
public interface LearningRecordRepository extends JpaRepository<LearningRecord, Long> {

        /**
         * 查找学生对特定词汇和任务的学习记录
         */
        Optional<LearningRecord> findByStudentIdAndVocabularyIdAndTaskId(
                        Long studentId, Long vocabularyId, Long taskId);

        /**
         * 查找学生的所有学习记录（分页）
         */
        Page<LearningRecord> findByStudentId(Long studentId, Pageable pageable);

        /**
         * 查找学生在特定任务中的学习记录
         */
        List<LearningRecord> findByStudentIdAndTaskId(Long studentId, Long taskId);

        /**
         * 查找学生特定掌握程度的词汇记录
         */
        List<LearningRecord> findByStudentIdAndMasteryLevel(Long studentId, MasteryLevel masteryLevel);

        /**
         * 统计学生已掌握的词汇数量
         */
        @Query("SELECT COUNT(lr) FROM LearningRecord lr WHERE lr.studentId = :studentId AND lr.masteryLevel = :masteryLevel")
        long countByStudentIdAndMasteryLevel(@Param("studentId") Long studentId,
                        @Param("masteryLevel") MasteryLevel masteryLevel);

        /**
         * 统计学生在特定任务中的学习进度
         */
        @Query("SELECT COUNT(lr) FROM LearningRecord lr WHERE lr.studentId = :studentId AND lr.taskId = :taskId AND lr.masteryLevel = :masteryLevel")
        long countByStudentIdAndTaskIdAndMasteryLevel(
                        @Param("studentId") Long studentId,
                        @Param("taskId") Long taskId,
                        @Param("masteryLevel") MasteryLevel masteryLevel);

        /**
         * 查找特定词汇的所有学习记录
         */
        List<LearningRecord> findByVocabularyId(Long vocabularyId);

        /**
         * 删除学生的所有学习记录
         */
        void deleteByStudentId(Long studentId);

        /**
         * 删除特定任务的所有学习记录
         */
        void deleteByTaskId(Long taskId);

        /**
         * 查找学生的所有学习记录（不分页）
         */
        List<LearningRecord> findByStudentId(Long studentId);

        /**
         * 查找学生在时间范围内的学习记录
         */
        @Query("SELECT lr FROM LearningRecord lr WHERE lr.studentId = :studentId AND lr.createdAt BETWEEN :startDate AND :endDate")
        List<LearningRecord> findByStudentIdAndCreatedAtBetween(
                        @Param("studentId") Long studentId,
                        @Param("startDate") java.time.LocalDateTime startDate,
                        @Param("endDate") java.time.LocalDateTime endDate);

        /**
         * 统计学生在时间范围内掌握的词汇数量
         */
        @Query("SELECT COUNT(lr) FROM LearningRecord lr WHERE lr.studentId = :studentId AND lr.masteryLevel = :masteryLevel AND lr.updatedAt BETWEEN :startDate AND :endDate")
        long countByStudentIdAndMasteryLevelAndUpdatedAtBetween(
                        @Param("studentId") Long studentId,
                        @Param("masteryLevel") MasteryLevel masteryLevel,
                        @Param("startDate") java.time.LocalDateTime startDate,
                        @Param("endDate") java.time.LocalDateTime endDate);

        /**
         * 查找学生最近的学习记录
         */
        List<LearningRecord> findTop5ByStudentIdOrderByUpdatedAtDesc(Long studentId);
}
