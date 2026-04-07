package com.example.ancientstars.repository;

import com.example.ancientstars.entity.WordListVocabulary;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 词汇表-词汇关联数据访问层
 */
@Repository
public interface WordListVocabularyRepository extends JpaRepository<WordListVocabulary, Long> {

    /**
     * 查询词汇表中的所有词汇ID
     */
    @Query("SELECT wlv.vocabularyId FROM WordListVocabulary wlv WHERE wlv.wordListId = :wordListId ORDER BY wlv.sortOrder")
    List<Long> findVocabularyIdsByWordListId(@Param("wordListId") Long wordListId);

    /**
     * 查询词汇表中的所有关联记录
     */
    List<WordListVocabulary> findByWordListIdOrderBySortOrder(Long wordListId);

    /**
     * 检查词汇是否已在词汇表中
     */
    boolean existsByWordListIdAndVocabularyId(Long wordListId, Long vocabularyId);

    /**
     * 查询特定关联
     */
    Optional<WordListVocabulary> findByWordListIdAndVocabularyId(Long wordListId, Long vocabularyId);

    /**
     * 删除词汇表中的词汇
     */
    @Modifying
    @Query("DELETE FROM WordListVocabulary wlv WHERE wlv.wordListId = :wordListId AND wlv.vocabularyId = :vocabularyId")
    void deleteByWordListIdAndVocabularyId(@Param("wordListId") Long wordListId,
            @Param("vocabularyId") Long vocabularyId);

    /**
     * 删除词汇表的所有词汇
     */
    @Modifying
    @Query("DELETE FROM WordListVocabulary wlv WHERE wlv.wordListId = :wordListId")
    void deleteByWordListId(@Param("wordListId") Long wordListId);

    /**
     * 统计词汇表中的词汇数量
     */
    long countByWordListId(Long wordListId);
}
