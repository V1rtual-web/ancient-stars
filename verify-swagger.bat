@echo off
echo ========================================
echo Swagger API 文档验证脚本
echo ========================================
echo.

echo [1/3] 检查应用是否运行...
curl -s http://localhost:8080/api/health > nul 2>&1
if %errorlevel% neq 0 (
    echo [错误] 应用未运行，请先启动应用
    echo 运行命令: mvn spring-boot:run
    pause
    exit /b 1
)
echo [成功] 应用正在运行
echo.

echo [2/3] 检查 OpenAPI 文档...
curl -s http://localhost:8080/api/v3/api-docs > nul 2>&1
if %errorlevel% neq 0 (
    echo [错误] 无法访问 OpenAPI 文档
    pause
    exit /b 1
)
echo [成功] OpenAPI 文档可访问
echo.

echo [3/3] 打开 Swagger UI...
start http://localhost:8080/api/swagger-ui.html
echo [成功] Swagger UI 已在浏览器中打开
echo.

echo ========================================
echo 验证完成！
echo ========================================
echo.
echo Swagger UI 地址: http://localhost:8080/api/swagger-ui.html
echo OpenAPI 文档地址: http://localhost:8080/api/v3/api-docs
echo.
pause
