package com.example.ancientstars.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * 词汇表-词汇关联实体类
 */
@Entity
@Table(name = "word_list_vocabulary", uniqueConstraints = @UniqueConstraint(columnNames = { "word_list_id",
        "vocabulary_id" }), indexes = {
                @Index(name = "idx_word_list_id", columnList = "word_list_id"),
                @Index(name = "idx_vocabulary_id", columnList = "vocabulary_id")
        })
@Data
@NoArgsConstructor
@AllArgsConstructor
public class WordListVocabulary {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "word_list_id", nullable = false)
    private Long wordListId;

    @Column(name = "vocabulary_id", nullable = false)
    private Long vocabularyId;

    @Column(name = "sort_order")
    private Integer sortOrder;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
