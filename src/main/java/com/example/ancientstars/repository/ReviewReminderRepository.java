package com.example.ancientstars.repository;

import com.example.ancientstars.entity.ReviewReminder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 复习提醒数据访问层
 * 用于学生自主背单词功能的复习提醒管理
 * 
 * <p>
 * 批量插入说明：
 * 使用继承自 JpaRepository 的 saveAll(Iterable<S> entities) 方法进行批量插入。
 * Spring Data JPA 会自动优化批量操作，确保在复习调度器生成大量提醒时的性能。
 * 配合 application.properties 中的 spring.jpa.properties.hibernate.jdbc.batch_size
 * 配置使用。
 * </p>
 */
@Repository
public interface ReviewReminderRepository extends JpaRepository<ReviewReminder, Long> {

    /**
     * 查找学生指定状态的复习提醒列表
     * 用于查询学生的待复习词汇列表
     * 
     * @param studentId 学生ID
     * @param status    提醒状态
     * @return 符合条件的复习提醒列表
     */
    List<ReviewReminder> findByStudentIdAndStatus(Long studentId, ReviewReminder.Status status);
}
