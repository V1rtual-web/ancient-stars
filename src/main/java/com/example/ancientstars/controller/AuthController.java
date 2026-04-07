package com.example.ancientstars.controller;

import com.example.ancientstars.common.ApiResponse;
import com.example.ancientstars.dto.LoginRequest;
import com.example.ancientstars.dto.LoginResponse;
import com.example.ancientstars.dto.RefreshTokenRequest;
import com.example.ancientstars.dto.RegisterRequest;
import com.example.ancientstars.dto.TokenResponse;
import com.example.ancientstars.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 认证控制器
 */
@RestController
@RequestMapping("/auth")
@Tag(name = "认证管理", description = "用户登录、注册、登出、令牌刷新等接口")
public class AuthController {

    @Autowired
    private UserService userService;

    /**
     * 用户登录
     */
    @PostMapping("/login")
    @Operation(summary = "用户登录", description = "使用用户名和密码登录系统")
    public ApiResponse<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse response = userService.login(request);
        return ApiResponse.success(response);
    }

    /**
     * 用户注册
     */
    @PostMapping("/register")
    @Operation(summary = "用户注册", description = "注册新用户账户（教师或学生）")
    public ApiResponse<LoginResponse> register(@Valid @RequestBody RegisterRequest request) {
        LoginResponse response = userService.register(request);
        return ApiResponse.success(response);
    }

    /**
     * 刷新访问令牌
     */
    @PostMapping("/refresh")
    @Operation(summary = "刷新令牌", description = "使用刷新令牌获取新的访问令牌")
    public ApiResponse<TokenResponse> refreshToken(@Valid @RequestBody RefreshTokenRequest request) {
        String accessToken = userService.refreshAccessToken(request.getRefreshToken());
        return ApiResponse.success(new TokenResponse(accessToken));
    }

    /**
     * 用户登出
     */
    @PostMapping("/logout")
    @Operation(summary = "用户登出", description = "退出登录")
    public ApiResponse<Void> logout() {
        // JWT是无状态的，客户端删除令牌即可
        // 如果需要黑名单功能，可以在这里实现
        return ApiResponse.success(null);
    }
}
