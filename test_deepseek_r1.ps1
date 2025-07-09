# 测试 DeepSeek-R1 模型配置
Write-Host "=== 测试 DeepSeek-R1 模型 ===" -ForegroundColor Green

$headers = @{
    "Content-Type" = "application/json"
}

$body = @{
    patientId = "3941"
} | ConvertTo-Json

Write-Host "发送请求到: http://localhost:8080/admin-api/business/ai-score/generate" -ForegroundColor Yellow
Write-Host "请求体: $body" -ForegroundColor Yellow

try {
    $response = Invoke-RestMethod -Uri "http://localhost:8080/admin-api/business/ai-score/generate" -Method POST -Headers $headers -Body $body -TimeoutSec 120
    
    Write-Host "✅ 请求成功!" -ForegroundColor Green
    Write-Host "响应内容:" -ForegroundColor Cyan
    $response | ConvertTo-Json -Depth 10
    
} catch {
    Write-Host "❌ 请求失败!" -ForegroundColor Red
    Write-Host "错误信息: $($_.Exception.Message)" -ForegroundColor Red
    
    if ($_.Exception.Response) {
        $statusCode = $_.Exception.Response.StatusCode
        Write-Host "HTTP状态码: $statusCode" -ForegroundColor Red
        
        try {
            $errorStream = $_.Exception.Response.GetResponseStream()
            $reader = New-Object System.IO.StreamReader($errorStream)
            $errorBody = $reader.ReadToEnd()
            Write-Host "错误响应体: $errorBody" -ForegroundColor Red
        } catch {
            Write-Host "无法读取错误响应体" -ForegroundColor Red
        }
    }
}

Write-Host "`n=== 测试完成 ===" -ForegroundColor Green 