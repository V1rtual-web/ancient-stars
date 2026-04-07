@echo off
echo ========================================
echo 旧星背单词系统 - 数据库连接测试
echo ========================================
echo.

echo 正在测试数据库连接...
echo.

mysql -h localhost -u root -p123456 -e "SELECT VERSION();"

if %errorlevel% equ 0 (
    echo.
    echo [成功] 数据库连接正常！
    echo.
    echo 是否要初始化数据库？(Y/N)
    set /p choice=
    if /i "%choice%"=="Y" (
        echo.
        echo 正在初始化数据库...
        mysql -h localhost -u root -p123456 < init-database.sql
        if %errorlevel% equ 0 (
            echo [成功] 数据库初始化完成！
        ) else (
            echo [失败] 数据库初始化失败！
        )
    )
) else (
    echo.
    echo [失败] 数据库连接失败！
    echo 请检查：
    echo 1. MySQL服务是否已启动
    echo 2. 用户名和密码是否正确（当前：root/123456）
    echo 3. 端口是否正确（默认：3306）
)

echo.
pause
