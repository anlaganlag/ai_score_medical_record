# 测试患者姓名获取
Write-Host "=== 测试患者姓名获取 ===" -ForegroundColor Green

# 测试HMS API直接调用
$hmsHeaders = @{
    "Authorization" = "Bearer 97285ff9-240a-4546-9f14-a9b858397716"
    "systemdeptid" = "127"
    "Content-Type" = "application/json"
}

Write-Host "1. 直接调用HMS API获取患者信息..." -ForegroundColor Yellow
try {
    $hmsResponse = Invoke-RestMethod -Uri "http://172.16.1.13:10086/admin-api/business/outpatient-blood/get?patientId=3835" -Method GET -Headers $hmsHeaders -TimeoutSec 30
    
    Write-Host "HMS API响应:" -ForegroundColor Cyan
    $hmsResponse | ConvertTo-Json -Depth 10
    
    if ($hmsResponse.code -eq 0 -and $hmsResponse.data) {
        Write-Host "患者姓名字段: $($hmsResponse.data.name)" -ForegroundColor Green
    } else {
        Write-Host "HMS API返回错误或无数据" -ForegroundColor Red
    }
    
} catch {
    Write-Host "HMS API调用失败: $($_.Exception.Message)" -ForegroundColor Red
}

Write-Host "`n2. 调用AI评分接口..." -ForegroundColor Yellow

$headers = @{
    "Content-Type" = "application/json"
}

$body = @{
    patientId = "3835"
} | ConvertTo-Json

try {
    $response = Invoke-RestMethod -Uri "http://localhost:8080/admin-api/business/ai-score/generate" -Method POST -Headers $headers -Body $body -TimeoutSec 120
    
    Write-Host "AI评分响应:" -ForegroundColor Cyan
    $response | ConvertTo-Json -Depth 10
    
} catch {
    Write-Host "AI评分调用失败: $($_.Exception.Message)" -ForegroundColor Red
    
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