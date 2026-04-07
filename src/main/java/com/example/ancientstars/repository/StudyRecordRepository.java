package com.example.ancientstars.repository;

import com.example.ancientstars.entity.StudyRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 学习记录数据访问层
 * 用于学生自主背单词功能的学习记录管理
 */
@Repository
public interface StudyRecordRepository extends JpaRepository<StudyRecord, Long> {

    /**
     * 查找学生对特定词汇的学习记录
     * 用于检查学习记录是否已存在（幂等性检查）
     * 
     * @param studentId    学生ID
     * @param vocabularyId 词汇ID
     * @return 学习记录（如果存在）
     */
    Optional<StudyRecord> findByStudentIdAndVocabularyId(Long studentId, Long vocabularyId);

    /**
     * 查找需要复习的学习记录
     * 查询下次复习时间在指定时间之前且不为null的记录
     * 用于复习调度器生成每日复习提醒
     * 
     * @param currentTime 当前时间
     * @return 需要复习的学习记录列表
     */
    @Query("SELECT sr FROM StudyRecord sr WHERE sr.nextReviewTime IS NOT NULL AND sr.nextReviewTime <= :currentTime")
    List<StudyRecord> findRecordsNeedReview(@Param("currentTime") LocalDateTime currentTime);

    /**
     * 统计学生已学习的词汇总数
     * 用于计算学生的累计学习进度
     * 
     * @param studentId 学生ID
     * @return 已学习的词汇数量
     */
    Long countByStudentId(Long studentId);

    /**
     * 查询学生的所有学习记录
     * 用于判断学生是否已学习某个词汇
     * 
     * @param studentId 学生ID
     * @return 学生的所有学习记录
     */
    List<StudyRecord> findByStudentId(Long studentId);

    /**
     * 查询多个学生的学习记录，按学生ID分组统计
     * 用于班级统计中获取每个学生的总学习单词数
     * 
     * @param studentIds 学生ID列表
     * @return 学习记录列表
     */
    List<StudyRecord> findByStudentIdIn(List<Long> studentIds);
}
