$uri = "https://api.siliconflow.cn/v1/chat/completions"
$headers = @{
    "Authorization" = "Bearer sk-stwdcstqnkcztrsqsrmosrcpekiacrualyzzvsgfmzjvuurv"
    "Content-Type" = "application/json"
}

# 模拟我们Spring应用发送的完全相同的请求体
$body = @{
    model = "Qwen/Qwen3-8B"
    messages = @(
        @{
            role = "system"
            content = "你是医疗质控专家，评估病历质量。评分维度：主诉(10分)、病史(20分)、体格检查(15分)、诊断(15分)、处理(20分)、其他(20分)。返回JSON格式：{`"totalScore`": 分数, `"level`": `"甲级/乙级/丙级`", `"details`": [{`"item`": `"主诉`", `"score`": 分数, `"deduction`": `"说明`"}]}"
        },
        @{
            role = "user"  
            content = "评估病历：患者{`"name`":`"胡安秀`"}，诊疗{`"mainSuit`":`"维持性血液透析13年余`"}"
        }
    )
    temperature = 0.1
    max_tokens = 10000
} | ConvertTo-Json -Depth 10

Write-Host "Testing exact Spring app request format..."
Write-Host "Request body:"
Write-Host $body

try {
    $response = Invoke-RestMethod -Uri $uri -Method Post -Headers $headers -Body $body -TimeoutSec 30
    Write-Host "`nSUCCESS! Spring app format works:"
    $response | ConvertTo-Json -Depth 10
} catch {
    Write-Host "`nFAILED with Spring app format: $($_.Exception.Message)"
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