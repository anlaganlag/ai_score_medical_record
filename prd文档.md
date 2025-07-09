# AI病历质控系统MVP功能 PRD文档

## 1. 项目概述

### 1.1 项目背景
基于HMS住院系统，集成AI病历质控功能，通过调用DeepSeek AI模型对病历进行自动化评分，提升医疗质量管理效率。

### 1.2 项目目标
- 实现病历自动化评分功能
- 提供专家点评机制
- 集成到现有HMS系统中
- 提升病历质量管理效率

### 1.3 项目范围
- 后端：SpringBoot 3.2.5 + MySQL 8
- 前端：Vue 2
- AI模型：DeepSeek-R1-0528-Qwen3-8B
- 开发方式：TDD（测试驱动开发）

## 2. 需求分析

### 2.1 功能需求

#### 2.1.1 核心功能
1. **病历数据获取**
   - 从HMS系统获取患者基本信息
   - 获取患者诊疗信息
   - 数据整合和映射

2. **AI病历评分**
   - 调用DeepSeek AI模型进行病历评分
   - 生成结构化评分报告
   - 支持8个评分维度

3. **专家点评**
   - 支持专家手动点评
   - 点评内容存储和展示
   - 点评字数限制（100字）

4. **报告查看**
   - 评分报告展示
   - 专家点评展示
   - 弹窗形式展示

#### 2.1.2 用户角色
- **医生**：可以触发AI评分，查看评分报告，进行专家点评

### 2.2 非功能需求

#### 2.2.1 性能需求
- 支持5个并发用户
- 异步处理AI评分请求
- API调用频率：RPM 1,000，TPM 50,000

#### 2.2.2 可靠性需求
- DeepSeek API调用失败重试1次
- 完整的错误日志记录
- 数据完整性保证

#### 2.2.3 扩展性需求
- 支持未来版本的历史记录功能
- 数据库设计考虑扩展性

## 3. 技术架构

### 3.1 系统架构图
```
前端(Vue2) → 后端(SpringBoot) → HMS API
                    ↓
              DeepSeek AI API
                    ↓
               MySQL数据库
```

### 3.2 技术栈
- **后端框架**：Spring Boot 3.2.5
- **Java版本**：JDK 17
- **数据库**：MySQL 8
- **缓存**：Redisson + Redis
- **认证**：Sa-Token
- **工具库**：Lombok, Hutool
- **测试框架**：JUnit 5, Mockito

### 3.3 外部依赖
- **HMS API**：http://172.16.1.13:10086
- **DeepSeek API**：https://api.siliconflow.cn/v1/chat/completions
- **数据库**：172.16.1.15:3306

## 4. 数据库设计

### 4.1 主表设计

#### 4.1.1 AI评分记录表 (ai_score_medical_record)
```sql
CREATE TABLE ai_score_medical_record (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    patient_id BIGINT NOT NULL COMMENT '患者ID',
    patient_name VARCHAR(100) COMMENT '患者姓名',
    score_result JSON COMMENT '评分结果JSON',
    expert_comment TEXT COMMENT '专家点评',
    score_total INT COMMENT '总分',
    score_level VARCHAR(20) COMMENT '评分等级(甲级/乙级/丙级)',
    ai_model VARCHAR(100) COMMENT 'AI模型名称',
    created_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    created_by BIGINT COMMENT '创建人ID',
    updated_by BIGINT COMMENT '更新人ID',
    deleted BOOLEAN DEFAULT FALSE COMMENT '是否删除',
    version INT DEFAULT 1 COMMENT '版本号(预留扩展)',
    INDEX idx_patient_id (patient_id),
    INDEX idx_created_time (created_time)
) COMMENT='AI病历评分记录表';
```

## 5. API设计

### 5.1 后端API接口

#### 5.1.1 生成/更新评分报告
```
POST /admin-api/business/ai-score/generate
Content-Type: application/json
Authorization: Bearer {token}
systemdeptid: {deptId}

Request Body:
{
    "patientId": 3835
}

Response:
{
    "code": 0,
    "data": {
        "id": 1,
        "patientId": 3835,
        "status": "processing" // processing/completed/failed
    },
    "msg": "评分任务已提交"
}
```

#### 5.1.2 查看评分报告
```
GET /admin-api/business/ai-score/report/{patientId}
Authorization: Bearer {token}
systemdeptid: {deptId}

Response:
{
    "code": 0,
    "data": {
        "id": 1,
        "patientId": 3835,
        "patientName": "胡安秀",
        "scoreResult": {
            "totalScore": 70,
            "level": "乙级",
            "details": [
                {
                    "item": "主诉",
                    "fullScore": 10,
                    "score": 8,
                    "deduction": "描述有缺陷 扣2分：主诉为'维持性血液透析13年余'，是对治疗状态的概括，不符合'主要症状+时间'的规范。"
                }
                // ... 其他评分项
            ]
        },
        "expertComment": "专家点评内容",
        "createdTime": "2024-01-01 10:00:00",
        "updatedTime": "2024-01-01 10:30:00"
    },
    "msg": ""
}
```

#### 5.1.3 保存专家点评
```
POST /admin-api/business/ai-score/expert-comment
Content-Type: application/json
Authorization: Bearer {token}
systemdeptid: {deptId}

Request Body:
{
    "patientId": 3835,
    "expertComment": "专家点评内容"
}

Response:
{
    "code": 0,
    "data": true,
    "msg": "专家点评保存成功"
}
```

### 5.2 HMS API调用

#### 5.2.1 获取患者信息
```
GET http://172.16.1.13:10086/admin-api/business/outpatient-blood/get?patientId=3835
Authorization: Bearer 97285ff9-240a-4546-9f14-a9b858397716
systemdeptid: 127
```

#### 5.2.2 获取诊疗信息
```
POST http://172.16.1.13:10086/admin-api/business/first-course-record/get
Content-Type: application/json
Authorization: Bearer 97285ff9-240a-4546-9f14-a9b858397716
systemdeptid: 127

Request Body:
{
    "patientId": 3835
}
```

### 5.3 DeepSeek AI API调用

#### 5.3.1 AI评分请求
```
POST https://api.siliconflow.cn/v1/chat/completions
Content-Type: application/json
Authorization: Bearer sk-stwdcstqnkcztrsqsrmosrcpekiacrualyzzvsgfmzjvuurv

Request Body:
{
    "model": "deepseek-ai/DeepSeek-R1-0528-Qwen3-8B",
    "messages": [
        {
            "role": "system",
            "content": "你是一个专业的医疗质控专家，需要根据病历信息进行评分..."
        },
        {
            "role": "user",
            "content": "请对以下病历进行评分：{病历数据JSON}"
        }
    ],
    "temperature": 0.1,
    "max_tokens": 4000
}
```

## 6. 数据映射关系

### 6.1 AI评分8个维度数据映射

#### 6.1.1 主诉 (10分)
- **数据来源**：诊疗信息API
- **字段映射**：
  - `mainSuit` → 主诉内容

#### 6.1.2 病史 (20分)
- **数据来源**：患者信息API + 诊疗信息API
- **字段映射**：
  - `allergy` → 过敏史
  - `tumor` → 肿瘤史
  - `smoking` → 吸烟史
  - `tipple` → 饮酒史
  - `infect` → 传染病史
  - `dialyzeAge` → 透析龄
  - `firstReceiveTime` → 首次透析时间
  - `nowDiseaseHistory` → 现病史
  - `oldDiseaseHistory` → 既往史

#### 6.1.3 体格检查 (15分)
- **数据来源**：患者信息API + 诊疗信息API
- **字段映射**：
  - `stature` → 身高
  - `initWeight` → 初始体重
  - `weightPre` → 透析前体重
  - `pressurePre` → 透析前血压
  - `eyesight` → 视力
  - `bodyInspect` → 体格检查详情

#### 6.1.4 辅助检查 (5分)
- **数据来源**：患者信息API + 诊疗信息API
- **字段映射**：
  - `bloodType` → 血型
  - `rh` → Rh血型
  - `erythrocyte` → 红细胞
  - `assayInspect` → 检验结果

#### 6.1.5 诊断 (15分)
- **数据来源**：患者信息API + 诊疗信息API
- **字段映射**：
  - `diagnosis` → 诊断
  - `diseaseReasonNames` → 原发病
  - `initDiagnosis` → 初步诊断

#### 6.1.6 处理 (20分)
- **数据来源**：患者信息API + 诊疗信息API
- **字段映射**：
  - `dialysisPlan` → 透析计划
  - `healingProject` → 治疗方案
  - `medic` → 医生
  - `nurse` → 护士
  - `facilityName` → 设备名称
  - `startDialyzeTime` → 开始透析时间
  - `endDialyzeTime` → 结束透析时间

#### 6.1.7 总体评价 (5分)
- **数据来源**：患者信息API
- **字段映射**：
  - `state` → 患者状态
  - `signature` → 签名信息

#### 6.1.8 其他 (10分)
- **数据来源**：患者信息API
- **字段映射**：
  - 基本信息完整性
  - 记录规范性评估

## 7. 前端设计

### 7.1 页面集成

#### 7.1.1 按钮集成位置
- **文件路径**：`hms-pc\src\views\components\patientInfo\index.vue`
- **集成位置**：患者管理-基本信息的右上角
- **按钮样式**：warning类型，醒目直观

#### 7.1.2 按钮功能
1. **AI病历质控生成/更新**
   - 触发AI评分任务
   - 显示处理状态
   - 异步处理提示

2. **AI病历质控报告查看**
   - 查看评分报告
   - 弹窗展示
   - 专家点评功能

### 7.2 弹窗设计

#### 7.2.1 评分报告展示
- **展示内容**：
  - 总分和等级
  - 8个维度详细评分
  - 扣分说明
  - 改进建议

#### 7.2.2 专家点评区域
- **输入框**：文本域，限制100字
- **操作按钮**：取消、确认
- **实时字数统计**

## 8. 开发计划

### 8.1 Phase 1 - 核心功能开发 (3天)

#### Day 1: 基础架构
- [ ] 数据库表创建和初始化
- [ ] 基础Service和Repository层
- [ ] HMS API调用服务
- [ ] 单元测试框架搭建

#### Day 2: AI集成
- [ ] DeepSeek AI调用服务
- [ ] 数据映射和整合逻辑
- [ ] AI评分核心业务逻辑
- [ ] 异步处理机制

#### Day 3: API接口
- [ ] 评分生成API
- [ ] 报告查看API
- [ ] 专家点评API
- [ ] 接口测试

### 8.2 Phase 2 - 前端集成 (2天)

#### Day 4: 前端开发
- [ ] 按钮集成到患者信息页面
- [ ] 评分报告弹窗组件
- [ ] 专家点评功能
- [ ] 状态提示和加载效果

#### Day 5: 联调测试
- [ ] 前后端联调
- [ ] 功能测试
- [ ] 用户体验优化

### 8.3 Phase 3 - 完善优化 (1天)

#### Day 6: 测试和部署
- [ ] 完整的单元测试
- [ ] 集成测试
- [ ] 错误处理完善
- [ ] 部署和上线

## 9. 测试策略

### 9.1 测试数据
- **主要测试患者ID**：3835
- **其他测试ID**3945, 4360, 4913, 4376

### 9.2 测试用例
1. **正常流程测试**
   - 完整病历数据评分
   - 专家点评保存
   - 报告查看

2. **异常情况测试**
   - HMS API调用失败
   - DeepSeek API调用失败
   - 数据不完整情况

3. **边界测试**
   - 专家点评字数限制
   - 并发用户测试
   - 长时间处理测试

### 9.3 TDD开发流程
1. 编写测试用例
2. 运行测试（失败）
3. 编写最小可行代码
4. 运行测试（通过）
5. 重构代码
6. 重复循环

## 10. 部署配置

### 10.1 环境配置
- **开发环境**：172.16.1.15:3306
- **生产环境**：172.16.1.13:3306
- **数据库**：bailun

### 10.2 配置参数
```yaml
# application.yml
ai:
  deepseek:
    api-url: https://api.siliconflow.cn/v1/chat/completions
    api-key: sk-stwdcstqnkcztrsqsrmosrcpekiacrualyzzvsgfmzjvuurv
    model: deepseek-ai/DeepSeek-R1-0528-Qwen3-8B
    retry-count: 1
    timeout: 30000
  
  hms:
    api-url: http://172.16.1.13:10086
    token: 97285ff9-240a-4546-9f14-a9b858397716
    systemdeptid: 127
```

## 11. 风险评估

### 11.1 技术风险
- **外部API依赖**：HMS和DeepSeek API的稳定性
- **数据质量**：病历数据的完整性和准确性
- **性能问题**：AI模型调用延迟

### 11.2 缓解措施
- 实现重试机制
- 完善错误处理
- 异步处理提升用户体验
- 详细的日志记录

## 12. 验收标准

### 12.1 功能验收
- [ ] 能够成功调用HMS API获取病历数据
- [ ] 能够成功调用DeepSeek AI进行评分
- [ ] 评分报告格式正确，包含8个维度
- [ ] 专家点评功能正常
- [ ] 前端页面集成无误

### 12.2 性能验收
- [ ] 支持5个并发用户
- [ ] API响应时间在合理范围内
- [ ] 异步处理机制正常工作

### 12.3 质量验收
- [ ] 单元测试覆盖率达到80%以上
- [ ] 集成测试通过
- [ ] 错误处理机制完善
- [ ] 日志记录完整

---

**文档版本**：v1.0  
**创建日期**：2024-01-01  
**最后更新**：2024-01-01
