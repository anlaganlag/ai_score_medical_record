$uri = "http://localhost:8080/admin-api/business/ai-score/report/3835"

try {
    Write-Host "Getting report from: $uri"
    
    $response = Invoke-RestMethod -Uri $uri -Method Get
    
    Write-Host "Report received:"
    $response | ConvertTo-Json -Depth 10
    
} catch {
    Write-Host "Error occurred:"
    Write-Host $_.Exception.Message
} 