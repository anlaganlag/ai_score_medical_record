# 测试患者姓名修复
Write-Host "=== 测试患者姓名修复 ===" -ForegroundColor Green

# 首先重新启动应用以加载修复
Write-Host "1. 重新启动应用..." -ForegroundColor Yellow
Start-Process -FilePath "cmd" -ArgumentList "/c", "start.bat" -WindowStyle Hidden
Start-Sleep -Seconds 10

Write-Host "2. 调用AI评分接口..." -ForegroundColor Yellow

$headers = @{
    "Content-Type" = "application/json"
}

$body = @{
    patientId = "3835"
} | ConvertTo-Json

try {
    $response = Invoke-RestMethod -Uri "http://localhost:8080/admin-api/business/ai-score/generate" -Method POST -Headers $headers -Body $body -TimeoutSec 120
    
    Write-Host "✅ AI评分调用成功!" -ForegroundColor Green
    Write-Host "响应内容:" -ForegroundColor Cyan
    $response | ConvertTo-Json -Depth 10
    
    # 获取评分报告查看患者姓名
    Write-Host "`n3. 获取评分报告查看患者姓名..." -ForegroundColor Yellow
    $reportResponse = Invoke-RestMethod -Uri "http://localhost:8080/admin-api/business/ai-score/report/3835" -Method GET -Headers $headers -TimeoutSec 30
    
    Write-Host "评分报告:" -ForegroundColor Cyan
    $reportResponse | ConvertTo-Json -Depth 10
    
    if ($reportResponse.data -and $reportResponse.data.patientName) {
        Write-Host "✅ 患者姓名已正确保存: $($reportResponse.data.patientName)" -ForegroundColor Green
    } else {
        Write-Host "❌ 患者姓名仍然为空" -ForegroundColor Red
    }
    
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