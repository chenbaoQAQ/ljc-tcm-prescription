# TCM Prescription Service

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

## 🛠️ 环境要求

*   **JDK**: 17
*   **Maven**: 3.6+ (或使用内置 `./mvnw`)
*   **Docker**: 可选，推荐用于快速启动 MySQL
*   **MySQL**: 8.x

---

## 🚀 快速启动 (Quick Start)

为方便前端联调，提供了 Docker Compose 一键启动环境。

### 1. 启动数据库 (推荐)

进入 `ljc-tcm-prescription-service` 目录：

```bash
docker-compose up -d
```

这会自动启动 MySQL 8，并导入 `sql/schema.sql` 初始化表结构。
*   端口映射：`3306:3306`
*   账号：`root`
*   密码：`root`
*   数据库：`tcm_prescription`

### 2. 启动后端

```bash
# Mac/Linux
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev

# Windows
mvnw.cmd spring-boot:run -Dspring-boot.run.profiles=dev
```

默认配置 (`application-dev.yml`) 已经预设连接 Docker 的 MySQL (此时密码为 root)。

### 3. Swagger 接口文档

服务启动成功后，访问：
👉 **http://localhost:8081/swagger-ui.html**

---

## ✅ 如何验证 (Smoke Test)

我们提供了一个脚本，可以在**不打开前端页面、不安装 Postman** 的情况下，一键验证核心流程：

```bash
./scripts/smoke-test.sh
```

该脚本会：
1. 创建 3 个药材 (A, B, C)
2. 创建 2 个药方 (P1, P2)
3. 执行 Merge 操作
4. 输出结果供检查 (应显示 SMOKE TEST PASSED)

---

## 🔌 核心接口说明 (Core APIs)

Base Path: `/api/v1`

| 资源 | 方法 | 路径 | 描述 |
| :--- | :--- | :--- | :--- |
| **Herbs** | GET | `/herbs` | 列表查询 (支持 keyword) |
| | POST | `/herbs` | 新增药材 |
| | PUT | `/herbs/{id}` | 修改药材 |
| | DELETE| `/herbs/{id}` | 软删除 |
| **Prescriptions** | POST | `/prescriptions` | 新增药方 (含 items) |
| | POST | `/prescriptions/merge` | **合并药方** (核心) |

**合并规则说明**：
请求体：`{ "prescriptionIds": [1, 2] }`
如果 药方1 含 黄芪 10g，药方2 含 黄芪 15g。
合并结果为 **黄芪 15g** (取 MAX，不相加)。

---

## 🔐 配置文件说明

敏感配置不会提交到 git。
*   `application.yml`: 通用配置
*   `application-dev.yml`: 本地开发配置 (已加入 .ignore)
*   `application-dev.example.yml`:配置示例 (ENV 变量版)

如果你不使用 Docker，需连接自己的 MySQL，请修改 `application-dev.yml` (或新建) 覆盖 `DB_USER` / `DB_PASS`。

---

## 📂 目录结构

```
ljc-tcm-prescription-service
├── src
│   └── main/java/com/tcm/prescription
│       ├── controller   # 接口
│       ├── service      # 业务逻辑 (Merge 逻辑在这里)
│       └── entity       # 数据库实体
├── sql
│   └── schema.sql       # 建表脚本 (Docker会自动挂载)
├── scripts
│   └── smoke-test.sh    # 一键验证脚本
├── docker-compose.yml   # MySQL 容器配置
└── pom.xml
```
