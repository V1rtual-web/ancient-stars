package com.example.ancientstars.service;

import com.example.ancientstars.dto.*;
import com.example.ancientstars.entity.*;
import com.example.ancientstars.exception.BusinessException;
import com.example.ancientstars.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 统计分析服务类
 */
@Service
@RequiredArgsConstructor
public class StatisticsService {

    private final UserRepository userRepository;
    private final LearningRecordRepository learningRecordRepository;
    private final TestRecordRepository testRecordRepository;
    private final TaskAssignmentRepository taskAssignmentRepository;

    /**
     * 获取学生个人统计
     */
    public StudentStatisticsDTO getStudentStatistics(Long studentId, LocalDate startDate, LocalDate endDate) {
        // 验证学生是否存在
        User student = userRepository.findById(studentId)
                .orElseThrow(() -> new BusinessException("学生不存在"));

        StudentStatisticsDTO statistics = new StudentStatisticsDTO();
        statistics.setStudentId(studentId);
        statistics.setStudentName(student.getRealName());

        // 转换日期为LocalDateTime
        LocalDateTime startDateTime = startDate != null ? startDate.atStartOfDay() : null;
        LocalDateTime endDateTime = endDate != null ? endDate.atTime(23, 59, 59) : null;

        // 1. 计算学习时长（天数）
        List<LearningRecord> learningRecords;
        if (startDateTime != null && endDateTime != null) {
            learningRecords = learningRecordRepository.findByStudentIdAndCreatedAtBetween(
                    studentId, startDateTime, endDateTime);
        } else {
            learningRecords = learningRecordRepository.findByStudentId(studentId);
        }

        if (!learningRecords.isEmpty()) {
            LocalDateTime firstRecord = learningRecords.stream()
                    .map(LearningRecord::getCreatedAt)
                    .min(LocalDateTime::compareTo)
                    .orElse(LocalDateTime.now());
            
            LocalDateTime lastRecord = learningRecords.stream()
                    .map(LearningRecord::getCreatedAt)
                    .max(LocalDateTime::compareTo)
                    .orElse(LocalDateTime.now());
            
            long days = ChronoUnit.DAYS.between(firstRecord, lastRecord) + 1;
            statistics.setLearningDays(days);
        } else {
            statistics.setLearningDays(0L);
        }

        // 2. 计算掌握词汇数
        Long masteredCount;
        if (startDateTime != null && endDateTime != null) {
            masteredCount = learningRecordRepository.countByStudentIdAndMasteryLevelAndUpdatedAtBetween(
                    studentId, LearningRecord.MasteryLevel.MASTERED, startDateTime, endDateTime);
        } else {
            masteredCount = learningRecordRepository.countByStudentIdAndMasteryLevel(
                    studentId, LearningRecord.MasteryLevel.MASTERED);
        }
        statistics.setMasteredVocabularyCount(masteredCount);

        // 3. 获取测试统计
        List<TestRecord> testRecords;
        if (startDateTime != null && endDateTime != null) {
            testRecords = testRecordRepository.findByStudentIdAndCreatedAtBetweenOrderByCreatedAtDesc(
                    studentId, startDateTime, endDateTime);
        } else {
            testRecords = testRecordRepository.findByStudentIdOrderByCreatedAtDesc(studentId);
        }

        statistics.setTestCount((long) testRecords.size());

        // 4. 计算平均分
        if (!testRecords.isEmpty())

    {
        BigDecimal totalScore = testRecords.stream()
                .map(TestRecord::getScore)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal avgScore = totalScore.divide(
                BigDecimal.valueOf(testRecords.size()), 2, RoundingMode.HALF_UP);
        statistics.setAverageScore(avgScore);
    }else
    {
        statistics.setAverageScore(BigDecimal.ZERO);
    }

    // 5. 获取成绩趋势（最近5次测试）
    List<BigDecimal> scoresTrend = testRecords.stream()
            .limit(5)
            .map(TestRecord::getScore)
            .collect(Collectors.toList());statistics.setScoresTrend(scoresTrend);

    // 6. 学习记录总数
    statistics.setTotalLearningRecords((long)learningRecords.size());

    return statistics;
    }

    /**
     * 获取班级统计
     */
    public ClassStatisticsDTO getClassStatistics(Long classId, LocalDate startDate, LocalDate endDate) {
        ClassStatisticsDTO statistics = new ClassStatisticsDTO();
        statistics.setClassId(classId);
        statistics.setClassName("班级" + classId); // 简化处理，实际应该从数据库获取

        // 转换日期为LocalDateTime
        LocalDateTime startDateTime = startDate != null ? startDate.atStartOfDay() : null;
        LocalDateTime endDateTime = endDate != null ? endDate.atTime(23, 59, 59) : null;

        // 1. 获取班级所有学生
        List<User> students = userRepository.findByClassIdAndRole(classId, User.Role.STUDENT);
        statistics.setTotalStudents((long) students.size());

        if (students.isEmpty()) {
            statistics.setAverageProgress(BigDecimal.ZERO);
            statistics.setAverageScore(BigDecimal.ZERO);
            statistics.setScoreDistribution(new HashMap<>());
            statistics.setTotalTests(0L);
            return statistics;
        }

        List<Long> studentIds = students.stream()
                .map(User::getId)
                .collect(Collectors.toList());

        // 2. 计算平均进度
        List<TaskAssignment> assignments = taskAssignmentRepository.findByStudentIdIn(studentIds);
        if (!assignments.isEmpty()) {
            double avgProgress = assignments.stream()
                    .mapToInt(TaskAssignment::getProgress)
                    .average()
                    .orElse(0.0);
            statistics.setAverageProgress(BigDecimal.valueOf(avgProgress).setScale(2, RoundingMode.HALF_UP));
        } else {
            statistics.setAverageProgress(BigDecimal.ZERO);
        }

        // 3. 获取所有测试记录
        List<TestRecord> allTestRecords = new ArrayList<>();
        for (Long studentId : studentIds) {
            List<TestRecord> testRecords;
            if (startDateTime != null && endDateTime != null) {
                testRecords = testRecordRepository.findByStudentIdAndCreatedAtBetweenOrderByCreatedAtDesc(
                        studentId, startDateTime, endDateTime);
            } else {
                testRecords = testRecordRepository.findByStudentIdOrderByCreatedAtDesc(studentId);
            }
            allTestRecords.addAll(testRecords);
        }

        statistics.setTotalTests((long) allTestRecords.size());

        // 4. 计算平均分
        if (!allTestRecords.isEmpty()) {
            BigDecimal totalScore = allTestRecords.stream()
                    .map(TestRecord::getScore)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            
            BigDecimal avgScore = totalScore.divide(
                    BigDecimal.valueOf(allTestRecords.size()), 2, RoundingMode.HALF_UP);
            statistics.setAverageScore(avgScore);
        } else {
            statistics.setAverageScore(BigDecimal.ZERO);
        }

        // 5. 计算成绩分布
        Map<String, Long> scoreDistribution = new LinkedHashMap<>();
        scoreDistribution.put("0-59", 0L);
        scoreDistribution.put("60-69", 0L);
        scoreDistribution.put("70-79", 0L);
        scoreDistribution.put("80-89", 0L);
        scoreDistribution.put("90-100", 0L);

        for (TestRecord record : allTestRecords) {
            BigDecimal score = record.getScore();
            if (score.compareTo(BigDecimal.valueOf(60)) < 0) {
                scoreDistribution.put("0-59", scoreDistribution.get("0-59") + 1);
            } else if (score.compareTo(BigDecimal.valueOf(70)) < 0) {
                scoreDistribution.put("60-69", scoreDistribution.get("60-69") + 1);
            } else if (score.compareTo(BigDecimal.valueOf(80)) < 0) {
                scoreDistribution.put("70-79", scoreDistribution.get("70-79") + 1);
            } else if (score.compareTo(BigDecimal.valueOf(90)) < 0) {
                scoreDistribution.put("80-89", scoreDistribution.get("80-89") + 1);
            } else {
                scoreDistribution.put("90-100", scoreDistribution.get("90-100") + 1);
            }
        }

        statistics.setScoreDistribution(scoreDistribution);

        return statistics;
    }

    /**
     * 生成学习报告
     */
    public LearningReportDTO generateLearningReport(Long studentId, LocalDate startDate, LocalDate endDate) {
        // 验证学生是否存在
        User student = userRepository.findById(studentId)
                .orElseThrow(() -> new BusinessException("学生不存在"));

        LearningReportDTO report = new LearningReportDTO();
        report.setStudentId(studentId);
        report.setStudentName(student.getRealName());
        report.setGeneratedAt(LocalDateTime.now());

        // 获取学生统计信息
        StudentStatisticsDTO statistics = getStudentStatistics(studentId, startDate, endDate);
        report.setStatistics(statistics);

        // 生成学习建议
        List<String> suggestions = generateSuggestions(statistics);
        report.setSuggestions(suggestions);

        // 获取最近的学习活动
        List<LearningReportDTO.RecentActivityDTO> recentActivities = getRecentActivities(studentId, 10);
        report.setRecentActivities(recentActivities);

        return report;
    }

    /**
     * 生成学习建议
     */
    private List<String> generateSuggestions(StudentStatisticsDTO statistics) {
        List<String> suggestions = new ArrayList<>();

        // 根据平均分给出建议
        if (statistics.getAverageScore().compareTo(BigDecimal.valueOf(60)) < 0) {
            suggestions.add("您的平均分较低，建议加强基础词汇的学习和记忆");
        } else if (statistics.getAverageScore().compareTo(BigDecimal.valueOf(80)) < 0) {
            suggestions.add("您的成绩还有提升空间，建议多做练习巩固已学词汇");
        } else {
            suggestions.add("您的成绩优秀，继续保持！可以尝试学习更多高级词汇");
        }

        // 根据掌握词汇数给出建议
        if (statistics.getMasteredVocabularyCount() < 50) {
            suggestions.add("建议每天坚持学习，逐步增加词汇量");
        } else if (statistics.getMasteredVocabularyCount() < 200) {
            suggestions.add("词汇量正在稳步增长，继续保持学习节奏");
        } else {
            suggestions.add("您已经掌握了大量词汇，可以尝试阅读英文文章来巩固");
        }

        // 根据成绩趋势给出建议
        if (statistics.getScoresTrend().size() >= 2) {
            List<BigDecimal> trend = statistics.getScoresTrend();
            BigDecimal recent = trend.get(0);
            BigDecimal previous = trend.get(1);
            
            if (recent.compareTo(previous) > 0) {
                suggestions.add("您的成绩呈上升趋势，学习方法很有效！");
            } else if (recent.compareTo(previous) < 0) {
                suggestions.add("最近成绩有所下降，建议复习之前学过的内容");
            }
        }

        return suggestions;
    }

    /**
     * 获取最近的学习活动
     */
    private List<LearningReportDTO.RecentActivityDTO> getRecentActivities(Long studentId, int limit) {
        List<LearningReportDTO.RecentActivityDTO> activities = new ArrayList<>();

        // 获取最近的学习记录
        List<LearningRecord> learningRecords = learningRecordRepository
                .findTop5ByStudentIdOrderByUpdatedAtDesc(studentId);
        
        for (LearningRecord record : learningRecords) {
            LearningReportDTO.RecentActivityDTO activity = new LearningReportDTO.RecentActivityDTO();
            activity.setActivityType("LEARNING");
            activity.setDescription("学习了词汇，掌握度：" + record.getMasteryLevel());
            activity.setTimestamp(record.getUpdatedAt());
            activities.add(activity);
        }

        // 获取最近的测试记录
        List<TestRecord> testRecords = testRecordRepository
                .findTop5ByStudentIdOrderByCreatedAtDesc(studentId);
        
        for (TestRecord record : testRecords) {
            LearningReportDTO.RecentActivityDTO activity = new LearningReportDTO.RecentActivityDTO();
            activity.setActivityType("TEST");
            activity.setDescription("完成测试，得分：" + record.getScore());
            activity.setTimestamp(record.getCreatedAt());
            activities.add(activity);
        }

        // 按时间排序并限制数量
        return activities.stream()
                .sorted(Comparator.comparing(LearningReportDTO.RecentActivityDTO::getTimestamp).reversed())
                .limit(limit)
                .collect(Collectors.toList());
    }
}
