# 🚀 部署指南

## 📋 环境要求

- **Java**: 11 或更高版本
- **MySQL**: 5.7 或更高版本
- **Maven**: 3.6+ （或使用项目自带的 `./mvnw`）

---

## 📦 快速部署步骤

### 1️⃣ 克隆项目

```bash
git clone https://github.com/chenbaoQAQ/ljc-tcm-prescription.git
cd ljc-tcm-prescription
```

### 2️⃣ 安装并配置 MySQL

#### macOS
```bash
# 安装 MySQL
brew install mysql
brew services start mysql

# 登录 MySQL（首次可能无密码，直接回车）
mysql -u root -p

# 创建数据库
CREATE DATABASE ljc_tcm_prescription CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

# 设置密码（如果没有）
ALTER USER 'root'@'localhost' IDENTIFIED BY '你的密码';

exit;
```

#### Windows
```bash
# 下载并安装 MySQL：https://dev.mysql.com/downloads/mysql/

# 登录 MySQL
mysql -u root -p

# 创建数据库
CREATE DATABASE ljc_tcm_prescription CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
exit;
```

### 3️⃣ 初始化数据库

```bash
cd ljc-tcm-prescription-service

# 初始化数据库表结构
mysql -u root -p ljc_tcm_prescription < src/main/resources/schema.sql
```

### 4️⃣ 配置数据库密码

**方法1：直接修改配置文件（简单）**

编辑 `src/main/resources/application-dev.yml`：

```yaml
spring:
  datasource:
    username: root
    password: 你的MySQL密码  # ← 在这里填写你的密码
```

**方法2：使用环境变量（推荐）**

```bash
# macOS/Linux
export DB_PASS=你的MySQL密码

# Windows (PowerShell)
$env:DB_PASS="你的MySQL密码"
```

### 5️⃣ 启动后端服务

```bash
cd ljc-tcm-prescription-service

# 使用 Maven 启动
./mvnw spring-boot:run

# 或者在 IDEA 中直接运行 TcmPrescriptionApplication
```

启动成功后，控制台会显示：
```
Started TcmPrescriptionApplication in X.XXX seconds
```

### 6️⃣ 访问应用

- **本机访问**: `http://localhost:8081/index.html`
- **手机访问**: `http://你的电脑IP:8081/index.html`

**查看本机 IP:**
```bash
# macOS/Linux
ifconfig | grep "inet " | grep -v 127.0.0.1

# Windows
ipconfig
```

---

## 📱 移动端访问

### 前提条件
- 手机和电脑在**同一个局域网**（WiFi）
- 电脑防火墙允许 8081 端口访问

### 步骤
1. 查看电脑 IP（比如 `192.168.1.13`）
2. 在手机浏览器输入：`http://192.168.1.13:8081/index.html`
3. 添加到手机主屏幕，像 App 一样使用

---

## 🔧 常见问题

### 1. 端口 8081 被占用
```bash
# 查看占用端口的进程
lsof -ti:8081

# 杀死进程
lsof -ti:8081 | xargs kill -9
```

### 2. 数据库连接失败
- 确认 MySQL 服务已启动：`brew services list` (macOS)
- 检查密码是否正确
- 确认数据库 `ljc_tcm_prescription` 已创建

### 3. 手机无法访问
- 确认手机和电脑在同一 WiFi
- 检查电脑防火墙设置
- 尝试关闭 VPN

---

## 📚 API 文档

启动服务后访问：
- Swagger UI: `http://localhost:8081/swagger-ui.html`
- API Docs: `http://localhost:8081/v3/api-docs`

---

## 🎯 功能概览

- ✅ **药材库管理** - 添加、删除药材
- ✅ **药方管理** - 创建、编辑、删除药方
- ✅ **开病历** - 选择多个药方，自动合并药材
- ✅ **病历历史** - 查看、搜索、删除病历记录
- ✅ **备注功能** - 为病历添加备注信息
- ✅ **复制功能** - 一键复制药材清单

---

## 📞 技术支持

遇到问题？请提交 [GitHub Issue](https://github.com/chenbaoQAQ/ljc-tcm-prescription/issues)
