package com.example.ancientstars.service;

import com.example.ancientstars.config.CacheConfig;
import com.example.ancientstars.dto.ClassProgressDTO;
import com.example.ancientstars.dto.StudentProgressDTO;
import com.example.ancientstars.entity.DailyProgress;
import com.example.ancientstars.entity.StudyRecord;
import com.example.ancientstars.entity.User;
import com.example.ancientstars.repository.DailyProgressRepository;
import com.example.ancientstars.repository.StudyRecordRepository;
import com.example.ancientstars.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 每日进度服务
 * 负责管理学生每日学习进度的统计和查询
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class DailyProgressService {

        private final DailyProgressRepository dailyProgressRepository;
        private final StudyRecordRepository studyRecordRepository;
        private final UserRepository userRepository;

        /**
         * 更新每日进度（增量更新）
         * 在同一事务中更新学生的每日学习统计
         * 
         * @param studentId 学生ID
         * @param studyDate 学习日期
         * @param isNewWord 是否为新学单词（true: 新学，false: 复习）
         */
        @Transactional
        @CacheEvict(value = { CacheConfig.STUDENT_PROGRESS_CACHE, CacheConfig.CLASS_PROGRESS_CACHE }, allEntries = true)
        public void updateDailyProgress(Long studentId, LocalDate studyDate, boolean isNewWord) {
                log.debug("更新每日进度 - 学生ID: {}, 日期: {}, 新学: {}", studentId, studyDate, isNewWord);

                Optional<DailyProgress> existingProgress = dailyProgressRepository
                                .findByStudentIdAndStudyDate(studentId, studyDate);

                DailyProgress progress;
                if (existingProgress.isPresent()) {
                        progress = existingProgress.get();
                } else {
                        progress = new DailyProgress();
                        progress.setStudentId(studentId);
                        progress.setStudyDate(studyDate);
                        progress.setNewWordsCount(0);
                        progress.setReviewWordsCount(0);
                        progress.setTotalStudyTime(0);
                }

                // 增量更新计数
                if (isNewWord) {
                        progress.setNewWordsCount(progress.getNewWordsCount() + 1);
                } else {
                        progress.setReviewWordsCount(progress.getReviewWordsCount() + 1);
                }

                dailyProgressRepository.save(progress);
                log.debug("每日进度更新成功 - 新学: {}, 复习: {}",
                                progress.getNewWordsCount(), progress.getReviewWordsCount());
        }

        /**
         * 异步更新每日进度
         * 用于非关键路径的进度更新，提高系统响应速度
         * 
         * @param studentId 学生ID
         * @param studyDate 学习日期
         * @param isNewWord 是否为新学单词（true: 新学，false: 复习）
         */
        @Async
        @Transactional
        public void updateDailyProgressAsync(Long studentId, LocalDate studyDate, boolean isNewWord) {
                log.debug("异步更新每日进度 - 学生ID: {}, 日期: {}, 新学: {}", studentId, studyDate, isNewWord);
                updateDailyProgress(studentId, studyDate, isNewWord);
        }

        /**
         * 查询学生进度
         * 返回学生的学习进度统计，包括今日、本周和累计数据
         * 
         * @param studentId 学生ID
         * @param startDate 开始日期（可选，用于查询特定时间范围）
         * @param endDate   结束日期（可选，用于查询特定时间范围）
         * @return 学生进度DTO
         */
        @Transactional(readOnly = true)
        @Cacheable(value = CacheConfig.STUDENT_PROGRESS_CACHE, key = "'student:' + #studentId + ':' + #startDate + ':' + #endDate")
        public StudentProgressDTO getStudentProgress(Long studentId, LocalDate startDate, LocalDate endDate) {
                log.info("查询学生进度 - 学生ID: {}, 开始日期: {}, 结束日期: {}", studentId, startDate, endDate);

                LocalDate today = LocalDate.now();
                LocalDate weekStart = today.minusDays(6); // 最近7天

                // 查询今日进度
                Optional<DailyProgress> todayProgress = dailyProgressRepository
                                .findByStudentIdAndStudyDate(studentId, today);

                Integer todayNewWords = todayProgress.map(DailyProgress::getNewWordsCount).orElse(0);
                Integer todayReviewWords = todayProgress.map(DailyProgress::getReviewWordsCount).orElse(0);

                // 查询本周进度
                List<DailyProgress> weekProgress = dailyProgressRepository
                                .findByStudentIdAndStudyDateBetweenOrderByStudyDateAsc(studentId, weekStart, today);

                Integer weekNewWords = weekProgress.stream()
                                .mapToInt(DailyProgress::getNewWordsCount)
                                .sum();

                // 查询累计已学单词数（通过学习记录表统计）
                Long totalLearnedWords = studyRecordRepository.countByStudentId(studentId);

                // 查询指定日期范围的每日进度详情
                List<DailyProgress> progressList;
                if (startDate != null && endDate != null) {
                        progressList = dailyProgressRepository
                                        .findByStudentIdAndStudyDateBetweenOrderByStudyDateAsc(studentId, startDate,
                                                        endDate);
                } else {
                        // 默认返回最近30天的数据
                        LocalDate defaultStartDate = today.minusDays(29);
                        progressList = dailyProgressRepository
                                        .findByStudentIdAndStudyDateBetweenOrderByStudyDateAsc(studentId,
                                                        defaultStartDate, today);
                }

                // 转换为DTO
                List<StudentProgressDTO.DailyProgressDetail> dailyProgressDetails = progressList.stream()
                                .map(progress -> StudentProgressDTO.DailyProgressDetail.builder()
                                                .date(progress.getStudyDate())
                                                .newWords(progress.getNewWordsCount())
                                                .reviewWords(progress.getReviewWordsCount())
                                                .build())
                                .collect(Collectors.toList());

                StudentProgressDTO result = StudentProgressDTO.builder()
                                .todayNewWords(todayNewWords)
                                .todayReviewWords(todayReviewWords)
                                .weekNewWords(weekNewWords)
                                .totalLearnedWords(totalLearnedWords.intValue())
                                .dailyProgress(dailyProgressDetails)
                                .build();

                log.info("学生进度查询成功 - 今日新学: {}, 本周新学: {}, 累计: {}",
                                todayNewWords, weekNewWords, totalLearnedWords);

                return result;
        }

        /**
         * 查询学生进度（不指定日期范围，返回默认最近30天）
         * 
         * @param studentId 学生ID
         * @return 学生进度DTO
         */
        @Transactional(readOnly = true)
        public StudentProgressDTO getStudentProgress(Long studentId) {
                return getStudentProgress(studentId, null, null);
        }

        /**
         * 查询班级学习进度统计
         * 返回班级整体学习统计和排名
         * 
         * @param classId   班级ID（可选，如果为null则查询所有班级）
         * @param startDate 开始日期（可选）
         * @param endDate   结束日期（可选）
         * @return 班级进度DTO
         */
        @Transactional(readOnly = true)
        @Cacheable(value = CacheConfig.CLASS_PROGRESS_CACHE, key = "'class:' + #classId + ':' + #startDate + ':' + #endDate")
        public ClassProgressDTO getClassProgress(Long classId, LocalDate startDate, LocalDate endDate) {
                log.info("查询班级学习进度 - 班级ID: {}, 开始日期: {}, 结束日期: {}", classId, startDate, endDate);

                // 设置默认日期范围（最近30天）
                LocalDate today = LocalDate.now();
                LocalDate effectiveStartDate = startDate != null ? startDate : today.minusDays(29);
                LocalDate effectiveEndDate = endDate != null ? endDate : today;

                // 查询班级的所有学生
                List<User> students;
                if (classId != null) {
                        students = userRepository.findByClassIdAndRole(classId, User.Role.STUDENT);
                } else {
                        students = userRepository.findByRole(User.Role.STUDENT);
                }

                Integer totalStudents = students.size();
                log.debug("班级学生总数: {}", totalStudents);

                if (totalStudents == 0) {
                        // 如果班级没有学生，返回空统计
                        return ClassProgressDTO.builder()
                                        .classId(classId)
                                        .totalStudents(0)
                                        .activeStudents(0)
                                        .averageDailyWords(0.0)
                                        .topStudents(Collections.emptyList())
                                        .build();
                }

                // 获取所有学生ID
                List<Long> studentIds = students.stream()
                                .map(User::getId)
                                .collect(Collectors.toList());

                // 查询指定日期范围内的每日进度
                List<DailyProgress> progressList = dailyProgressRepository
                                .findByStudentIdInAndStudyDateBetween(studentIds, effectiveStartDate, effectiveEndDate);

                // 统计活跃学生数（在时间范围内有学习记录的学生）
                Set<Long> activeStudentIds = progressList.stream()
                                .map(DailyProgress::getStudentId)
                                .collect(Collectors.toSet());
                Integer activeStudents = activeStudentIds.size();

                // 计算平均每日学习单词数
                int totalNewWords = progressList.stream()
                                .mapToInt(DailyProgress::getNewWordsCount)
                                .sum();

                long daysBetween = effectiveEndDate.toEpochDay() - effectiveStartDate.toEpochDay() + 1;
                double averageDailyWords = activeStudents > 0
                                ? (double) totalNewWords / (activeStudents * daysBetween)
                                : 0.0;

                // 查询所有学生的学习记录，统计每个学生的总学习单词数
                List<StudyRecord> studyRecords = studyRecordRepository.findByStudentIdIn(studentIds);

                // 按学生ID分组统计
                Map<Long, Long> studentWordCounts = studyRecords.stream()
                                .collect(Collectors.groupingBy(
                                                StudyRecord::getStudentId,
                                                Collectors.counting()));

                // 创建学生姓名映射
                Map<Long, String> studentNames = students.stream()
                                .collect(Collectors.toMap(
                                                User::getId,
                                                user -> user.getRealName() != null ? user.getRealName()
                                                                : user.getUsername()));

                // 获取Top 5学生
                List<ClassProgressDTO.TopStudent> topStudents = studentWordCounts.entrySet().stream()
                                .sorted(Map.Entry.<Long, Long>comparingByValue().reversed())
                                .limit(5)
                                .map(entry -> ClassProgressDTO.TopStudent.builder()
                                                .studentId(entry.getKey())
                                                .studentName(studentNames.get(entry.getKey()))
                                                .totalWords(entry.getValue().intValue())
                                                .build())
                                .collect(Collectors.toList());

                ClassProgressDTO result = ClassProgressDTO.builder()
                                .classId(classId)
                                .totalStudents(totalStudents)
                                .activeStudents(activeStudents)
                                .averageDailyWords(Math.round(averageDailyWords * 100.0) / 100.0) // 保留两位小数
                                .topStudents(topStudents)
                                .build();

                log.info("班级进度查询成功 - 总学生: {}, 活跃学生: {}, 平均每日: {}",
                                totalStudents, activeStudents, result.getAverageDailyWords());

                return result;
        }
}
