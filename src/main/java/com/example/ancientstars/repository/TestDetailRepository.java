package com.example.ancientstars.repository;

import com.example.ancientstars.entity.TestDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 测试详情Repository
 */
@Repository
public interface TestDetailRepository extends JpaRepository<TestDetail, Long> {

    /**
     * 根据测试记录ID查询测试详情
     */
    List<TestDetail> findByTestRecordId(Long testRecordId);

    /**
     * 根据测试记录ID查询错题（答错的题目）
     */
    List<TestDetail> findByTestRecordIdAndIsCorrectFalse(Long testRecordId);
}
