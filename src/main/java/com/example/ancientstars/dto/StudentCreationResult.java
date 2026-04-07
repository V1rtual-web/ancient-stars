package com.example.ancientstars.dto;

import com.example.ancientstars.entity.User;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 学生创建结果（包含初始密码）
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class StudentCreationResult {

    private User student;
    private String initialPassword;
}
