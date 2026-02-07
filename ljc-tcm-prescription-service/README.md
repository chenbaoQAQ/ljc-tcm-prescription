# 中药方剂管理服务 (TCM Prescription Service)

这是一个用于中药开方小程序的核心后端服务，基于 Spring Boot 2.7.18 和 MySQL 8 构建。

## 🎯 核心功能

1.  **药材库管理 (Herb Library)**
    *   药材的增删改查 (CRUD)。
    *   名字唯一性校验。
    *   软删除支持 (`deleted_at`)。
2.  **药方管理 (Prescription Management)**
    *   药方及明细的增删改查。
    *   支持药方内的全量更新（覆盖模式）。
    *   基于快照保存药材名。
3.  **多药方合并 (Smart Merge)** 🔥
    *   支持选择多个药方合并为一个总清单。
    *   **核心逻辑**：同名药材取**最大克重 (Max Dose)**，而非累加。
    *   输出包含溯源信息（即该克重来自哪个方子）。

---

## 🛠️ 技术栈

*   **JDK**: 17
*   **Framework**: Spring Boot 2.7.18
*   **Database**: MySQL 8.x
*   **Build Tool**: Maven
*   **ORM**: Spring Data JPA
*   **Validation**: Hibernate Validator
*   **Doc**: SpringDoc OpenAPI (Swagger UI)

---

## 🚀 快速启动

### 1. 数据库准备

请先连接到你的 MySQL 数据库，执行项目根目录下的 `schema.sql` 脚本。
**注意**：脚本会先**删除**同名数据库（如果存在），然后重新创建。

*   **数据库名**: `ljc_tcm_prescription`
*   **账号**: `root` (根据你的配置修改)
*   **密码**: `020222`

```bash
# 命令行示例
mysql -u root -p020222 < schema.sql
```

或者在数据库客户端中复制 `schema.sql` 内容执行。

### 2.配置说明

默认配置文件位于 `src/main/resources/application-dev.yml`。

关键配置项已预设：

```yaml
server:
  port: 8081 # 端口号修改为 8081

spring:
  datasource:
    url: jdbc:mysql://localhost:3306/ljc_tcm_prescription?...
    password: ${DB_PASS:020222} # 默认密码 020222
```

如果你的环境密码不同，可以通过环境变量覆盖：

```bash
export DB_PASS=your_password
```

### 3. 运行服务

使用 Maven Wrapper 启动：

```bash
# Mac/Linux
./mvnw spring-boot:run

# Windows
mvnw.cmd spring-boot:run
```

启动成功后，访问 Swagger 文档：
👉 **http://localhost:8081/swagger-ui.html**

---

## 🧪 接口测试示例 (cURL)

以下示例基于默认端口 `8081`。

### 1. 新增药材 (Create Herb)

```bash
curl -X POST http://localhost:8081/api/v1/herbs \
  -H "Content-Type: application/json" \
  -d '{
    "nameCn": "黄芪",
    "defaultDoseG": 10.0,
    "notes": "补气固表",
    "status": 1
  }'
```

```bash
curl -X POST http://localhost:8081/api/v1/herbs \
  -H "Content-Type: application/json" \
  -d '{
    "nameCn": "当归",
    "defaultDoseG": 5.0,
    "notes": "补血活血"
  }'
```

### 2. 新增药方 (Create Prescription)

**药方 A** (黄芪 15g + 当归 5g):

```bash
curl -X POST http://localhost:8081/api/v1/prescriptions \
  -H "Content-Type: application/json" \
  -d '{
    "name": "补气方A",
    "items": [
      { "herbId": 1, "doseG": 15.0 },
      { "herbId": 2, "doseG": 5.0 }
    ]
  }'
```

**药方 B** (黄芪 10g + 当归 10g):

```bash
curl -X POST http://localhost:8081/api/v1/prescriptions \
  -H "Content-Type: application/json" \
  -d '{
    "name": "调理方B",
    "items": [
      { "herbId": 1, "doseG": 10.0 },
      { "herbId": 2, "doseG": 10.0 }
    ]
  }'
```

### 3. 多药方合并 (Merge)

请求合并 药方A (ID=1) 和 药方B (ID=2)。
预期结果：
*   黄芪：取 max(15, 10) = **15g**
*   当归：取 max(5, 10) = **10g**

```bash
curl -X POST http://localhost:8081/api/v1/prescriptions/merge \
  -H "Content-Type: application/json" \
  -d '{
    "prescriptionIds": [1, 2]
  }'
```

**响应示例**:

```json
{
  "code": 0,
  "message": "success",
  "data": {
    "items": [
      {
        "herbId": 2,
        "name": "当归",
        "doseG": 10.00,
        "sources": [
          { "prescriptionId": 1, "doseG": 5.00 },
          { "prescriptionId": 2, "doseG": 10.00 }
        ]
      },
      {
        "herbId": 1,
        "name": "黄芪",
        "doseG": 15.00,
        "sources": [
          { "prescriptionId": 1, "doseG": 15.00 },
          { "prescriptionId": 2, "doseG": 10.00 }
        ]
      }
    ]
  },
  "traceId": "..."
}
```

---

## 📂 工程结构

```
src/main/java/com/tcm/prescription
├── common       # 通用结果封装 Result, ErrorCode
├── config       # Swagger及过滤器配置
├── controller   # 接口层
├── dto          # 数据传输对象 (Request/Response)
├── entity       # 数据库实体 (JPA)
├── exception    # 全局异常处理
├── repository   # 数据访问层 (DAO)
└── service      # 业务逻辑层
```

## ⚠️ 注意事项

*   所有克重单位均为 **g (克)**，数据库存储类型为 `DECIMAL(10,2)`。
*   删除操作均为**软删除**，数据保留在数据库中，字段 `deleted_at` 不为空。
*   多方合并时，如果某药材在多个方子中出现，**只取最大值**，不会累加。

