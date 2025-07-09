$uri = "https://api.siliconflow.cn/v1/chat/completions"
$headers = @{
    "Authorization" = "Bearer sk-stwdcstqnkcztrsqsrmosrcpekiacrualyzzvsgfmzjvuurv"
    "Content-Type" = "application/json"
}

# Test 1: Simple request (baseline)
Write-Host "=== Test 1: Simple Request ==="
$simpleBody = @{
    "model" = "Qwen/Qwen3-8B"
    "messages" = @(
        @{
            "role" = "user"
            "content" = "Hello, please respond with a simple JSON like: {`"message`": `"Hello!`"}"
        }
    )
    "temperature" = 0.1
    "max_tokens" = 100
} | ConvertTo-Json -Depth 10

try {
    $response = Invoke-RestMethod -Uri $uri -Method Post -Headers $headers -Body $simpleBody -TimeoutSec 30
    Write-Host "✅ Simple request successful"
    $response | ConvertTo-Json -Depth 10
} catch {
    Write-Host "❌ Simple request failed: $($_.Exception.Message)"
}

# Test 2: Complex system prompt (like our app)
Write-Host "`n=== Test 2: Complex System Prompt ==="
$complexBody = @{
    "model" = "Qwen/Qwen3-8B"
    "messages" = @(
        @{
            "role" = "system"
            "content" = @"
你是一个专业的医疗质控专家，需要根据病历信息进行评分。

## 评分维度
请按照以下8个维度进行评分：
1. 主诉 (10分)：评估主诉的完整性和规范性
2. 病史 (20分)：包括现病史、既往史、过敏史等
3. 体格检查 (15分)：评估体格检查记录的完整性

## 输出格式要求
请严格按照以下JSON格式返回评分结果：
{
  "totalScore": 总分数字,
  "level": "甲级/乙级/丙级",
  "details": [
    {
      "item": "主诉",
      "fullScore": 10,
      "score": 实际得分,
      "deduction": "扣分说明"
    }
  ]
}
"@
        },
        @{
            "role" = "user"
            "content" = "请根据以下病历信息进行评分：患者信息：{\"name\":\"测试患者\"} 诊疗信息：{\"mainSuit\":\"头痛3天\"}"
        }
    )
    "temperature" = 0.1
    "max_tokens" = 1000
} | ConvertTo-Json -Depth 10

try {
    $response = Invoke-RestMethod -Uri $uri -Method Post -Headers $headers -Body $complexBody -TimeoutSec 30
    Write-Host "✅ Complex request successful"
    $response | ConvertTo-Json -Depth 10
} catch {
    Write-Host "❌ Complex request failed: $($_.Exception.Message)"
    if ($_.Exception.Response) {
        Write-Host "Status Code: $($_.Exception.Response.StatusCode.value__)"
        try {
            $reader = New-Object System.IO.StreamReader($_.Exception.Response.GetResponseStream())
            $responseContent = $reader.ReadToEnd()
            Write-Host "Response Content: $responseContent"
        } catch {
            Write-Host "Could not read response content"
        }
    }
} 