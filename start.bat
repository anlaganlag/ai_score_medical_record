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

REM 删除现有JAR文件并重新构建
if exist "target\ai-medical-score-1.0.0-SNAPSHOT.jar" (
    echo Deleting existing JAR file...
    del target\ai-medical-score-1.0.0-SNAPSHOT.jar
)

echo Building project...
call mvnd clean package -DskipTests
if %errorlevel% neq 0 (
    echo Build failed!
    pause
    exit /b 1
)

REM 启动应用
echo Starting application on port 8080...
java -jar target\ai-medical-score-1.0.0-SNAPSHOT.jar

pause 