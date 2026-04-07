package com.example.ancientstars.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 学生DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class StudentDTO {

    private Long id;
    private String username;
    private String realName;
    private String email;
    private Long classId;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
