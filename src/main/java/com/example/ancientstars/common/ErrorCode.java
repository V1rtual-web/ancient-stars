package com.example.ancientstars.common;

import lombok.Getter;

/**
 * 错误码枚举
 */
@Getter
public enum ErrorCode {

    // 通用错误 1xxx
    SUCCESS("1000", "操作成功"),
    SYSTEM_ERROR("1001", "系统错误"),
    INVALID_PARAMETER("1002", "参数错误"),
    RESOURCE_NOT_FOUND("1003", "资源不存在"),

    // 认证授权错误 2xxx
    UNAUTHORIZED("2001", "未授权，请先登录"),
    FORBIDDEN("2002", "权限不足"),
    TOKEN_EXPIRED("2003", "令牌已过期"),
    TOKEN_INVALID("2004", "令牌无效"),
    LOGIN_FAILED("2005", "用户名或密码错误"),

    // 用户相关错误 3xxx
    USER_NOT_FOUND("3001", "用户不存在"),
    USER_ALREADY_EXISTS("3002", "用户已存在"),
    USER_DISABLED("3003", "用户已被禁用"),
    USERNAME_ALREADY_EXISTS("3004", "用户名已存在"),
    INVALID_TOKEN("3005", "无效的令牌"),

    // 词汇相关错误 4xxx
    VOCABULARY_NOT_FOUND("4001", "词汇不存在"),
    VOCABULARY_ALREADY_EXISTS("4002", "词汇已存在"),
    WORD_LIST_NOT_FOUND("4003", "词汇表不存在"),
    DUPLICATE_RESOURCE("4004", "资源重复"),

    // 任务相关错误 5xxx
    TASK_NOT_FOUND("5001", "任务不存在"),
    TASK_ALREADY_ASSIGNED("5002", "任务已分配"),
    TASK_ASSIGNMENT_NOT_FOUND("5003", "任务分配记录不存在"),
    INVALID_USER_ROLE("5004", "用户角色不正确"),

    // 数据库错误 6xxx
    DATABASE_ERROR("6001", "数据库操作失败"),
    DATABASE_CONNECTION_FAILED("6002", "数据库连接失败"),
    TRANSACTION_ERROR("6003", "事务处理失败");

    private final String code;
    private final String message;

    ErrorCode(String code, String message) {
        this.code = code;
        this.message = message;
    }
}
