$uri = "http://localhost:8080/admin-api/business/ai-score/generate"
$body = '{"patientId": 4360}'

Write-Host "=== Testing Fixed AI API ==="
Write-Host "URI: $uri"
Write-Host "Body: $body"
Write-Host ""

try {
    Write-Host "Sending request..."
    $response = Invoke-RestMethod -Uri $uri -Method Post -Body $body -ContentType "application/json" -TimeoutSec 60
    
    Write-Host "🎉 SUCCESS! AI API is now working!"
    Write-Host "Response:"
    $response | ConvertTo-Json -Depth 10
    
    # Test report endpoint as well
    Write-Host "`n=== Testing Report Endpoint ==="
    $reportUri = "http://localhost:8080/admin-api/business/ai-score/report/4360"
    $reportResponse = Invoke-RestMethod -Uri $reportUri -Method Get -TimeoutSec 30
    
    Write-Host "Report response:"
    $reportResponse | ConvertTo-Json -Depth 10
    
} catch {
    Write-Host "❌ FAILED: $($_.Exception.Message)"
    if ($_.Exception.Response) {
        Write-Host "Status Code: $($_.Exception.Response.StatusCode.value__)"
        try {
            $stream = $_.Exception.Response.GetResponseStream()
            $reader = New-Object System.IO.StreamReader($stream)
            $responseText = $reader.ReadToEnd()
            Write-Host "Error Response: $responseText"
        } catch {
            Write-Host "Could not read error response"
        }
    }
} 