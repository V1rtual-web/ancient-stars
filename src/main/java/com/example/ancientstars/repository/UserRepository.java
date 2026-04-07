package com.example.ancientstars.repository;

import com.example.ancientstars.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * 用户数据访问层
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * 根据用户名查找用户
     * 
     * @param username 用户名
     * @return 用户对象
     */
    Optional<User> findByUsername(String username);

    /**
     * 检查用户名是否存在
     * 
     * @param username 用户名
     * @return 是否存在
     */
    boolean existsByUsername(String username);

    /**
     * 根据邮箱查找用户
     * 
     * @param email 邮箱
     * @return 用户对象
     */
    Optional<User> findByEmail(String email);

    /**
     * 根据角色和班级ID查找用户列表
     * 
     * @param role    角色
     * @param classId 班级ID
     * @return 用户列表
     */
    java.util.List<User> findByRoleAndClassId(User.Role role, Long classId);

    /**
     * 根据角色查找用户列表
     * 
     * @param role 角色
     * @return 用户列表
     */
    java.util.List<User> findByRole(User.Role role);

    /**
     * 根据班级ID和角色查找用户列表
     * 
     * @param classId 班级ID
     * @param role    角色
     * @return 用户列表
     */
    java.util.List<User> findByClassIdAndRole(Long classId, User.Role role);
}
