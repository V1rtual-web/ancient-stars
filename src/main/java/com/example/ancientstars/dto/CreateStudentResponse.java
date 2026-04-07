package com.example.ancientstars.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 创建学生响应DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateStudentResponse {

    private Long id;
    private String username;
    private String realName;
    private String email;
    private Long classId;
    private String initialPassword;
    private String message;
}
