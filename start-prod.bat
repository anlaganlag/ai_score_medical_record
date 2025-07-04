@echo off
rem AI病历质控系统生产环境启动脚本

echo Starting AI Medical Score System (Production)...
echo.

rem 检查Java是否安装
java -version > nul 2>&1
if %errorlevel% neq 0 (
    echo Error: Java is not installed or not in PATH
    pause
    exit /b 1
)

rem 创建日志目录
if not exist logs mkdir logs

rem 设置JVM参数
set JAVA_OPTS=-Xms512m -Xmx2g -XX:+UseG1GC -XX:+UseStringDeduplication

rem 启动应用
echo Starting with production profile...
java %JAVA_OPTS% -jar target/ai-medical-score-1.0.0-SNAPSHOT.jar --spring.profiles.active=prod

echo.
echo Application stopped.
pause 