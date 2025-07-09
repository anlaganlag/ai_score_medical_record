# 最终测试患者姓名修复
Write-Host "=== 最终测试患者姓名修复 ===" -ForegroundColor Green

$headers = @{
    "Content-Type" = "application/json"
}

# 使用患者ID 3835 (胡安秀) 重新生成评分
Write-Host "1. 重新生成患者3835的AI评分..." -ForegroundColor Yellow

$body = @{
    patientId = "3835"
} | ConvertTo-Json

try {
    $response = Invoke-RestMethod -Uri "http://localhost:8080/admin-api/business/ai-score/generate" -Method POST -Headers $headers -Body $body -TimeoutSec 120
    
    Write-Host "✅ AI评分生成成功!" -ForegroundColor Green
    Write-Host "记录ID: $($response.data.id)" -ForegroundColor Cyan
    
    # 等待2秒确保数据保存
    Start-Sleep -Seconds 2
    
    # 获取最新的评分报告
    Write-Host "`n2. 获取最新的评分报告..." -ForegroundColor Yellow
    $reportResponse = Invoke-RestMethod -Uri "http://localhost:8080/admin-api/business/ai-score/report/3835" -Method GET -Headers $headers -TimeoutSec 30
    
    Write-Host "✅ 报告获取成功!" -ForegroundColor Green
    Write-Host "患者ID: $($reportResponse.data.patientId)" -ForegroundColor Cyan
    Write-Host "患者姓名: '$($reportResponse.data.patientName)'" -ForegroundColor Cyan
    Write-Host "评分总分: $($reportResponse.data.scoreResult.totalScore)" -ForegroundColor Cyan
    Write-Host "评分等级: $($reportResponse.data.scoreResult.level)" -ForegroundColor Cyan
    Write-Host "创建时间: $($reportResponse.data.createdTime)" -ForegroundColor Cyan
    
    if ($reportResponse.data.patientName -and $reportResponse.data.patientName.Trim() -ne "") {
        Write-Host "`n🎉 SUCCESS: 患者姓名已正确存储和显示: '$($reportResponse.data.patientName)'" -ForegroundColor Green
    } else {
        Write-Host "`n❌ FAILED: 患者姓名仍为空或null" -ForegroundColor Red
    }
    
} catch {
    Write-Host "❌ 测试失败: $($_.Exception.Message)" -ForegroundColor Red
    if ($_.Exception.Response) {
        $statusCode = $_.Exception.Response.StatusCode
        Write-Host "HTTP状态码: $statusCode" -ForegroundColor Red
    }
}

Write-Host "`n=== 测试完成 ===" -ForegroundColor Green 