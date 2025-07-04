# AI病历质控系统 API接口文档

## 接口概述

基于HMS住院系统集成的AI病历质控功能，提供病历自动化评分和专家点评功能。

**基础URL**: `http://localhost:8080/admin-api/business/ai-score`

## 接口列表

### 1. 生成AI评分

**接口地址**: `POST /generate`

**功能描述**: 对指定患者的病历进行AI评分

**请求头**:
```
Content-Type: application/json
Authorization: Bearer {token} (可选)
systemdeptid: {部门ID} (可选)
```

**请求参数**:
```json
{
  "patientId": 123
}
```

**参数说明**:
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| patientId | Long | 是 | 患者ID |

**响应示例**:
```json
{
  "code": 0,
  "data": {
    "id": 1,
    "patientId": 123,
    "status": "completed"
  },
  "msg": "评分生成成功"
}
```

### 2. 查看AI评分报告

**接口地址**: `GET /report/{patientId}`

**功能描述**: 获取指定患者的AI评分报告

**请求头**:
```
Authorization: Bearer {token} (可选)
systemdeptid: {部门ID} (可选)
```

**路径参数**:
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| patientId | Long | 是 | 患者ID |

**响应示例**:
```json
{
  "code": 0,
  "data": {
    "id": 1,
    "patientId": 123,
    "patientName": "张三",
    "expertComment": "病历记录较为完整，建议补充辅助检查结果。",
    "createdTime": "2024-01-01T10:00:00",
    "updatedTime": "2024-01-01T10:30:00",
    "scoreResult": {
      "totalScore": 85,
      "level": "乙级",
      "details": [
        {
          "item": "主诉",
          "fullScore": 10,
          "score": 8,
          "deduction": "主诉描述较为简单，缺少具体时间和症状细节"
        },
        {
          "item": "病史",
          "fullScore": 20,
          "score": 16,
          "deduction": "现病史记录完整，但既往史略显简单"
        },
        {
          "item": "体格检查",
          "fullScore": 15,
          "score": 15,
          "deduction": "无扣分"
        },
        {
          "item": "辅助检查",
          "fullScore": 5,
          "score": 0,
          "deduction": "缺少必要的辅助检查结果记录"
        },
        {
          "item": "诊断",
          "fullScore": 15,
          "score": 15,
          "deduction": "无扣分"
        },
        {
          "item": "处理",
          "fullScore": 20,
          "score": 16,
          "deduction": "治疗方案基本合理，但缺少详细的用药说明"
        },
        {
          "item": "总体评价",
          "fullScore": 5,
          "score": 5,
          "deduction": "无扣分"
        },
        {
          "item": "其他",
          "fullScore": 10,
          "score": 10,
          "deduction": "无扣分"
        }
      ]
    }
  },
  "msg": ""
}
```

### 3. 保存专家点评

**接口地址**: `POST /expert-comment`

**功能描述**: 为指定患者的病历保存专家点评

**请求头**:
```
Content-Type: application/json
Authorization: Bearer {token} (可选)
systemdeptid: {部门ID} (可选)
```

**请求参数**:
```json
{
  "patientId": 123,
  "expertComment": "病历记录较为完整，建议补充辅助检查结果。"
}
```

**参数说明**:
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| patientId | Long | 是 | 患者ID |
| expertComment | String | 是 | 专家点评内容，最多100字 |

**响应示例**:
```json
{
  "code": 0,
  "data": true,
  "msg": "专家点评保存成功"
}
```

## 错误码说明

| 错误码 | 说明 |
|--------|------|
| 0 | 成功 |
| 400 | 请求参数错误 |
| 404 | 资源不存在 |
| 500 | 服务器内部错误 |

## 评分等级说明

| 等级 | 分数范围 | 说明 |
|------|----------|------|
| 甲级 | 90-100分 | 优秀 |
| 乙级 | 75-89分 | 良好 |
| 丙级 | 60-74分 | 合格 |
| 不合格 | 60分以下 | 需要改进 |

## 评分维度说明

1. **主诉** (10分)：评估主诉的完整性和规范性
2. **病史** (20分)：包括现病史、既往史、过敏史等
3. **体格检查** (15分)：评估体格检查记录的完整性
4. **辅助检查** (5分)：评估检验检查结果的完整性
5. **诊断** (15分)：评估诊断的准确性和完整性
6. **处理** (20分)：评估治疗方案的合理性
7. **总体评价** (5分)：评估病历的整体质量
8. **其他** (10分)：评估其他方面如签名、规范性等

## 调用示例

### 使用curl生成AI评分
```bash
curl -X POST "http://localhost:8080/admin-api/business/ai-score/generate" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer your-token" \
  -H "systemdeptid: 100" \
  -d '{
    "patientId": 123
  }'
```

### 使用curl查看评分报告
```bash
curl -X GET "http://localhost:8080/admin-api/business/ai-score/report/123" \
  -H "Authorization: Bearer your-token" \
  -H "systemdeptid: 100"
```

### 使用curl保存专家点评
```bash
curl -X POST "http://localhost:8080/admin-api/business/ai-score/expert-comment" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer your-token" \
  -H "systemdeptid: 100" \
  -d '{
    "patientId": 123,
    "expertComment": "病历记录较为完整，建议补充辅助检查结果。"
  }'
```

## 注意事项

1. 所有接口都支持跨域请求
2. 请求头中的Authorization和systemdeptid为可选项，用于与HMS系统集成
3. AI评分过程可能需要几秒钟时间，建议在前端显示加载状态
4. 专家点评内容不能超过100字
5. 同一患者可以重复生成评分，新的评分会覆盖旧的评分 