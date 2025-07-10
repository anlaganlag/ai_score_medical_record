# 调试HMS API患者姓名返回
Write-Host "=== 调试HMS API患者姓名返回 ===" -ForegroundColor Green

# 测试HMS API直接调用
$hmsHeaders = @{
    "Authorization" = "Bearer 6a5a8919-a9c7-4605-b917-3abb1df91e83"
    "systemdeptid" = "127"
    "Content-Type" = "application/json"
}

Write-Host "1. 直接调用HMS API获取患者信息..." -ForegroundColor Yellow
try {
    $hmsResponse = Invoke-RestMethod -Uri "http://172.16.1.13:10086/admin-api/business/outpatient-blood/get?patientId=3835" -Method GET -Headers $hmsHeaders -TimeoutSec 30
    
    Write-Host "HMS API响应:" -ForegroundColor Cyan
    $hmsResponse | ConvertTo-Json -Depth 10
    
    if ($hmsResponse.code -eq 0 -and $hmsResponse.data) {
        Write-Host "`n关键字段检查:" -ForegroundColor Yellow
        Write-Host "data.name: '$($hmsResponse.data.name)'" -ForegroundColor Cyan
        Write-Host "data.patientName: '$($hmsResponse.data.patientName)'" -ForegroundColor Cyan
        Write-Host "data.patientNickName: '$($hmsResponse.data.patientNickName)'" -ForegroundColor Cyan
        
        if ($hmsResponse.data.patientName) {
            Write-Host "✅ patientName字段存在且有值: '$($hmsResponse.data.patientName)'" -ForegroundColor Green
        } elseif ($hmsResponse.data.name) {
            Write-Host "⚠️ name字段存在且有值: '$($hmsResponse.data.name)'" -ForegroundColor Yellow
        } else {
            Write-Host "❌ 两个姓名字段都为空" -ForegroundColor Red
        }
    } else {
        Write-Host "HMS API返回错误或无数据" -ForegroundColor Red
    }
    
} catch {
    Write-Host "HMS API调用失败: $($_.Exception.Message)" -ForegroundColor Red
}

Write-Host "`n=== 调试完成 ===" -ForegroundColor Green 