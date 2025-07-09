# 测试修复后的generate接口
Write-Host "Testing fixed generate API..."

$headers = @{
    "Content-Type" = "application/json"
}

$body = @{
    "patientId" = 4360
} | ConvertTo-Json

try {
    $response = Invoke-RestMethod -Uri "http://localhost:8080/api/ai-score/generate" -Method POST -Headers $headers -Body $body
    Write-Host "SUCCESS: Generate API works!"
    Write-Host "Response: $($response | ConvertTo-Json -Depth 10)"
} catch {
    Write-Host "ERROR: Generate API failed"
    Write-Host "Status: $($_.Exception.Response.StatusCode)"
    Write-Host "Error: $($_.Exception.Message)"
} 