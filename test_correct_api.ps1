# 测试正确的API路径
Write-Host "Testing correct API path with Mock mode..."

$headers = @{
    "Content-Type" = "application/json"
}

$body = @{
    "patientId" = 4360
} | ConvertTo-Json

Write-Host "Request URL: http://localhost:8080/admin-api/business/ai-score/generate"
Write-Host "Request Body: $body"

try {
    $response = Invoke-RestMethod -Uri "http://localhost:8080/admin-api/business/ai-score/generate" -Method POST -Headers $headers -Body $body
    Write-Host "SUCCESS: API works!"
    Write-Host "Response Code: $($response.code)"
    Write-Host "Response Message: $($response.msg)"
    
    if ($response.data) {
        Write-Host "Patient ID: $($response.data.patientId)"
        Write-Host "Status: $($response.data.status)"
        Write-Host "Record ID: $($response.data.id)"
    }
    
    Write-Host "Full Response:"
    Write-Host ($response | ConvertTo-Json -Depth 10)
} catch {
    Write-Host "ERROR: API failed"
    Write-Host "Status: $($_.Exception.Response.StatusCode)"
    Write-Host "Error: $($_.Exception.Message)"
    
    if ($_.Exception.Response) {
        try {
            $reader = New-Object System.IO.StreamReader($_.Exception.Response.GetResponseStream())
            $responseBody = $reader.ReadToEnd()
            Write-Host "Response body: $responseBody"
        } catch {
            Write-Host "Could not read response body"
        }
    }
}

Write-Host "`nPress any key to continue..."
Read-Host 