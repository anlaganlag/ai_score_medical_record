$uri = "http://localhost:8080/admin-api/business/ai-score/generate"
$body = @{
    patientId = 3835
} | ConvertTo-Json

try {
    Write-Host "Sending request to: $uri"
    Write-Host "Request body: $body"
    
    $response = Invoke-RestMethod -Uri $uri -Method Post -Body $body -ContentType "application/json"
    
    Write-Host "Response received:"
    $response | ConvertTo-Json -Depth 10
    
} catch {
    Write-Host "Error occurred:"
    Write-Host $_.Exception.Message
    if ($_.Exception.Response) {
        Write-Host "Status Code: $($_.Exception.Response.StatusCode)"
        Write-Host "Status Description: $($_.Exception.Response.StatusDescription)"
    }
} 