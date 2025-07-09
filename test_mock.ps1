$uri = "http://localhost:8080/admin-api/business/ai-score/generate"
$body = '{"patientId": 3945}'

Write-Host "Testing Mock AI Service..."
Write-Host "URI: $uri"
Write-Host "Body: $body"

try {
    $response = Invoke-RestMethod -Uri $uri -Method Post -Body $body -ContentType "application/json"
    Write-Host "SUCCESS! Mock AI response received:"
    $response | ConvertTo-Json -Depth 10
    
    # Also test the report endpoint
    Write-Host "`nTesting report endpoint..."
    $reportUri = "http://localhost:8080/admin-api/business/ai-score/report/3945"
    $reportResponse = Invoke-RestMethod -Uri $reportUri -Method Get
    Write-Host "Report response:"
    $reportResponse | ConvertTo-Json -Depth 10
    
} catch {
    Write-Host "FAILED: $($_.Exception.Message)"
    if ($_.Exception.Response) {
        Write-Host "Status Code: $($_.Exception.Response.StatusCode.value__)"
    }
} 