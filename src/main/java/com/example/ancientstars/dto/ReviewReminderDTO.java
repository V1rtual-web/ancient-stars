package com.example.ancientstars.dto;

import com.example.ancientstars.entity.ReviewReminder;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

/**
 * 复习提醒响应DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReviewReminderDTO {

    private Long id;
    private VocabularyDTO vocabulary;
    private LocalDateTime remindTime;
    private Long daysSinceLastStudy;

    /**
     * 从实体转换为DTO
     * 
     * @param entity     复习提醒实体
     * @param vocabulary 词汇信息
     * @param studyTime  学习时间
     * @return DTO对象
     */
    public static ReviewReminderDTO fromEntity(ReviewReminder entity, VocabularyDTO vocabulary,
            LocalDateTime studyTime) {
        if (entity == null) {
            return null;
        }

        ReviewReminderDTO dto = new ReviewReminderDTO();
        dto.setId(entity.getId());
        dto.setVocabulary(vocabulary);
        dto.setRemindTime(entity.getRemindTime());

        // 计算距离上次学习的天数
        if (studyTime != null) {
            dto.setDaysSinceLastStudy(ChronoUnit.DAYS.between(studyTime, LocalDateTime.now()));
        }

        return dto;
    }
}
