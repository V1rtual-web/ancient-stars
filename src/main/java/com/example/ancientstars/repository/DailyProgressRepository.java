package com.example.ancientstars.repository;

import com.example.ancientstars.entity.DailyProgress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * 每日进度数据访问层
 * 用于学生每日学习进度的统计和查询
 */
@Repository
public interface DailyProgressRepository extends JpaRepository<DailyProgress, Long> {

        /**
         * 查找学生在特定日期的学习进度记录
         * 用于更新每日进度统计
         * 
         * @param studentId 学生ID
         * @param studyDate 学习日期
         * @return 每日进度记录（如果存在）
         */
        Optional<DailyProgress> findByStudentIdAndStudyDate(Long studentId, LocalDate studyDate);

        /**
         * 查询学生在指定日期范围内的学习进度记录
         * 用于学生查看自己的学习进度统计和趋势
         * 
         * @param studentId 学生ID
         * @param startDate 开始日期（包含）
         * @param endDate   结束日期（包含）
         * @return 日期范围内的进度记录列表，按日期升序排列
         */
        List<DailyProgress> findByStudentIdAndStudyDateBetweenOrderByStudyDateAsc(
                        Long studentId, LocalDate startDate, LocalDate endDate);

        /**
         * 查询学生从指定日期开始的所有学习进度记录
         * 用于查询学生的累计学习数据
         * 
         * @param studentId 学生ID
         * @param startDate 开始日期（包含）
         * @return 从开始日期至今的进度记录列表，按日期升序排列
         */
        List<DailyProgress> findByStudentIdAndStudyDateGreaterThanEqualOrderByStudyDateAsc(
                        Long studentId, LocalDate startDate);

        /**
         * 查询多个学生在指定日期范围内的学习进度记录
         * 用于班级统计
         * 
         * @param studentIds 学生ID列表
         * @param startDate  开始日期（包含）
         * @param endDate    结束日期（包含）
         * @return 日期范围内的进度记录列表
         */
        List<DailyProgress> findByStudentIdInAndStudyDateBetween(
                        List<Long> studentIds, LocalDate startDate, LocalDate endDate);
}
