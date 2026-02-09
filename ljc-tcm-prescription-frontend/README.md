# TCM 病历助手 - 微信小程序

> 为妈妈设计的中医病历管理微信小程序

## 功能特性

### ✅ 核心功能（P0）
- **开病历** - 选择患者、日期、多个药方，自动生成总药材清单
- **病历历史** - 按姓名搜索病历，查看药材需求
- **一键复制** - 复制药材清单方便使用

### ✅ 辅助功能（P1）
- **药方管理** - 新建/编辑/删除药方
- **药材库** - 添加/删除药材

## 技术栈

- 微信小程序原生开发（WXML + WXSS + JS）
- 无第三方框架依赖
- RESTful API 对接后端 SpringBoot 服务

## 快速开始

### 1. 前置准备

**安装微信开发者工具**
- 下载：https://developers.weixin.qq.com/miniprogram/dev/devtools/download.html

**确保后端服务已启动**
- 项目：`ljc-tcm-prescription-service`
- 端口：默认 8081

### 2. 配置后端地址

修改 `config/env.js`：

```javascript
module.exports = {
  // 改成你的电脑局域网IP（通过 ifconfig 或 ipconfig 查看）
  BASE_URL: 'http://192.168.1.7:8081'
};
```

**如何获取局域网IP：**
- Mac/Linux: 终端运行 `ifconfig | grep "inet "` 
- Windows: 命令行运行 `ipconfig`
- 找到类似 `192.168.x.x` 的地址

### 3. 导入项目

1. 打开微信开发者工具
2. 选择"导入项目"
3. 项目目录：选择 `ljc-tcm-prescription-frontend`
4. AppID：选择"测试号"（或使用自己的）
5. 点击"导入"

### 4. 配置不校验域名（重要！）

因为开发阶段使用 `http://` 而非 `https://`：

1. 微信开发者工具右上角：点击"详情"
2. "本地设置"标签页
3. 勾选：**不校验合法域名、web-view（业务域名）、TLS版本以及HTTPS证书**

### 5. 编译运行

点击左上角"编译"按钮即可！

## 项目结构

```
ljc-tcm-prescription-frontend/
├── config/
│   └── env.js              # 环境配置（后端地址）
├── utils/
│   ├── request.js          # 统一请求封装
│   └── date.js             # 日期工具
├── api/
│   ├── herb.js             # 药材API
│   ├── prescription.js     # 药方API
│   └── medical.js          # 病历API
├── pages/
│   ├── home/               # 首页（3个入口按钮）
│   ├── medical/
│   │   ├── create/         # 开病历（核心功能）
│   │   └── list/           # 病历历史
│   ├── prescription/
│   │   ├── list/           # 药方列表
│   │   └── edit/           # 药方编辑
│   └── herb/
│       └── list/           # 药材库
├── app.json                # 页面路由配置
├── app.wxss                # 全局样式
└── README.md               # 本文档
```

## 使用流程（验收标准）

### 1. 准备数据

1. 打开"药材库"，添加3个药材：
   - 黄芪
   - 党参
   - 甘草

2. 打开"制作药方"，新建2个药方：
   
   **药方1：补气汤**
   - 黄芪 10g
   - 党参 5g
   
   **药方2：健脾方**
   - 党参 8g （注意：与药方1有重复）
   - 甘草 3g

### 2. 开病历

1. 点击首页"开病历"
2. 输入患者姓名：王一帆
3. 选择日期：保持今天
4. 勾选两个药方：补气汤、健脾方
5. 点击"保存病历"

**预期结果：**
- 弹窗显示保存成功
- 药方：补气汤,健脾方
- 所需药材：**党参 8g**（取最大值），甘草 3g，黄芪 10g
- 可以点击"复制药材清单"

### 3. 查看历史

1. 点击"去看历史"（或从首页进入"病历历史"）
2. 输入"王一帆"并搜索
3. 应该能看到刚才创建的病历记录
4. 点击记录可查看详情

## API 对接说明

### 后端响应格式

所有接口返回统一格式：

```json
{
  "code": 0,
  "message": "success",
  "data": { ... },
  "traceId": "..."
}
```

- `code === 0` 表示成功
- `code !== 0` 自动 toast 显示 `message`

### 主要接口

**药材库**
- `GET /api/v1/herbs?keyword=&page=1&size=50`
- `POST /api/v1/herbs` - body: `{ "nameCn": "黄芪" }`
- `DELETE /api/v1/herbs/{id}`

**药方库**
- `GET /api/v1/prescriptions?keyword=&page=1&size=50`
- `GET /api/v1/prescriptions/{id}`
- `POST /api/v1/prescriptions` - body: `{ "name": "补气汤", "items": [{"herbId":1,"doseG":"10"}] }`
- `PUT /api/v1/prescriptions/{id}`
- `DELETE /api/v1/prescriptions/{id}`

**病历管理**
- `POST /api/v1/medical-records` - body: `{ "patientName":"王一帆", "visitDate":"2026-02-07", "prescriptionIds":[1,3] }`
- `GET /api/v1/medical-records?patientName=王一帆&page=1&size=50`

## 常见问题

### Q1: 网络请求失败？

**检查清单：**
1. 后端服务是否启动？（访问 http://localhost:8081/swagger-ui.html 确认）
2. `config/env.js` 中的IP地址是否正确？
3. 微信开发者工具是否勾选了"不校验域名"？

### Q2: 药材选择器是空的？

需要先去"药材库"添加药材后，才能在药方编辑中选择。

### Q3: Toast 提示"网络错误/后端未启动"？

1. 确认后端在运行
2. 如果后端在另一台电脑，确保手机/开发者工具能访问到该IP
3. 检查防火墙是否阻止了 8081 端口

## 设计理念

### 为妈妈优化
-  **大按钮**：所有主要按钮都加大字号和间距
- ✨ **简单流程**：必要操作3步以内完成
- 📱 **清晰提示**：所有操作都有toast反馈
- 🎯 **核心突出**：首页最大按钮是"开病历"

### 交互细节
- 所有成功操作都有 toast 提示
- 删除操作需要二次确认
- 保存病历后显示详细结果弹窗
- 药材清单一键复制方便分享

## 后续优化方向（可选）

- [ ] 病历列表支持下拉刷新
- [ ] 药方支持更多字段（功效、注意事项）
- [ ] 支持导出病历为图片或PDF
- [ ] 添加常用药方模板
- [ ] 支持病历修改/删除

## 技术支持

- 微信小程序官方文档：https://developers.weixin.qq.com/miniprogram/dev/framework/
- 后端项目文档：`../ljc-tcm-prescription-service/README.md`

---

## 开发者注意事项

### 字段名对应关系

**后端 → 前端**
- `nameCn` → 药材名称字段
- `content` → Page分页响应的列表字段
- `prescriptionIds` → 病历请求参数（不是ids）
- `mergedHerbsText` → 已格式化的药材文本（含g单位）

### 错误处理

`utils/request.js` 已统一处理：
- HTTP错误
- code !== 0 的业务错误
- 网络超时

所有页面只需关注成功场景的逻辑。

---

Made with ❤️ for Mom
