$uri = "http://localhost:8080/admin-api/business/ai-score/generate"
$body = @{
    patientId = 3835
} | ConvertTo-Json

Write-Host "Testing API: $uri"
Write-Host "Request body: $body"

try {
    $response = Invoke-RestMethod -Uri $uri -Method Post -Body $body -ContentType "application/json"
    Write-Host "Success! Response:"
    $response | ConvertTo-Json -Depth 10
} catch {
    Write-Host "Error occurred:"
    Write-Host "Message: $($_.Exception.Message)"
    
    if ($_.Exception.Response) {
        Write-Host "Status Code: $($_.Exception.Response.StatusCode.value__)"
        Write-Host "Status Description: $($_.Exception.Response.StatusDescription)"
        
        # Try to read the response content
        try {
            $reader = New-Object System.IO.StreamReader($_.Exception.Response.GetResponseStream())
            $responseContent = $reader.ReadToEnd()
            Write-Host "Response Content: $responseContent"
        } catch {
            Write-Host "Could not read response content"
        }
    }
} 