package com.example.ancientstars.scheduler;

import com.example.ancientstars.entity.ReviewReminder;
import com.example.ancientstars.entity.StudyRecord;
import com.example.ancientstars.repository.ReviewReminderRepository;
import com.example.ancientstars.repository.StudyRecordRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * ReviewScheduler 集成测试
 * 验证调度器在真实环境中的行为
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
@DisplayName("复习调度器集成测试")
class ReviewSchedulerIntegrationTest {

    @Autowired
    private ReviewScheduler reviewScheduler;

    @Autowired
    private StudyRecordRepository studyRecordRepository;

    @Autowired
    private ReviewReminderRepository reviewReminderRepository;

    @Test
    @DisplayName("集成测试 - 验证调度器能够正常执行")
    void shouldExecuteSchedulerSuccessfully() {
        // Given: 数据库中有需要复习的记录（由于是集成测试，使用真实数据库）
        // When: 执行调度任务
        reviewScheduler.generateDailyReviewReminders();

        // Then: 调度器应该成功执行（不抛出异常）
        // 注意：这个测试主要验证调度器能够在真实环境中运行
        // 具体的业务逻辑已经在单元测试中验证
    }
}
