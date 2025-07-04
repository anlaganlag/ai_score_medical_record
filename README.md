# AI病历质控系统

基于HMS住院系统的AI病历质控功能，通过DeepSeek AI模型对病历进行自动化评分，提升医疗质量管理效率。

## 🚀 快速开始

### 环境要求
- JDK 17+
- Maven 3.6+ 或 MVND 1.0+
- MySQL 8.0+

### 快速启动

#### Windows
```bash
# 运行启动脚本
start.bat
```

#### Linux/Mac
```bash
# 运行启动脚本
./start.sh
```

#### 手动启动
```bash
# 编译项目
mvnd clean package -DskipTests

# 启动应用
java -jar target/ai-medical-score-1.0.0-SNAPSHOT.jar
```

### 配置文件
1. 复制 `application-template.yml` 为 `application-prod.yml`
2. 修改数据库连接信息
3. 配置DeepSeek API密钥
4. 配置HMS系统信息

## 📋 功能特性

### 核心功能
- ✅ **AI病历评分**: 8个维度智能评分
- ✅ **专家点评**: 医生专业点评功能
- ✅ **HMS集成**: 无缝集成HMS住院系统
- ✅ **实时报告**: 即时生成评分报告

### 评分维度
1. **主诉** (10分) - 评估主诉的完整性和规范性
2. **病史** (20分) - 包括现病史、既往史、过敏史等
3. **体格检查** (15分) - 评估体格检查记录的完整性
4. **辅助检查** (5分) - 评估检验检查结果的完整性
5. **诊断** (15分) - 评估诊断的准确性和完整性
6. **处理** (20分) - 评估治疗方案的合理性
7. **总体评价** (5分) - 评估病历的整体质量
8. **其他** (10分) - 评估其他方面如签名、规范性等

### 评分等级
- **甲级**: 90-100分 (优秀)
- **乙级**: 75-89分 (良好)  
- **丙级**: 60-74分 (合格)
- **不合格**: 60分以下 (需要改进)

## 🔧 技术架构

### 后端技术栈
- **Spring Boot 3.2.5** - 主框架
- **JDK 17** - 运行环境
- **MySQL 8** - 数据存储
- **JPA + Flyway** - 数据访问和迁移
- **WebFlux** - HTTP客户端
- **Lombok** - 代码简化

### AI服务
- **DeepSeek-R1-0528-Qwen3-8B** - AI评分模型
- **SiliconFlow API** - AI服务提供商

### 系统集成
- **HMS住院系统** - 患者数据来源
- **REST API** - 标准接口协议

## 📖 API文档

### 基础URL
```
http://localhost:8080/admin-api/business/ai-score
```

### 主要接口

#### 1. 生成AI评分
```http
POST /generate
Content-Type: application/json

{
  "patientId": 123
}
```

#### 2. 查看评分报告
```http
GET /report/{patientId}
```

#### 3. 保存专家点评
```http
POST /expert-comment
Content-Type: application/json

{
  "patientId": 123,
  "expertComment": "病历记录较为完整，建议补充辅助检查结果。"
}
```

详细API文档请参考：[API接口文档.md](API接口文档.md)

## 🛠️ 开发指南

### 项目结构
```
src/
├── main/java/com/bailun/ai/
│   ├── controller/     # REST控制器
│   ├── service/        # 业务逻辑
│   ├── entity/         # 数据实体
│   ├── dto/           # 数据传输对象
│   ├── repository/     # 数据访问
│   └── config/        # 配置类
├── main/resources/
│   ├── application.yml # 配置文件
│   └── db/migration/   # 数据库迁移
└── test/              # 测试代码
```

### 开发环境设置
1. 安装JDK 17
2. 安装Maven或MVND
3. 安装MySQL 8.0
4. 配置IDE (推荐IntelliJ IDEA)

### 代码规范
- 遵循Spring Boot最佳实践
- 使用Lombok减少样板代码
- 完整的异常处理和日志记录
- TDD测试驱动开发

## 📦 部署说明

详细部署指南请参考：[部署说明.md](部署说明.md)

### 生产环境部署
1. 配置数据库
2. 修改配置文件
3. 编译打包
4. 启动应用
5. 验证功能

### Docker部署
```bash
# 构建镜像
docker build -t ai-medical-score .

# 运行容器
docker run -p 8080:8080 ai-medical-score
```

## 🧪 测试

### 运行测试
```bash
# 运行所有测试
mvnd test

# 运行特定测试
mvnd test -Dtest=AiScoreServiceTest
```

### API测试
```bash
# Windows
test-api.bat

# Linux/Mac
./test-api.sh
```

## 📊 监控

### 健康检查
```bash
curl http://localhost:8080/actuator/health
```

### 应用指标
```bash
curl http://localhost:8080/actuator/metrics
```

## 🔒 安全

- API密钥安全存储
- 数据库连接加密
- 输入参数验证
- 错误信息脱敏

## 📝 更新日志

### v1.0.0 (2024-01-01)
- ✅ 初始版本发布
- ✅ AI病历评分功能
- ✅ HMS系统集成
- ✅ 专家点评功能
- ✅ REST API接口

## 🤝 贡献

欢迎提交Issue和Pull Request来改进项目。

## 📄 许可证

本项目采用MIT许可证 - 查看 [LICENSE](LICENSE) 文件了解详情。

## 📞 联系方式

如有问题或建议，请联系开发团队。

---

**AI病历质控系统** - 让医疗质量管理更智能、更高效！ 