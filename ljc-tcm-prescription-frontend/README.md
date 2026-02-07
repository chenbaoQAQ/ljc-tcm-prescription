# TCM Prescription 微信小程序前端

中医药方管理微信小程序，支持药材库管理、药方管理和多药方合并功能。

## 功能特性

- ✅ **药材库管理**：增删改查药材，支持搜索
- ✅ **药方库管理**：增删改查药方，支持搜索
- ✅ **药方编辑**：从药材库选择药材并设置克重
- ✅ **多药方合并**：一次选择多个药方生成总药材清单
- ✅ **合并规则**：同名药材自动取最大克重（由后端处理）
- ✅ **一键复制**：合并结果支持一键复制到剪贴板

## 技术栈

- 微信小程序原生开发（WXML + WXSS + JS）
- 无第三方 UI 框架依赖
- 统一 API 封装
- RESTful 接口对接

## 项目结构

```
ljc-tcm-prescription-frontend/
├── pages/                      # 页面目录
│   ├── home/                   # 首页
│   │   ├── index.js
│   │   ├── index.json
│   │   ├── index.wxml
│   │   └── index.wxss
│   ├── herb/                   # 药材库
│   │   ├── list.js
│   │   ├── list.json
│   │   ├── list.wxml
│   │   └── list.wxss
│   ├── prescription/           # 药方库
│   │   ├── list.js             # 药方列表
│   │   ├── list.json
│   │   ├── list.wxml
│   │   ├── list.wxss
│   │   ├── edit.js             # 药方编辑
│   │   ├── edit.json
│   │   ├── edit.wxml
│   │   └── edit.wxss
│   └── merge/                  # 合并功能
│       ├── index/              # 选择药方
│       │   ├── index.js
│       │   ├── index.json
│       │   ├── index.wxml
│       │   └── index.wxss
│       └── result/             # 合并结果
│           ├── result.js
│           ├── result.json
│           ├── result.wxml
│           └── result.wxss
├── api/                        # API 接口封装
│   ├── herb.js                 # 药材 API
│   └── prescription.js         # 药方 API
├── utils/                      # 工具函数
│   └── request.js              # 统一请求封装
├── app.js                      # 小程序入口
├── app.json                    # 小程序配置
├── app.wxss                    # 全局样式
├── project.config.json         # 项目配置
└── README.md                   # 本文档

```

## 快速开始

### 1. 前置准备

- 下载并安装 [微信开发者工具](https://developers.weixin.qq.com/miniprogram/dev/devtools/download.html)
- 确保后端服务 `ljc-tcm-prescription-service` 已启动（默认端口 8080）

### 2. 导入项目

1. 打开微信开发者工具
2. 选择"导入项目"
3. 选择项目目录：`ljc-tcm-prescription-frontend`
4. AppID 选择"测试号"（或使用自己的 AppID）
5. 点击"导入"

### 3. 配置后端地址

修改 `utils/request.js` 中的 `baseUrl`：

```javascript
const baseUrl = 'http://localhost:8080/api/v1';  // 开发环境
```

如果后端不在本地，修改为实际地址（如 `http://192.168.1.100:8080/api/v1`）。

### 4. 配置不校验域名（开发环境）

由于小程序要求 HTTPS，在开发阶段需要关闭域名校验：

1. 在微信开发者工具右上角，点击"详情"
2. 在"本地设置"中，勾选"不校验合法域名、web-view（业务域名）、TLS 版本以及 HTTPS 证书"

### 5. 编译运行

点击微信开发者工具左上角的"编译"按钮即可运行。

## 页面说明

### 1. 首页 (pages/home)

三个入口按钮：
- 药材库管理
- 药方库管理
- 多药方合并开方

### 2. 药材库 (pages/herb/list)

**功能：**
- 搜索药材（按名称）
- 查看药材列表（名称、默认克重、状态）
- 新增药材（FAB 按钮）
- 编辑药材（卡片上的"Edit"按钮）
- 删除药材（卡片上的"Delete"按钮，带二次确认）

**字段：**
- 药材名称（必填）
- 单位（默认"g"）
- 默认克重（可选）
- 状态（开关：启用/禁用）

### 3. 药方库 (pages/prescription/list)

**功能：**
- 搜索药方（按名称）
- 查看药方列表（名称、药味数量、更新时间）
- 新增药方（FAB 按钮）
- 编辑药方（跳转到编辑页）
- 删除药方（带二次确认）

### 4. 药方编辑 (pages/prescription/edit)

**功能：**
- 输入药方名称（必填）
- 输入药方描述（可选）
- 添加药材：从药材库选择
- 设置每味药的克重（必须 > 0）
- 删除药材
- 保存药方

**校验规则：**
- 药方名称不能为空
- 至少包含 1 味药
- 每味药的克重必须 > 0
- 同一药方中药材不可重复

### 5. 多药方合并 (pages/merge/index)

**功能：**
- 显示所有药方列表
- 支持搜索药方
- 多选药方（checkbox）
- 点击"生成总药材清单"跳转到结果页

**校验：**
- 至少选择 1 个药方

### 6. 合并结果 (pages/merge/result)

**功能：**
- 展示合并后的总药材清单
- 每味药显示：药材名 + 总克重
- 可选：显示来源明细（sources）
- 一键复制到剪贴板（格式：`药材名 克重g`）

**合并规则（后端实现）：**
- 同名药材取最大克重

## API 接口对接

所有接口通过 `utils/request.js` 统一封装，自动处理：
- BASE URL 拼接
- 统一错误处理（code != 0 时弹 toast）
- 网络异常处理

### 药材接口 (api/herb.js)

```javascript
getHerbs(keyword)           // GET /herbs?keyword=xxx
createHerb(data)            // POST /herbs
updateHerb(id, data)        // PUT /herbs/:id
deleteHerb(id)              // DELETE /herbs/:id
```

### 药方接口 (api/prescription.js)

```javascript
getPrescriptions(keyword)   // GET /prescriptions?keyword=xxx
getPrescription(id)         // GET /prescriptions/:id
createPrescription(data)    // POST /prescriptions
updatePrescription(id, data)// PUT /prescriptions/:id
deletePrescription(id)      // DELETE /prescriptions/:id
mergePrescriptions(ids)     // POST /prescriptions/merge
```

## 后端接口要求

后端需要提供以下接口（base path: `/api/v1`）：

### 药材接口

- `GET /herbs` - 获取药材列表（支持 keyword 搜索）
- `POST /herbs` - 创建药材
- `PUT /herbs/:id` - 更新药材
- `DELETE /herbs/:id` - 删除药材

### 药方接口

- `GET /prescriptions` - 获取药方列表（支持 keyword 搜索）
- `GET /prescriptions/:id` - 获取单个药方详情（含药味明细）
- `POST /prescriptions` - 创建药方
- `PUT /prescriptions/:id` - 更新药方
- `DELETE /prescriptions/:id` - 删除药方
- `POST /prescriptions/merge` - 合并多个药方

### 响应格式

```json
{
  "code": 0,           // 0 表示成功，非 0 表示失败
  "message": "成功",
  "data": { ... }      // 具体数据
}
```

## 开发注意事项

### 1. 输入校验

- 克重输入框自动过滤非数字字符（只允许数字和小数点）
- 所有删除操作都有二次确认弹窗
- 保存成功后显示 toast 提示

### 2. 项目配置

- `project.config.json`：微信开发者工具项目配置
- `sitemap.json`：小程序索引配置
- 不要将开发者工具生成的临时文件推到 git

### 3. 正式发布前

1. 修改 `baseUrl` 为生产环境地址（HTTPS）
2. 在微信公众平台配置服务器域名白名单
3. 关闭"不校验域名"选项
4. 上传代码并提交审核

## 验收清单

- [x] 能新增药材
- [x] 能新增药方并选择药材输入克重
- [x] 能多选两个药方进行 merge
- [x] 合并结果显示总药材清单
- [x] 同名药材显示后端返回的最大克重
- [x] 支持一键复制合并结果

## 常见问题

### Q1: 小程序启动后提示网络错误？

**A:** 
1. 确认后端服务已启动（`http://localhost:8080`）
2. 确认微信开发者工具已关闭域名校验
3. 如果后端不在本地，修改 `utils/request.js` 中的 `baseUrl`

### Q2: 如何查看网络请求？

**A:** 在微信开发者工具中，点击右下角"调试器" -> "Network"，可查看所有 HTTP 请求。

### Q3: 如何清空数据重新测试？

**A:** 
1. 在微信开发者工具中，点击顶部菜单"工具" -> "清缓存"
2. 或者直接重启后端服务并重新初始化数据库

## 技术支持

如有问题，请查看：
- 微信小程序官方文档：https://developers.weixin.qq.com/miniprogram/dev/framework/
- 后端项目 README：`ljc-tcm-prescription-service/README.md`

## 许可证

MIT
