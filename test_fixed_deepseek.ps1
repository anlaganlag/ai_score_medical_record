# 测试DeepSeek API 400错误修复
Write-Host "测试DeepSeek API 400错误是否已修复..." -ForegroundColor Green

$baseUrl = "http://localhost:8080"
$patientId = 3835

# 构建请求体
$requestBody = @{
    patientId = $patientId
} | ConvertTo-Json

Write-Host "正在调用 /generate 接口..." -ForegroundColor Yellow

try {
    $response = Invoke-RestMethod -Uri "$baseUrl/admin-api/business/ai-score/generate" -Method POST -Body $requestBody -ContentType "application/json" -TimeoutSec 60
    
    Write-Host "✅ 成功! AI评分已生成" -ForegroundColor Green
    Write-Host "总分: $($response.totalScore), 等级: $($response.level)" -ForegroundColor Cyan
    
} catch {
    $statusCode = $_.Exception.Response.StatusCode.value__
    Write-Host "❌ 失败! 状态码: $statusCode" -ForegroundColor Red
    
    if ($statusCode -eq 400) {
        Write-Host "🔍 仍然是400错误，需要进一步调试" -ForegroundColor Yellow
    } elseif ($statusCode -eq 500) {
        Write-Host "🔍 500错误，可能是AI API调用问题" -ForegroundColor Yellow
    }
    
    Write-Host "错误信息: $($_.Exception.Message)" -ForegroundColor Red
}

Write-Host "测试完成!" -ForegroundColor Green 