# 诊断500错误的具体原因
Write-Host "=== 诊断500错误 ===" -ForegroundColor Green

# 1. 检查应用是否在运行
Write-Host "1. 检查应用状态..." -ForegroundColor Yellow
try {
    $healthCheck = Invoke-RestMethod -Uri "http://localhost:8080/actuator/health" -Method GET -TimeoutSec 5
    Write-Host "✅ 应用正在运行" -ForegroundColor Green
} catch {
    Write-Host "❌ 应用可能未运行或健康检查失败" -ForegroundColor Red
    Write-Host "请确保应用已启动: java -jar target/ai-medical-score-1.0.0-SNAPSHOT.jar"
    exit
}

# 2. 测试HMS API连接
Write-Host "`n2. 测试HMS API连接..." -ForegroundColor Yellow
$hmsUrl = "http://172.16.1.13:10086"
$token = "97285ff9-240a-4546-9f14-a9b858397716"

try {
    $headers = @{ "token" = $token }
    $hmsResponse = Invoke-RestMethod -Uri "$hmsUrl/admin-api/business/outpatient-blood/get?patientId=3835" -Headers $headers -TimeoutSec 10
    Write-Host "✅ HMS API连接正常" -ForegroundColor Green
} catch {
    Write-Host "❌ HMS API连接失败: $($_.Exception.Message)" -ForegroundColor Red
    Write-Host "这可能是500错误的原因" -ForegroundColor Yellow
}

# 3. 测试DeepSeek API连接
Write-Host "`n3. 测试DeepSeek API连接..." -ForegroundColor Yellow
$deepseekUrl = "https://api.siliconflow.cn/v1/chat/completions"
$apiKey = "sk-stwdcstqnkcztrsqsrmosrcpekiacrualyzzvsgfmzjvuurv"

$testBody = @{
    model = "deepseek-ai/DeepSeek-V3"
    messages = @(
        @{
            role = "user"
            content = "Hello"
        }
    )
    max_tokens = 50
} | ConvertTo-Json -Depth 10

try {
    $headers = @{ 
        "Authorization" = "Bearer $apiKey"
        "Content-Type" = "application/json"
    }
    $deepseekResponse = Invoke-RestMethod -Uri $deepseekUrl -Method POST -Body $testBody -Headers $headers -TimeoutSec 15
    Write-Host "✅ DeepSeek API连接正常" -ForegroundColor Green
} catch {
    Write-Host "❌ DeepSeek API连接失败: $($_.Exception.Message)" -ForegroundColor Red
    Write-Host "这可能是500错误的原因" -ForegroundColor Yellow
}

# 4. 查看应用日志（如果可能）
Write-Host "`n4. 建议查看应用日志..." -ForegroundColor Yellow
Write-Host "请在应用控制台查看详细错误信息" -ForegroundColor Cyan
Write-Host "或者查看日志文件: logs/application.log" -ForegroundColor Cyan

Write-Host "`n=== 诊断完成 ===" -ForegroundColor Green 