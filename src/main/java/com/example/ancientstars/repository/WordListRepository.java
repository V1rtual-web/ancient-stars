package com.example.ancientstars.repository;

import com.example.ancientstars.entity.WordList;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 词汇表数据访问层
 */
@Repository
public interface WordListRepository extends JpaRepository<WordList, Long> {

    /**
     * 根据创建者ID查询词汇表
     */
    List<WordList> findByCreatorId(Long creatorId);

    /**
     * 根据创建者ID分页查询
     */
    Page<WordList> findByCreatorId(Long creatorId, Pageable pageable);

    /**
     * 根据名称模糊查询
     */
    Page<WordList> findByNameContaining(String name, Pageable pageable);

    /**
     * 检查名称是否存在
     */
    boolean existsByNameAndCreatorId(String name, Long creatorId);
}
