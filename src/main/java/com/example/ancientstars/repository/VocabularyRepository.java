package com.example.ancientstars.repository;

import com.example.ancientstars.entity.Vocabulary;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 词汇数据访问层
 */
@Repository
public interface VocabularyRepository extends JpaRepository<Vocabulary, Long> {

        /** 获取所有未删除单词（用于批量导入查重） */
        @Query("SELECT v.word FROM Vocabulary v WHERE v.isDeleted = false")
        List<String> findAllWords();

        /** 根据单词查找（不含已删除） */
        Optional<Vocabulary> findByWordAndIsDeletedFalse(String word);

        /** 检查单词是否存在（不含已删除） */
        boolean existsByWordAndIsDeletedFalse(String word);

        /** 分页查询所有未删除词汇 */
        Page<Vocabulary> findByIsDeletedFalse(Pageable pageable);

        /** 查询未删除且ID不在指定集合中的词汇 */
        Page<Vocabulary> findByIsDeletedFalseAndIdNotIn(java.util.Collection<Long> ids, Pageable pageable);

        /** 统计未删除且ID不在指定集合中的词汇数量 */
        long countByIsDeletedFalseAndIdNotIn(java.util.Collection<Long> ids);

        /** 根据难度级别查询（不含已删除） */
        List<Vocabulary> findByDifficultyLevelAndIsDeletedFalse(Integer difficultyLevel);

        /** 模糊搜索词汇（不含已删除） */
        @Query("SELECT v FROM Vocabulary v WHERE v.isDeleted = false AND (" +
                        "v.word LIKE %:keyword% OR " +
                        "v.definition LIKE %:keyword% OR " +
                        "v.translation LIKE %:keyword%)")
        Page<Vocabulary> searchByKeyword(@Param("keyword") String keyword, Pageable pageable);

        /** 查询所有词汇（含已删除，用于管理） */
        @Query("SELECT v FROM Vocabulary v WHERE v.isDeleted = :deleted")
        Page<Vocabulary> findByDeleted(@Param("deleted") Boolean deleted, Pageable pageable);

        /** 根据词汇表ID查询词汇 */
        @Query("SELECT v FROM Vocabulary v WHERE v.isDeleted = false AND v.id IN " +
                        "(SELECT wlv.vocabularyId FROM WordListVocabulary wlv WHERE wlv.wordListId = :wordListId)")
        Page<Vocabulary> findByWordListId(@Param("wordListId") Long wordListId, Pageable pageable);

        /** 根据词汇表ID和关键词查询词汇 */
        @Query("SELECT v FROM Vocabulary v WHERE v.isDeleted = false AND v.id IN " +
                        "(SELECT wlv.vocabularyId FROM WordListVocabulary wlv WHERE wlv.wordListId = :wordListId) " +
                        "AND (v.word LIKE %:keyword% OR v.definition LIKE %:keyword% OR v.translation LIKE %:keyword%)")
        Page<Vocabulary> findByWordListIdAndKeyword(@Param("wordListId") Long wordListId,
                        @Param("keyword") String keyword, Pageable pageable);
}
