# 🔧 API路径修正说明

## 🎯 重要发现
通过分析Controller代码，发现了API路径错误：

### ❌ 错误路径（之前使用）
```
POST http://localhost:8080/api/ai-score/generate
```
**结果**：404 Not Found

### ✅ 正确路径（应该使用）
```
POST http://localhost:8080/admin-api/business/ai-score/generate
```

## 📋 完整API列表

### 1. 生成AI评分
- **路径**：`POST /admin-api/business/ai-score/generate`
- **请求体**：
```json
{
  "patientId": 4360
}
```

### 2. 查看评分报告
- **路径**：`GET /admin-api/business/ai-score/report/{patientId}`
- **示例**：`GET /admin-api/business/ai-score/report/4360`

### 3. 保存专家点评
- **路径**：`POST /admin-api/business/ai-score/expert-comment`
- **请求体**：
```json
{
  "patientId": 4360,
  "expertComment": "专家点评内容"
}
```

## 🚀 测试方法

### 方法1：使用curl
```bash
# 生成评分
curl -X POST http://localhost:8080/admin-api/business/ai-score/generate \
  -H "Content-Type: application/json" \
  -d "{\"patientId\": 4360}"

# 查看报告
curl -X GET http://localhost:8080/admin-api/business/ai-score/report/4360
```

### 方法2：使用PowerShell脚本
```bash
.\test_correct_api.ps1
```

### 方法3：使用批处理文件
```bash
.\test_correct_curl.bat
```

## 📊 预期响应格式

### 成功响应
```json
{
  "code": 0,
  "msg": "评分生成成功",
  "data": {
    "id": 123,
    "patientId": 4360,
    "status": "completed"
  }
}
```

### 错误响应
```json
{
  "code": 500,
  "msg": "评分生成失败：错误详情",
  "data": null
}
```

## 🔍 当前状态
1. ✅ **URI问题已修复**：空URI改为正确端点
2. ✅ **Mock模式已启用**：绕过AI API问题
3. ✅ **API路径已确认**：使用正确的Controller路径
4. ⏳ **等待测试验证**：使用正确路径进行测试

## 💡 下一步
请使用正确的API路径进行测试：
```bash
curl -X POST http://localhost:8080/admin-api/business/ai-score/generate \
  -H "Content-Type: application/json" \
  -d "{\"patientId\": 4360}"
``` 