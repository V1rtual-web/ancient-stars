package com.example.ancientstars.service;

import com.example.ancientstars.dto.ReviewReminderDTO;
import com.example.ancientstars.dto.VocabularyDTO;
import com.example.ancientstars.entity.ReviewReminder;
import com.example.ancientstars.entity.StudyRecord;
import com.example.ancientstars.entity.Vocabulary;
import com.example.ancientstars.repository.ReviewReminderRepository;
import com.example.ancientstars.repository.StudyRecordRepository;
import com.example.ancientstars.repository.VocabularyRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 复习提醒服务
 * 负责管理学生自主背单词的复习提醒，包括创建提醒、查询待复习列表和完成复习
 * 
 * <p>
 * 需求映射：
 * </p>
 * <ul>
 * <li>需求 3.4: 向学生发送复习提醒通知</li>
 * <li>需求 3.5: 在学生端界面展示待复习词汇列表</li>
 * <li>需求 3.6: 完成复习后更新学习记录并重新计算下一次复习时间</li>
 * </ul>
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ReviewReminderService {

        private final ReviewReminderRepository reviewReminderRepository;
        private final StudyRecordRepository studyRecordRepository;
        private final StudyRecordService studyRecordService;
        private final VocabularyRepository vocabularyRepository;

        /**
         * 创建复习提醒
         * 用于复习调度器生成每日复习提醒
         * 
         * @param studentId     学生ID
         * @param vocabularyId  词汇ID
         * @param studyRecordId 学习记录ID
         * @param remindTime    提醒时间
         * @return 创建的复习提醒
         */
        @Transactional
        public ReviewReminder createReminder(Long studentId, Long vocabularyId,
                        Long studyRecordId, LocalDateTime remindTime) {
                log.info("创建复习提醒 - 学生ID: {}, 词汇ID: {}, 学习记录ID: {}, 提醒时间: {}",
                                studentId, vocabularyId, studyRecordId, remindTime);

                ReviewReminder reminder = new ReviewReminder();
                reminder.setStudentId(studentId);
                reminder.setVocabularyId(vocabularyId);
                reminder.setStudyRecordId(studyRecordId);
                reminder.setRemindTime(remindTime);
                reminder.setStatus(ReviewReminder.Status.PENDING);

                ReviewReminder savedReminder = reviewReminderRepository.save(reminder);
                log.info("复习提醒创建成功 - 提醒ID: {}", savedReminder.getId());

                return savedReminder;
        }

        /**
         * 批量创建复习提醒
         * 用于复习调度器批量生成每日复习提醒，提高性能
         * 
         * @param reminders 复习提醒列表
         * @return 创建的复习提醒列表
         */
        @Transactional
        public List<ReviewReminder> createRemindersInBatch(List<ReviewReminder> reminders) {
                log.info("批量创建复习提醒 - 数量: {}", reminders.size());

                if (reminders.isEmpty()) {
                        return new ArrayList<>();
                }

                // 使用 saveAll 进行批量插入
                List<ReviewReminder> savedReminders = reviewReminderRepository.saveAll(reminders);
                log.info("批量创建复习提醒成功 - 数量: {}", savedReminders.size());

                return savedReminders;
        }

        /**
         * 获取学生的待复习词汇列表
         * 查询状态为 PENDING 的复习提醒
         * 
         * @param studentId 学生ID
         * @return 待复习的复习提醒列表
         */
        @Transactional(readOnly = true)
        public List<ReviewReminder> getStudentReminders(Long studentId) {
                log.info("查询学生待复习列表 - 学生ID: {}", studentId);

                List<ReviewReminder> reminders = reviewReminderRepository
                                .findByStudentIdAndStatus(studentId, ReviewReminder.Status.PENDING);

                log.info("查询完成 - 学生ID: {}, 待复习数量: {}", studentId, reminders.size());

                return reminders;
        }

        /**
         * 完成复习
         * 标记复习提醒为已完成，并更新对应的学习记录
         * 更新学习记录会触发复习次数增加和下次复习时间的重新计算
         * 
         * @param reminderId 复习提醒ID
         * @return 更新后的学习记录
         * @throws IllegalArgumentException 如果复习提醒不存在或学习记录不存在
         */
        @Transactional
        public StudyRecord completeReview(Long reminderId) {
                log.info("完成复习 - 提醒ID: {}", reminderId);

                // 查找复习提醒
                ReviewReminder reminder = reviewReminderRepository.findById(reminderId)
                                .orElseThrow(() -> new IllegalArgumentException("复习提醒不存在: " + reminderId));

                // 标记提醒为已完成
                reminder.setStatus(ReviewReminder.Status.COMPLETED);
                reviewReminderRepository.save(reminder);
                log.info("复习提醒已标记为完成 - 提醒ID: {}", reminderId);

                // 查找并更新学习记录
                StudyRecord studyRecord = studyRecordRepository.findById(reminder.getStudyRecordId())
                                .orElseThrow(() -> new IllegalArgumentException(
                                                "学习记录不存在: " + reminder.getStudyRecordId()));

                // 更新学习记录（会自动增加复习次数并重新计算下次复习时间）
                StudyRecord updatedRecord = studyRecordService.updateStudyRecord(studyRecord);
                log.info("学习记录已更新 - 记录ID: {}, 新的复习次数: {}, 下次复习时间: {}",
                                updatedRecord.getId(), updatedRecord.getReviewCount(),
                                updatedRecord.getNextReviewTime());

                return updatedRecord;
        }

        /**
         * 获取学生的待复习词汇列表（包含词汇详情）
         * 查询状态为 PENDING 的复习提醒，并关联词汇信息
         * 
         * @param studentId 学生ID
         * @return 待复习的复习提醒DTO列表
         */
        @Transactional(readOnly = true)
        public List<ReviewReminderDTO> getStudentRemindersWithDetails(Long studentId) {
                log.info("查询学生待复习列表（含详情） - 学生ID: {}", studentId);

                List<ReviewReminder> reminders = reviewReminderRepository
                                .findByStudentIdAndStatus(studentId, ReviewReminder.Status.PENDING);

                if (reminders.isEmpty()) {
                        log.info("学生 {} 暂无待复习词汇", studentId);
                        return new ArrayList<>();
                }

                // 获取所有词汇ID
                List<Long> vocabularyIds = reminders.stream()
                                .map(ReviewReminder::getVocabularyId)
                                .distinct()
                                .collect(Collectors.toList());

                // 批量查询词汇信息
                Map<Long, Vocabulary> vocabularyMap = vocabularyRepository.findAllById(vocabularyIds)
                                .stream()
                                .collect(Collectors.toMap(Vocabulary::getId, v -> v));

                // 获取所有学习记录ID
                List<Long> studyRecordIds = reminders.stream()
                                .map(ReviewReminder::getStudyRecordId)
                                .distinct()
                                .collect(Collectors.toList());

                // 批量查询学习记录
                Map<Long, StudyRecord> studyRecordMap = studyRecordRepository.findAllById(studyRecordIds)
                                .stream()
                                .collect(Collectors.toMap(StudyRecord::getId, sr -> sr));

                // 转换为DTO
                List<ReviewReminderDTO> dtoList = reminders.stream()
                                .map(reminder -> {
                                        Vocabulary vocabulary = vocabularyMap.get(reminder.getVocabularyId());
                                        StudyRecord studyRecord = studyRecordMap.get(reminder.getStudyRecordId());

                                        VocabularyDTO vocabularyDTO = convertVocabularyToDTO(vocabulary);
                                        LocalDateTime studyTime = studyRecord != null ? studyRecord.getStudyTime()
                                                        : null;

                                        return ReviewReminderDTO.fromEntity(reminder, vocabularyDTO, studyTime);
                                })
                                .collect(Collectors.toList());

                log.info("查询完成 - 学生ID: {}, 待复习数量: {}", studentId, dtoList.size());

                return dtoList;
        }

        /**
         * 将Vocabulary实体转换为DTO
         */
        private VocabularyDTO convertVocabularyToDTO(Vocabulary vocabulary) {
                if (vocabulary == null) {
                        return null;
                }

                VocabularyDTO dto = new VocabularyDTO();
                BeanUtils.copyProperties(vocabulary, dto);
                return dto;
        }
}
