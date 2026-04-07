package com.example.ancientstars.dto;

import com.example.ancientstars.entity.StudyRecord;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 学习记录响应DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class StudyRecordDTO {

    private Long id;
    private Long studentId;
    private Long vocabularyId;
    private LocalDateTime studyTime;
    private Integer reviewCount;
    private LocalDateTime lastReviewTime;
    private LocalDateTime nextReviewTime;
    private StudyRecord.MasteryLevel masteryLevel;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /**
     * 从实体转换为DTO
     */
    public static StudyRecordDTO fromEntity(StudyRecord entity) {
        if (entity == null) {
            return null;
        }

        StudyRecordDTO dto = new StudyRecordDTO();
        dto.setId(entity.getId());
        dto.setStudentId(entity.getStudentId());
        dto.setVocabularyId(entity.getVocabularyId());
        dto.setStudyTime(entity.getStudyTime());
        dto.setReviewCount(entity.getReviewCount());
        dto.setLastReviewTime(entity.getLastReviewTime());
        dto.setNextReviewTime(entity.getNextReviewTime());
        dto.setMasteryLevel(entity.getMasteryLevel());
        dto.setCreatedAt(entity.getCreatedAt());
        dto.setUpdatedAt(entity.getUpdatedAt());

        return dto;
    }
}
