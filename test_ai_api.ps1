$uri = "https://api.siliconflow.cn/v1/chat/completions"
$headers = @{
    "Authorization" = "Bearer sk-stwdcstqnkcztrsqsrmosrcpekiacrualyzzvsgfmzjvuurv"
    "Content-Type" = "application/json"
}

$body = @{
    "model" = "Qwen/Qwen3-8B"
    "messages" = @(
        @{
            "role" = "user"
            "content" = "Hello"
        }
    )
    "temperature" = 0.1
    "max_tokens" = 100
} | ConvertTo-Json -Depth 10

Write-Host "Testing AI API: $uri"
Write-Host "Request body: $body"

try {
    $response = Invoke-RestMethod -Uri $uri -Method Post -Headers $headers -Body $body -TimeoutSec 30
    Write-Host "AI API test successful!"
    $response | ConvertTo-Json -Depth 10
} catch {
    Write-Host "AI API test failed:"
    Write-Host "Error: $($_.Exception.Message)"
    
    if ($_.Exception.Response) {
        Write-Host "Status Code: $($_.Exception.Response.StatusCode.value__)"
        Write-Host "Status Description: $($_.Exception.Response.StatusDescription)"
        
        try {
            $reader = New-Object System.IO.StreamReader($_.Exception.Response.GetResponseStream())
            $responseContent = $reader.ReadToEnd()
            Write-Host "Response Content: $responseContent"
        } catch {
            Write-Host "Could not read response content"
        }
    }
} 