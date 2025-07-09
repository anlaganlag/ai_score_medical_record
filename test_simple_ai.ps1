# 直接测试SiliconFlow AI API
Write-Host "Testing SiliconFlow AI API directly..."

$headers = @{
    "Authorization" = "Bearer sk-stwdcstqnkcztrsqsrmosrcpekiacrualyzzvsgfmzjvuurv"
    "Content-Type" = "application/json"
}

$body = @{
    "model" = "Qwen/Qwen3-8B"
    "messages" = @(
        @{
            "role" = "system"
            "content" = "你是医疗质控专家。"
        },
        @{
            "role" = "user"
            "content" = "请对病历进行简单评分，返回JSON格式：{`"totalScore`": 85, `"level`": `"乙级`"}"
        }
    )
    "temperature" = 0.1
    "max_tokens" = 1000
} | ConvertTo-Json -Depth 10

Write-Host "Request body:"
Write-Host $body

try {
    $response = Invoke-RestMethod -Uri "https://api.siliconflow.cn/v1/chat/completions" -Method POST -Headers $headers -Body $body
    Write-Host "SUCCESS: AI API works!"
    Write-Host "Response: $($response | ConvertTo-Json -Depth 10)"
} catch {
    Write-Host "ERROR: AI API failed"
    Write-Host "Status: $($_.Exception.Response.StatusCode)"
    Write-Host "Error: $($_.Exception.Message)"
    if ($_.Exception.Response) {
        $reader = New-Object System.IO.StreamReader($_.Exception.Response.GetResponseStream())
        $responseBody = $reader.ReadToEnd()
        Write-Host "Response body: $responseBody"
    }
} 