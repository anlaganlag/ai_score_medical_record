@echo off
echo Starting AI Medical Score System...
echo.

REM 检查Java版本
java -version
if %errorlevel% neq 0 (
    echo Error: Java not found! Please install JDK 17 or higher.
    pause
    exit /b 1
)

REM 检查JAR文件是否存在
if not exist "target\ai-medical-score-1.0.0-SNAPSHOT.jar" (
    echo JAR file not found. Building project...
    call mvnd clean package -DskipTests
    if %errorlevel% neq 0 (
        echo Build failed!
        pause
        exit /b 1
    )
)

REM 启动应用
echo Starting application on port 8080...
java -jar target\ai-medical-score-1.0.0-SNAPSHOT.jar

pause 