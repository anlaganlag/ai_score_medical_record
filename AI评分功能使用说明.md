# AI评分功能使用说明

## 功能概述

AI医疗质控评分系统为医疗机构提供智能化的病历质控评分服务，通过人工智能技术自动分析患者病历，生成客观的质量评分报告，并支持专家进行点评和优化。

## 主要特性

### ✅ 核心功能
- **智能评分生成**: 基于AI算法自动分析病历并生成综合评分
- **多维度评价**: 从用药合理性、治疗方案、监测指标等多个维度进行评分
- **专家点评系统**: 支持专家对AI评分结果进行点评和优化
- **权限控制**: 基于用户角色的权限管控，确保数据安全
- **多报告支持**: 同时展示最多3个评分报告，便于对比分析

### ✅ 技术特性
- **响应式设计**: 适配各种屏幕尺寸
- **实时更新**: 支持实时生成和查看评分报告
- **错误处理**: 完善的错误提示和异常处理机制
- **性能优化**: 采用懒加载和缓存策略提升用户体验

## 组件结构

```
src/
├── api/
│   └── aiScore.js                 # AI评分API接口
├── components/
│   └── AiScore/
│       ├── AiScoreButton.vue      # AI评分按钮组件
│       ├── AiScoreDialog.vue      # AI评分弹窗组件
│       └── ScoreReportCard.vue    # 评分报告卡片组件
└── views/
    └── patient/
        └── PatientDetail.vue      # 患者详情页面示例
```

## 使用方法

### 1. 基本集成

在患者详情页面中集成AI评分功能：

```vue
<template>
  <div class="patient-detail">
    <!-- 其他内容 -->
    
    <!-- AI评分按钮 -->
    <ai-score-button
      :patient-id="patientInfo.id"
      :patient-info="patientInfo"
      @score-generated="handleScoreGenerated"
      @view-scores="handleViewScores"
    />

    <!-- AI评分弹窗 -->
    <ai-score-dialog
      :visible.sync="scoreDialogVisible"
      :patient-id="patientInfo.id"
      :patient-info="patientInfo"
      :initial-score-data="newScoreData"
    />
  </div>
</template>

<script>
import AiScoreButton from '@/components/AiScore/AiScoreButton.vue'
import AiScoreDialog from '@/components/AiScore/AiScoreDialog.vue'

export default {
  components: {
    AiScoreButton,
    AiScoreDialog
  },
  data() {
    return {
      patientInfo: {
        id: 1001,
        name: '患者姓名',
        gender: '男',
        age: 65,
        // ... 其他患者信息
      },
      scoreDialogVisible: false,
      newScoreData: null
    }
  },
  methods: {
    handleScoreGenerated(data) {
      this.newScoreData = data.scoreData
      this.scoreDialogVisible = true
    },
    
    handleViewScores() {
      this.newScoreData = null
      this.scoreDialogVisible = true
    }
  }
}
</script>
```

### 2. 权限配置

在系统中配置相应的权限标识：

```javascript
// 权限配置示例
const permissions = [
  'ai:score:generate',    // 生成AI评分权限
  'ai:score:view',        // 查看评分报告权限
  'ai:score:comment',     // 专家点评权限
  'ai:score:delete'       // 删除评分记录权限
]
```

### 3. API接口对接

确保后端提供以下API接口：

```javascript
// 生成AI评分
POST /ai-score/generate
{
  "patientId": 1001,
  "recordType": "comprehensive",
  "patientInfo": { ... }
}

// 获取评分列表
GET /ai-score/list?patientId=1001

// 保存专家点评
POST /ai-score/expert-comment
{
  "scoreId": "score123",
  "comment": "专家点评内容",
  "patientId": 1001
}
```

## API接口规范

### 生成AI评分

**接口地址**: `POST /ai-score/generate`

**请求参数**:
```json
{
  "patientId": 1001,
  "recordType": "comprehensive",
  "patientInfo": {
    "name": "患者姓名",
    "gender": "男",
    "age": 65,
    "hospitalId": "H202400001",
    "diagnosis": "主要诊断"
  }
}
```

**响应格式**:
```json
{
  "code": 200,
  "msg": "生成成功",
  "data": {
    "scoreId": "score123",
    "patientId": 1001,
    "score": 85,
    "recordType": "comprehensive",
    "createTime": "2024-01-17T10:30:00",
    "details": {
      "medication": 88,
      "treatment": 82,
      "monitoring": 85,
      "nursing": 90,
      "safety": 87,
      "documentation": 80
    },
    "suggestions": [
      "建议优化用药方案",
      "加强患者监测指标记录",
      "完善护理文档"
    ],
    "expertComment": null
  }
}
```

### 获取评分列表

**接口地址**: `GET /ai-score/list`

**请求参数**: `patientId=1001`

**响应格式**:
```json
{
  "code": 200,
  "msg": "获取成功",
  "data": [
    {
      "scoreId": "score123",
      "patientId": 1001,
      "score": 85,
      "recordType": "comprehensive",
      "createTime": "2024-01-17T10:30:00",
      "expertComment": "专家点评内容"
    }
  ]
}
```

### 保存专家点评

**接口地址**: `POST /ai-score/expert-comment`

**请求参数**:
```json
{
  "scoreId": "score123",
  "comment": "专家点评内容",
  "patientId": 1001
}
```

**响应格式**:
```json
{
  "code": 200,
  "msg": "保存成功",
  "data": null
}
```

## 组件属性说明

### AiScoreButton 组件

| 属性名 | 类型 | 必填 | 默认值 | 说明 |
|--------|------|------|--------|------|
| patientId | String/Number | 是 | - | 患者ID |
| patientInfo | Object | 否 | {} | 患者信息对象 |

**事件**:
- `score-generated`: 评分生成成功时触发
- `view-scores`: 查看评分报告时触发

### AiScoreDialog 组件

| 属性名 | 类型 | 必填 | 默认值 | 说明 |
|--------|------|------|--------|------|
| visible | Boolean | 是 | false | 弹窗显示状态 |
| patientId | String/Number | 是 | - | 患者ID |
| patientInfo | Object | 否 | {} | 患者信息对象 |
| initialScoreData | Object | 否 | null | 初始评分数据 |

### ScoreReportCard 组件

| 属性名 | 类型 | 必填 | 默认值 | 说明 |
|--------|------|------|--------|------|
| report | Object | 是 | - | 评分报告数据 |
| patientInfo | Object | 否 | {} | 患者信息对象 |

**事件**:
- `comment-updated`: 专家点评更新时触发

## 错误处理

系统提供完善的错误处理机制：

### 常见错误类型

1. **权限不足 (403)**
   - 提示: "您没有权限执行此操作"
   - 处理: 隐藏相关按钮或功能

2. **数据不完整 (422)**
   - 提示: "患者病历信息不完整，无法生成评分"
   - 处理: 引导用户完善患者信息

3. **网络异常 (500/网络错误)**
   - 提示: "网络异常，请稍后重试"
   - 处理: 提供重试机制

4. **业务逻辑错误**
   - 提示: 根据后端返回的具体错误信息
   - 处理: 显示具体的业务错误提示

### 错误处理示例

```javascript
try {
  const response = await generateAiScore(requestData)
  // 处理成功逻辑
} catch (error) {
  if (error.response && error.response.status === 422) {
    this.$modal.msgError('患者病历信息不完整，无法生成评分')
  } else if (error.response && error.response.status === 403) {
    this.$modal.msgError('您没有权限执行此操作')
  } else {
    this.$modal.msgError('网络异常，请稍后重试')
  }
}
```

## 样式定制

### 主题颜色配置

可以通过修改 SCSS 变量来定制主题颜色：

```scss
// 主要色彩
$primary-color: #1890ff;
$success-color: #52c41a;
$warning-color: #faad14;
$danger-color: #f5222d;

// 评分等级颜色
$excellent-color: #52c41a;  // 优秀 (90+)
$good-color: #1890ff;       // 良好 (80-89)
$fair-color: #faad14;       // 合格 (70-79)
$poor-color: #f5222d;       // 不合格 (<70)
```

### 组件样式覆盖

```scss
// 自定义按钮样式
.ai-score-button {
  .el-button--primary {
    background-color: #1890ff;
    border-color: #1890ff;
    
    &:hover {
      background-color: #40a9ff;
      border-color: #40a9ff;
    }
  }
}

// 自定义弹窗样式
.ai-score-dialog {
  .el-dialog {
    border-radius: 8px;
  }
}
```

## 性能优化建议

### 1. 懒加载
```javascript
// 路由懒加载
const PatientDetail = () => import('@/views/patient/PatientDetail.vue')
```

### 2. 组件懒加载
```javascript
// 组件异步加载
components: {
  AiScoreButton: () => import('@/components/AiScore/AiScoreButton.vue'),
  AiScoreDialog: () => import('@/components/AiScore/AiScoreDialog.vue')
}
```

### 3. 数据缓存
```javascript
// 使用 keep-alive 缓存组件状态
<keep-alive>
  <ai-score-dialog v-if="scoreDialogVisible" />
</keep-alive>
```

## 部署说明

### 1. 依赖安装
```bash
npm install
```

### 2. 开发环境启动
```bash
npm run dev
```

### 3. 生产环境构建
```bash
npm run build
```

### 4. 环境变量配置
```bash
# .env.development
VUE_APP_BASE_API = 'http://localhost:8080'

# .env.production
VUE_APP_BASE_API = 'https://your-api-domain.com'
```

## 常见问题

### Q1: AI评分按钮不显示？
**A**: 检查用户是否有 `ai:score:generate` 权限，确认 `v-hasPermi` 指令正常工作。

### Q2: 评分生成失败？
**A**: 
1. 检查患者信息是否完整
2. 确认后端API接口正常
3. 查看网络请求是否成功

### Q3: 专家点评无法保存？
**A**: 
1. 检查用户是否有 `ai:score:comment` 权限
2. 确认点评内容不为空
3. 检查API接口调用是否正常

### Q4: 弹窗样式异常？
**A**: 
1. 检查Element UI版本兼容性
2. 确认SCSS编译正常
3. 查看样式覆盖是否生效

## 联系支持

如果在使用过程中遇到问题，请联系开发团队或查看项目文档。

---

**版本**: v1.0.0  
**更新时间**: 2024-01-17  
**维护人员**: 前端开发团队 