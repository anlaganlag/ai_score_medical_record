# 检查AI评分结果中的患者姓名
Write-Host "=== 检查AI评分结果中的患者姓名 ===" -ForegroundColor Green

$headers = @{
    "Content-Type" = "application/json"
}

# 检查患者ID 3835 (胡安秀) 的评分报告
Write-Host "1. 检查患者ID 3835 (胡安秀) 的评分报告..." -ForegroundColor Yellow

try {
    $reportResponse = Invoke-RestMethod -Uri "http://localhost:8080/admin-api/business/ai-score/report/3835" -Method GET -Headers $headers -TimeoutSec 30
    
    Write-Host "✅ 报告获取成功!" -ForegroundColor Green
    Write-Host "患者ID: $($reportResponse.data.patientId)" -ForegroundColor Cyan
    Write-Host "患者姓名: $($reportResponse.data.patientName)" -ForegroundColor Cyan
    Write-Host "评分总分: $($reportResponse.data.scoreResult.totalScore)" -ForegroundColor Cyan
    Write-Host "评分等级: $($reportResponse.data.scoreResult.level)" -ForegroundColor Cyan
    Write-Host "创建时间: $($reportResponse.data.createdTime)" -ForegroundColor Cyan
    
    if ($reportResponse.data.patientName) {
        Write-Host "🎉 患者姓名已正确存储: $($reportResponse.data.patientName)" -ForegroundColor Green
    } else {
        Write-Host "❌ 患者姓名仍为空" -ForegroundColor Red
    }
    
} catch {
    Write-Host "❌ 获取报告失败: $($_.Exception.Message)" -ForegroundColor Red
}

Write-Host "`n=== 检查完成 ===" -ForegroundColor Green 