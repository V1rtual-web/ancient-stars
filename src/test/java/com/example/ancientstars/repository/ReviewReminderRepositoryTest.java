package com.example.ancientstars.repository;

import com.example.ancientstars.entity.ReviewReminder;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * ReviewReminderRepository 测试类
 * 验证复习提醒数据访问层的功能
 */
@DataJpaTest
@ActiveProfiles("test")
class ReviewReminderRepositoryTest {

    @Autowired
    private ReviewReminderRepository reviewReminderRepository;

    @Test
    void testFindByStudentIdAndStatus_shouldReturnPendingReminders() {
        // 创建测试数据
        Long studentId = 1L;

        ReviewReminder reminder1 = new ReviewReminder();
        reminder1.setStudentId(studentId);
        reminder1.setVocabularyId(100L);
        reminder1.setStudyRecordId(1L);
        reminder1.setRemindTime(LocalDateTime.now().plusDays(1));
        reminder1.setStatus(ReviewReminder.Status.PENDING);

        ReviewReminder reminder2 = new ReviewReminder();
        reminder2.setStudentId(studentId);
        reminder2.setVocabularyId(101L);
        reminder2.setStudyRecordId(2L);
        reminder2.setRemindTime(LocalDateTime.now().plusDays(2));
        reminder2.setStatus(ReviewReminder.Status.PENDING);

        ReviewReminder reminder3 = new ReviewReminder();
        reminder3.setStudentId(studentId);
        reminder3.setVocabularyId(102L);
        reminder3.setStudyRecordId(3L);
        reminder3.setRemindTime(LocalDateTime.now().plusDays(3));
        reminder3.setStatus(ReviewReminder.Status.COMPLETED);

        reviewReminderRepository.save(reminder1);
        reviewReminderRepository.save(reminder2);
        reviewReminderRepository.save(reminder3);

        // 查询待复习的提醒
        List<ReviewReminder> pendingReminders = reviewReminderRepository
                .findByStudentIdAndStatus(studentId, ReviewReminder.Status.PENDING);

        // 验证结果
        assertThat(pendingReminders).hasSize(2);
        assertThat(pendingReminders).allMatch(r -> r.getStatus() == ReviewReminder.Status.PENDING);
        assertThat(pendingReminders).allMatch(r -> r.getStudentId().equals(studentId));
    }

    @Test
    void testFindByStudentIdAndStatus_shouldReturnEmptyListWhenNoMatch() {
        // 创建不同学生的提醒
        ReviewReminder reminder = new ReviewReminder();
        reminder.setStudentId(1L);
        reminder.setVocabularyId(100L);
        reminder.setStudyRecordId(1L);
        reminder.setRemindTime(LocalDateTime.now());
        reminder.setStatus(ReviewReminder.Status.PENDING);
        reviewReminderRepository.save(reminder);

        // 查询不存在的学生
        List<ReviewReminder> result = reviewReminderRepository
                .findByStudentIdAndStatus(999L, ReviewReminder.Status.PENDING);

        // 验证返回空列表
        assertThat(result).isEmpty();
    }

    @Test
    void testBatchInsert_shouldSaveMultipleReminders() {
        // 创建批量提醒数据
        List<ReviewReminder> reminders = new ArrayList<>();
        Long studentId = 1L;

        for (int i = 0; i < 10; i++) {
            ReviewReminder reminder = new ReviewReminder();
            reminder.setStudentId(studentId);
            reminder.setVocabularyId(100L + i);
            reminder.setStudyRecordId((long) (i + 1));
            reminder.setRemindTime(LocalDateTime.now().plusDays(i));
            reminder.setStatus(ReviewReminder.Status.PENDING);
            reminders.add(reminder);
        }

        // 批量保存
        List<ReviewReminder> savedReminders = reviewReminderRepository.saveAll(reminders);

        // 验证批量保存成功
        assertThat(savedReminders).hasSize(10);
        assertThat(savedReminders).allMatch(r -> r.getId() != null);
        assertThat(savedReminders).allMatch(r -> r.getCreatedAt() != null);
        assertThat(savedReminders).allMatch(r -> r.getUpdatedAt() != null);

        // 验证可以查询到所有保存的数据
        List<ReviewReminder> allPending = reviewReminderRepository
                .findByStudentIdAndStatus(studentId, ReviewReminder.Status.PENDING);
        assertThat(allPending).hasSize(10);
    }

    @Test
    void testSaveAndFind_shouldPersistAllFields() {
        // 创建完整的提醒记录
        ReviewReminder reminder = new ReviewReminder();
        reminder.setStudentId(1L);
        reminder.setVocabularyId(100L);
        reminder.setStudyRecordId(1L);
        LocalDateTime remindTime = LocalDateTime.now().plusDays(7);
        reminder.setRemindTime(remindTime);
        reminder.setStatus(ReviewReminder.Status.PENDING);

        // 保存
        ReviewReminder saved = reviewReminderRepository.save(reminder);

        // 验证所有字段
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getStudentId()).isEqualTo(1L);
        assertThat(saved.getVocabularyId()).isEqualTo(100L);
        assertThat(saved.getStudyRecordId()).isEqualTo(1L);
        assertThat(saved.getRemindTime()).isEqualToIgnoringNanos(remindTime);
        assertThat(saved.getStatus()).isEqualTo(ReviewReminder.Status.PENDING);
        assertThat(saved.getCreatedAt()).isNotNull();
        assertThat(saved.getUpdatedAt()).isNotNull();
    }

    @Test
    void testFindByStudentIdAndStatus_shouldFilterByStatus() {
        // 创建不同状态的提醒
        Long studentId = 1L;
        
        ReviewReminder pending = new ReviewReminder();
        pending.setStudentId(studentId);
        pending.setVocabularyId(100L);
        pending.setStudyRecordId(1L);
        pending.setRemindTime(LocalDateTime.now());
        pending.setStatus(ReviewReminder.Status.PENDING);
        
        ReviewReminder completed = new ReviewReminder();
        completed.setStudentId(studentId);
        completed.setVocabularyId(101L);
        completed.setStudyRecordId(2L);
        completed.setRemindTime(LocalDateTime.now());
        completed.setStatus(ReviewReminder.Status.COMPLETED);
        
        ReviewReminder skipped = new ReviewReminder();
        skipped.setStudentId(studentId);
        skipped.setVocabularyId(102L);
        skipped.setStudyRecordId(3L);
        skipped.setRemindTime(LocalDateTime.now());
        skipped.setStatus(ReviewReminder.Status.SKIPPED);
        
        reviewReminderRepository.save(pending);
        reviewReminderRepository.save(completed);
        reviewReminderRepository.save(skipped);

        // 分别查询不同状态
        List<ReviewReminder> pendingList = reviewReminderRepository
                .findByStudentIdAndStatus(studentId, ReviewReminder.Status.PENDING);
        List<ReviewReminder> completedList = reviewReminderRepository
                .findByStudentIdAndStatus(studentId, ReviewReminder.Status.COMPLETED);
        List<ReviewReminder> skippedList = reviewReminderRepository
                .findByStudentIdAndStatus(studentId, ReviewReminder.Status.SKIPPED);

        // 验证状态过滤正确
        assertThat(pendingList).hasSize(1);
        assertThat(completedList).hasSize(1);
        assertThat(skippedList).hasSize(1);
        assertThat(pendingList.get(0).getStatus()).isEqualTo(ReviewReminder.Status.PENDING);
        assertThat(completedList.get(0).getStatus()).isEqualTo(ReviewReminder.Status.COMPLETED);
        assertThat(skippedList.get(0).getStatus()).isEqualTo(ReviewReminder.Status.SKIPPED);
    }
}
