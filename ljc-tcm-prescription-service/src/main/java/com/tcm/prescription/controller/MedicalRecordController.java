package com.tcm.prescription.controller;

import com.tcm.prescription.common.Result;
import com.tcm.prescription.dto.MedicalRecordCreateReq;
import com.tcm.prescription.dto.MedicalRecordListItemResp;
import com.tcm.prescription.dto.MedicalRecordResp;
import com.tcm.prescription.service.MedicalRecordService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/v1/medical-records")
@RequiredArgsConstructor
@Validated
@Tag(name = "Medical Records", description = "病历管理 API")
public class MedicalRecordController {

    private final MedicalRecordService medicalRecordService;

    @PostMapping
    @Operation(summary = "创建病历", description = "保存病历记录并生成合并药材快照")
    public Result<MedicalRecordResp> create(@Valid @RequestBody MedicalRecordCreateReq req) {
        log.info("Creating medical record for patient: {}", req.getPatientName());
        MedicalRecordResp resp = medicalRecordService.createMedicalRecord(req);
        return Result.success(resp);
    }

    @GetMapping
    @Operation(summary = "按姓名查询病历列表", description = "支持模糊搜索和分页")
    public Result<Map<String, Object>> list(
            @RequestParam @NotBlank(message = "Patient name is required") String patientName,
            @RequestParam(defaultValue = "1") @Min(1) int page,
            @RequestParam(defaultValue = "50") @Min(1) int size) {
        
        log.info("Listing medical records for patient: {}, page: {}, size: {}", patientName, page, size);
        Page<MedicalRecordListItemResp> pageResult = medicalRecordService.listByPatientName(patientName, page, size);

        Map<String, Object> data = new HashMap<>();
        data.put("list", pageResult.getContent());
        data.put("page", page);
        data.put("size", size);
        data.put("total", pageResult.getTotalElements());

        return Result.success(data);
    }

    @GetMapping("/{id}")
    @Operation(summary = "获取病历详情")
    public Result<MedicalRecordResp> getDetail(@PathVariable Long id) {
        log.info("Getting medical record detail: {}", id);
        MedicalRecordResp resp = medicalRecordService.getDetail(id);
        return Result.success(resp);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除病历", description = "软删除")
    public Result<Void> delete(@PathVariable Long id) {
        log.info("Deleting medical record: {}", id);
        medicalRecordService.delete(id);
        return Result.success(null);
    }
}
