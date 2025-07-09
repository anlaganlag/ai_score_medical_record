# 测试Mock模式
Write-Host "Testing Mock mode..."

$headers = @{
    "Content-Type" = "application/json"
}

$body = @{
    "patientId" = 4360
} | ConvertTo-Json

Write-Host "Testing /generate endpoint with Mock mode..."

try {
    $response = Invoke-RestMethod -Uri "http://localhost:8080/api/ai-score/generate" -Method POST -Headers $headers -Body $body
    Write-Host "SUCCESS: Mock mode works!"
    Write-Host "Total Score: $($response.data.scoreResponse.totalScore)"
    Write-Host "Level: $($response.data.scoreResponse.level)"
    Write-Host "Full Response: $($response | ConvertTo-Json -Depth 10)"
} catch {
    Write-Host "ERROR: Mock mode failed"
    Write-Host "Status: $($_.Exception.Response.StatusCode)"
    Write-Host "Error: $($_.Exception.Message)"
    if ($_.Exception.Response) {
        $reader = New-Object System.IO.StreamReader($_.Exception.Response.GetResponseStream())
        $responseBody = $reader.ReadToEnd()
        Write-Host "Response body: $responseBody"
    }
} 