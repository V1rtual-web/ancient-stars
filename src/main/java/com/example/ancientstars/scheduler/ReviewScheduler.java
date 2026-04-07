package com.example.ancientstars.scheduler;

import com.example.ancientstars.entity.ReviewReminder;
import com.example.ancientstars.entity.StudyRecord;
import com.example.ancientstars.repository.StudyRecordRepository;
import com.example.ancientstars.service.ReviewReminderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 复习调度器
 * 负责定时生成学生自主背单词的复习提醒
 * 
 * <p>
 * 需求映射：
 * </p>
 * <ul>
 * <li>需求 3.3: 每天定时检查需要复习的词汇</li>
 * <li>需求 6.1: 每天在固定时间（早上8点）执行一次调度任务</li>
 * <li>需求 6.2: 查询所有需要在当天复习的学习记录</li>
 * <li>需求 6.3: 为每个需要复习的学生生成复习提醒</li>
 * <li>需求 6.4: 执行失败时记录错误日志并在下次执行时重试</li>
 * <li>需求 6.5: 在高并发情况下保证性能（处理10000条记录应在60秒内完成）</li>
 * </ul>
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ReviewScheduler {

    private final StudyRecordRepository studyRecordRepository;
    private final ReviewReminderService reviewReminderService;

    /**
     * 每天早上8点执行复习提醒生成任务
     * 使用 cron 表达式: "0 0 8 * * ?" 表示每天8:00:00执行
     * 
     * <p>
     * 执行流程：
     * </p>
     * <ol>
     * <li>查询所有需要在今天复习的学习记录</li>
     * <li>为每条记录生成复习提醒</li>
     * <li>记录执行结果和统计信息</li>
     * <li>如果执行失败，记录错误日志，下次执行时会重新处理</li>
     * </ol>
     */
    @Scheduled(cron = "0 0 8 * * ?")
    @Transactional
    public void generateDailyReviewReminders() {
        log.info("========== 开始生成每日复习提醒 ==========");
        long startTime = System.currentTimeMillis();

        try {
            LocalDateTime now = LocalDateTime.now();
            log.info("当前时间: {}", now);

            // 查询所有需要在今天复习的学习记录
            List<StudyRecord> recordsToReview = studyRecordRepository.findRecordsNeedReview(now);
            log.info("查询到需要复习的学习记录数量: {}", recordsToReview.size());

            if (recordsToReview.isEmpty()) {
                log.info("没有需要复习的记录，任务结束");
                return;
            }

            // 批量生成复习提醒
            int successCount = 0;
            int failureCount = 0;
            List<ReviewReminder> reminders = new ArrayList<>();

            for (StudyRecord record : recordsToReview) {
                try {
                    ReviewReminder reminder = reviewReminderService.createReminder(
                            record.getStudentId(),
                            record.getVocabularyId(),
                            record.getId(),
                            now);
                    reminders.add(reminder);
                    successCount++;
                } catch (Exception e) {
                    failureCount++;
                    log.error("创建复习提醒失败 - 学习记录ID: {}, 学生ID: {}, 词汇ID: {}, 错误: {}",
                            record.getId(), record.getStudentId(), record.getVocabularyId(),
                            e.getMessage(), e);
                }
            }

            long endTime = System.currentTimeMillis();
            long duration = endTime - startTime;

            log.info("========== 复习提醒生成完成 ==========");
            log.info("总记录数: {}", recordsToReview.size());
            log.info("成功生成: {}", successCount);
            log.info("失败数量: {}", failureCount);
            log.info("执行耗时: {} ms ({} 秒)", duration, duration / 1000.0);

            // 性能警告：如果处理10000条记录超过60秒，记录警告日志
            if (recordsToReview.size() >= 10000 && duration > 60000) {
                log.warn("性能警告: 处理 {} 条记录耗时 {} 秒，超过60秒性能要求",
                        recordsToReview.size(), duration / 1000.0);
            }

        } catch (Exception e) {
            log.error("复习提醒生成任务执行失败", e);
            // 不抛出异常，确保下次调度能够继续执行
        }
    }
}
