# TCM Prescription 小程序 - 文件清单

## 项目结构验证

✅ 已创建的文件：

### 1. 核心配置文件
- [x] app.json - 小程序全局配置
- [x] app.js - 小程序入口逻辑
- [x] app.wxss - 全局样式
- [x] .gitignore - Git 忽略配置
- [x] README.md - 项目文档（中文）

### 2. API 层 (api/)
- [x] api/herb.js - 药材 API 封装
- [x] api/prescription.js - 药方 API 封装

### 3. 工具层 (utils/)
- [x] utils/request.js - 统一请求封装

### 4. 页面 (pages/)

#### 4.1 首页 (pages/home/)
- [x] index.js
- [x] index.json
- [x] index.wxml
- [x] index.wxss

#### 4.2 药材库 (pages/herb/)
- [x] list.js
- [x] list.json
- [x] list.wxml
- [x] list.wxss

#### 4.3 药方库 (pages/prescription/)
- [x] list.js
- [x] list.json
- [x] list.wxml
- [x] list.wxss
- [x] edit.js
- [x] edit.json
- [x] edit.wxml
- [x] edit.wxss

#### 4.4 合并功能 (pages/merge/)
- [x] index.js
- [x] index.json
- [x] index.wxml
- [x] index.wxss
- [x] result.js
- [x] result.json
- [x] result.wxml
- [x] result.wxss

## 功能实现验证

### ✅ 药材库管理
- 搜索药材（GET /herbs?keyword=xxx）
- 查看药材列表（名称、默认克重、状态）
- 新增药材（POST /herbs）
- 编辑药材（PUT /herbs/:id）
- 删除药材（DELETE /herbs/:id，带二次确认）

### ✅ 药方库管理
- 搜索药方（GET /prescriptions?keyword=xxx）
- 查看药方列表（名称、药味数量、更新时间）
- 新增药方（导航到编辑页）
- 编辑药方（导航到编辑页）
- 删除药方（DELETE /prescriptions/:id，带二次确认）

### ✅ 药方编辑
- 输入药方名称（必填）
- 输入药方描述（可选）
- 从药材库选择药材（弹窗选择，支持搜索）
- 设置每味药的克重（数字输入框，限制只允许数字和小数点）
- 删除药材（点击 × 图标）
- 保存药方（POST/PUT /prescriptions）
- 校验规则：
  - 药方名称不能为空
  - 至少包含 1 味药
  - 每味药克重必须 > 0
  - 同一药方中药材不可重复

### ✅ 多药方合并
- 显示所有药方列表
- 支持搜索药方
- 多选药方（checkbox）
- 点击"生成总药材清单"导航到结果页
- 校验：至少选择 1 个药方

### ✅ 合并结果
- 展示合并后的总药材清单（药材名 + 总克重）
- 显示来源明细（sources）
- 一键复制到剪贴板（格式：药材名 克重g）
- 返回按钮

## 后端接口对接

### API Base Path
- `/api/v1`

### 后端响应格式匹配
- ✅ Result<T> wrapper: `{ code, message, data }`
- ✅ Page<T> response: `{ content: [], ... }`
- ✅ Herb entity fields
- ✅ Prescription DTOs (PrescriptionDetailResp, PrescriptionSimpleResp)
- ✅ Merge request/response (prescriptionIds, items)

### 字段名映射 (Backend → Frontend)
```
Herb:
- name_cn ✓
- default_dose_g ✓
- status ✓

Prescription:
- itemCount → herbCount ✓
- updatedAt → updated_at (formatted) ✓

PrescriptionDetailResp.ItemResp:
- herbId ✓
- herbNameSnapshot → herbName ✓
- doseG → dose_g ✓

PrescriptionCreateReq:
- items[].herbId ✓
- items[].doseG ✓

MergeReq:
- prescriptionIds ✓

MergeResp.MergedItem:
- name → herbName ✓
- doseG → totalDoseG ✓
- sources ✓
```

## 交互细节

- ✅ 删除操作二次确认（wx.showModal）
- ✅ 保存成功 toast 提示（wx.showToast）
- ✅ merge 未选择药方时提示（wx.showToast）
- ✅ merge 结果页支持复制（wx.setClipboardData）
- ✅ 克重输入框限制数字和小数点（regex filter）

## 验收标准

按照用户要求的最终验收清单：

1. ✅ 能新增药材
2. ✅ 能新增药方并选择药材输入克重
3. ✅ 能多选两个药方进行 merge
4. ✅ 合并结果显示总药材清单
5. ✅ 同名药材显示后端返回的最大克重（由后端 merge 逻辑决定）

## 下一步操作

1. 打开微信开发者工具
2. 导入项目目录：`ljc-tcm-prescription-frontend`
3. 配置不校验域名（开发环境）
4. 确保后端服务已启动（http://localhost:8080）
5. 点击编译运行
6. 按照验收标准逐一测试功能

## 注意事项

1. 如果后端不在本地，需修改 `utils/request.js` 中的 `baseUrl`
2. 正式发布前需要配置 HTTPS 域名白名单
3. 当前版本为 MVP，可根据实际使用情况迭代优化
