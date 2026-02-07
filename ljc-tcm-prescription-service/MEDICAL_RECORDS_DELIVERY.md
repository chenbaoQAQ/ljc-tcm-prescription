# 病历功能交付文档

## 1. 新增数据库表 DDL

已在 `sql/schema.sql` 中添加第 4 张表：

```sql
-- 4. Medical Records Table (病历表)
CREATE TABLE IF NOT EXISTS `medical_records` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `patient_name` VARCHAR(64) NOT NULL COMMENT 'Patient Name (患者姓名)',
    `visit_date` DATE NOT NULL COMMENT 'Visit Date (就诊日期)',
    `prescription_ids_json` TEXT NOT NULL COMMENT 'Selected Prescription IDs JSON (选中的药方ID列表)',
    `prescription_names_snapshot` VARCHAR(512) NOT NULL COMMENT 'Prescription Names Snapshot (药方名快照，逗号分隔)',
    `merged_herbs_json` MEDIUMTEXT NOT NULL COMMENT 'Merged Herbs Snapshot JSON (合并后的药材清单快照)',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted_at` DATETIME NULL COMMENT 'Soft Delete Timestamp (软删除时间)',
    KEY `idx_mr_patient_name` (`patient_name`),
    KEY `idx_mr_visit_date` (`visit_date`),
    KEY `idx_mr_deleted_at` (`deleted_at`),
    KEY `idx_mr_patient_date` (`patient_name`, `visit_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Medical Records (病历表)';
```

**字段说明：**
- `prescription_ids_json`: 保存选中的药方 ID 列表，格式：`[1, 3, 5]`
- `prescription_names_snapshot`: 药方名快照（逗号分隔），格式：`"补气方,调理方"`
- `merged_herbs_json`: 合并后的药材清单（JSON 数组），示例见下方

## 2. 新增接口列表

Base Path: `/api/v1/medical-records`

| 方法 | 路径 | 描述 | 请求示例 |
|------|------|------|----------|
| POST | `/` | 创建病历 | 见下方示例 |
| GET | `/?patientName={name}&page={page}&size={size}` | 按姓名查询病历列表 | `?patientName=王一帆&page=1&size=50` |
| GET | `/{id}` | 获取病历详情 | - |
| DELETE | `/{id}` | 删除病历（软删除） | - |

### 接口示例

#### 创建病历

```bash
curl -X POST http://localhost:8081/api/v1/medical-records \
  -H "Content-Type: application/json" \
  -d '{
    "patientName": "王一帆",
    "visitDate": "2026-02-07",
    "prescriptionIds": [1, 3]
  }'
```

**响应示例：**
```json
{
  "code": 0,
  "message": "success",
  "data": {
    "id": 1,
    "patientName": "王一帆",
    "visitDate": "2026-02-07",
    "prescriptionIds": [1, 3],
    "prescriptionNames": "补气方,调理方",
    "mergedHerbs": [
      {"name": "黄芪", "doseG": "15"},
      {"name": "当归", "doseG": "10"}
    ],
    "mergedHerbsText": "黄芪 15g, 当归 10g"
  },
  "traceId": "..."
}
```

#### 查询病历列表

```bash
curl -X GET "http://localhost:8081/api/v1/medical-records?patientName=王一帆&page=1&size=50"
```

**响应示例：**
```json
{
  "code": 0,
  "message": "success",
  "data": {
    "list": [
      {
        "id": 1,
        "patientName": "王一帆",
        "visitDate": "2026-02-07",
        "prescriptionNames": "补气方,调理方",
        "mergedHerbsText": "黄芪 15g, 当归 10g",
        "mergedHerbs": [
          {"name": "黄芪", "doseG": "15"},
          {"name": "当归", "doseG": "10"}
        ]
      }
    ],
    "page": 1,
    "size": 50,
    "total": 1
  },
  "traceId": "..."
}
```

## 3. merged_herbs_json 结构说明

数据库中存储的 `merged_herbs_json` 字段格式为 JSON 数组：

```json
[
  {
    "name": "黄芪",
    "doseG": 15.00
  },
  {
    "name": "当归",
    "doseG": 10.00
  },
  {
    "name": "白术",
    "doseG": 8.00
  }
]
```

**特点：**
- 使用 `BigDecimal` 存储克重，精度 `DECIMAL(10,2)`
- 按药材名称自然排序
- **合并规则：同一药材取最大克重**（不累加）

## 4. 单元测试通过截图

```
[INFO] Running com.tcm.prescription.service.MedicalRecordServiceTest
[INFO] Tests run: 4, Failures: 0, Errors: 0, Skipped: 0
[INFO] Running com.tcm.prescription.service.PrescriptionServiceTest
[INFO] Tests run: 3, Failures: 0, Errors: 0, Skipped: 0
[INFO] 
[INFO] Results:
[INFO] Tests run: 7, Failures: 0, Errors: 0, Skipped: 0
[INFO] BUILD SUCCESS
```

**测试覆盖：**
- ✅ 合并取最大克重逻辑验证（A10 + A5 → A10）
- ✅ 药方不存在抛出 NOT_FOUND 异常
- ✅ 空药方列表抛出 PARAM_ERROR 异常
- ✅ 患者姓名校验（空白名字抛异常）

## 5. Swagger 文档地址

启动服务后访问：**http://localhost:8081/swagger-ui.html**

在 "Medical Records" 标签下可以看到所有病历相关接口。

## 6. Smoke Test

已扩展 `scripts/smoke-test.sh` 包含病历测试：

```bash
./scripts/smoke-test.sh
```

**测试流程：**
1. 创建药材 A、B、C
2. 创建药方 P1(A10, B20)、P2(A5, C10)
3. 测试药方合并接口
4. **创建病历（选择 P1 和 P2）**
5. **按姓名查询病历列表**
6. **获取病历详情**

输出示例：
```
=== TCM Prescription Service Smoke Test ===
...
4. Testing Medical Records...
  -> Create Medical Record: {"code":0,"message":"success","data":{...}}
✅ Medical Record Created: ID(1)

5. Querying Medical Records by Patient Name...
✅ Medical Record Query Successful

6. Getting Medical Record Detail...
✅ Medical Record Detail Retrieved

=== SMOKE TEST PASSED ===
```

## 7. 核心逻辑说明

### 合并规则实现

`MedicalRecordService.mergeHerbsInternal()` 方法：

1. 批量获取所有选中药方的明细项
2. 按 `herb_id` 分组
3. 每组取最大 `dose_g`（使用 `BigDecimal.compareTo()`）
4. 按药材名称自然排序
5. 序列化为 JSON 存入 `merged_herbs_json`

### 历史稳定性保证

- **药方名快照**：存储时保存药方名，即使未来改名也不影响历史记录
- **药材名快照**：从 `prescription_items.herb_name_snapshot` 读取
- **克重快照**：合并结果直接存入 JSON，不依赖实时计算

## 8. 文件清单

### 新增文件
```
src/main/java/com/tcm/prescription/
├── controller/MedicalRecordController.java
├── dto/
│   ├── MedicalRecordCreateReq.java
│   ├── MedicalRecordListItemResp.java
│   └── MedicalRecordResp.java
├── entity/MedicalRecord.java
├── repository/MedicalRecordRepository.java
└── service/MedicalRecordService.java

src/test/java/com/tcm/prescription/
└── service/MedicalRecordServiceTest.java
```

### 修改文件
```
sql/schema.sql                    # 新增 medical_records 表
scripts/smoke-test.sh             # 扩展病历测试步骤
```

## 9. 验证清单

- [x] `./mvnw clean test` 通过（7/7 测试）
- [x] Docker Compose 启动 MySQL 正常
- [x] Schema.sql 可重复执行（CREATE IF NOT EXISTS）
- [x] Swagger 文档可访问并展示病历接口
- [x] Smoke Test 全流程通过
- [x] 合并规则正确实现（取最大克重）
- [x] 软删除正常工作
- [x] 历史记录稳定性保证（快照机制）

---

✅ **病历功能开发完成，所有测试通过，可交付前端联调。**
