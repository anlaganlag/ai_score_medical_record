@echo off
echo ===========================================
echo    AI病历质控系统 - 批量测试脚本
echo ===========================================
echo.

rem 设置服务器地址
set SERVER_URL=http://localhost:8080

echo 1. 检查应用是否启动...
echo 访问地址: %SERVER_URL%/actuator/health
echo.
curl -s %SERVER_URL%/actuator/health
if %errorlevel% neq 0 (
    echo [错误] 应用未启动或连接失败
    echo 请先运行: start.bat
    pause
    exit /b 1
)
echo [成功] 应用已启动
echo.

echo 2. 测试生成AI病历评分...
echo 接口: POST %SERVER_URL%/api/ai-score/generate
echo.
curl -X POST %SERVER_URL%/api/ai-score/generate ^
  -H "Content-Type: application/json" ^
  -d "{\"patientId\":\"TEST001\",\"requestId\":\"REQ%date:~0,4%%date:~5,2%%date:~8,2%_%time:~0,2%%time:~3,2%%time:~6,2%\"}"
echo.
echo.

echo 3. 查看评分报告...
echo 接口: GET %SERVER_URL%/api/ai-score/report/TEST001
echo.
curl -X GET %SERVER_URL%/api/ai-score/report/TEST001
echo.
echo.

echo 4. 测试保存专家点评...
echo 接口: POST %SERVER_URL%/api/ai-score/expert-comment
echo.
curl -X POST %SERVER_URL%/api/ai-score/expert-comment ^
  -H "Content-Type: application/json" ^
  -d "{\"patientId\":\"TEST001\",\"expertComment\":\"测试专家点评：病历记录规范，诊断准确\",\"expertScore\":88,\"expertName\":\"测试医生\"}"
echo.
echo.

echo 5. 再次查看更新后的评分报告...
echo.
curl -X GET %SERVER_URL%/api/ai-score/report/TEST001
echo.
echo.

echo 6. 测试应用信息接口...
echo 接口: GET %SERVER_URL%/actuator/info
echo.
curl -s %SERVER_URL%/actuator/info
echo.
echo.

echo ===========================================
echo           测试完成！
echo ===========================================
echo.
echo 如果看到JSON格式的响应数据，说明API接口工作正常
echo 如果有错误，请检查：
echo 1. 应用是否正常启动
echo 2. 数据库连接是否正常
echo 3. API密钥是否有效
echo.
pause 